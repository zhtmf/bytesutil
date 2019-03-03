package org.dzh.bytesutil.converters.auxiliary;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * An input stream implementation that supports <tt>mark()</tt> and
 * <tt>reset()</tt> operations but does not maintain any internal buffer.
 * <p>
 * Due to the way how this library is used, the <tt>InputStream</tt> object
 * passed in by client code may be used again by client code thereafter. Classes
 * like {@link java.io.BufferedInputStream BufferedInputStream} effectively
 * prevent such use because of their internal buffering mechanics, which reads
 * more data than needed and those data cannot be put back to the original
 * stream.
 * 
 * @author dzh
 */
public final class MarkableInputStream extends InputStream implements AutoCloseable{
	
	private static final int[] SHARED_EMPTY_BUFFER = new int[0];
	private InputStream in;
	private int[] buffer = SHARED_EMPTY_BUFFER;
	private int bufferUpperLimit;
	private int bufferReadPos;
	private int bufferFillPos;
	
	public MarkableInputStream(InputStream is) {
		if(is==null) {
			throw new NullPointerException();
		}
		this.in = is;
		reset0();
	}
	
	@Override
	public int read() throws IOException {
		checkClosed();
		if(!marked())
			return in.read();
		if(bufferReadPos>=bufferUpperLimit) {
			/*
			 * throw away all bytes recorded after last call to mark() 
			 * because more data than readlimit has been read
			 */
			reset0();
			return in.read();
		}else {
			if(bufferReadPos<bufferFillPos) {
				return buffer[bufferReadPos++];
			}
			int b = in.read();
			if(b==-1) {
				return b;
			}
			buffer[bufferReadPos++] = b;
			++bufferFillPos;
			return b;
		}
	}

	@Override
	public void mark(int readlimit) {
		checkClosed();
		if(readlimit<=0) {
			throw new IllegalArgumentException("invalid mark limit "+readlimit);
		}
		if(marked()) {
			/*
			 * mark again, throw away bytes before bufferReadPos 
			 * and copy the remaining data to the beginning of buffer
			 */
			if(readlimit>buffer.length) {
				this.buffer = Arrays.copyOf(buffer, readlimit);
			}
			System.arraycopy(this.buffer, bufferReadPos, buffer, 0, bufferUpperLimit - bufferReadPos);
			bufferUpperLimit = readlimit;
			bufferFillPos -= bufferReadPos;
			bufferFillPos = Math.min(bufferFillPos, bufferUpperLimit);
			bufferReadPos = 0;
			return;
		}
		bufferUpperLimit = readlimit;
		buffer = Arrays.copyOf(buffer, readlimit);
		bufferReadPos = 0;
		bufferFillPos = 0;
	}

	@Override
	public void reset() throws IOException {
		if(marked()) {
			bufferReadPos = 0;
		}else {
			throw new IOException("not marked or already depleted buffer");
		}
	}
	public int remaining() {
		return marked() ? bufferFillPos - bufferReadPos : 0;
	}
	public boolean marked() {
		return bufferReadPos>=0;
	}

	private void reset0() {
		bufferUpperLimit = -1;
		bufferReadPos = -1;
		bufferFillPos = -1;
	}
	private void checkClosed() {
		if(in==null)
			throw new IllegalStateException("stream closed");
	}
	public void close() throws IOException {
		reset0();
		in = null;//intended
	}
	public int available() throws IOException {return in.available();}
	public int read(byte[] b) throws IOException {return super.read(b);}
	public int read(byte[] b, int off, int len) throws IOException {return super.read(b, off, len);}
	public boolean markSupported() {return true;}
	@Override
	public long skip(long n) throws IOException {
		if(n<=0) {
			return 0;
		}
		long tmp = n;
		while(n-->0){
			int b = read();
			if(b==-1) {
				break;
			}
		}
		return tmp - n - 1;
	}
}
