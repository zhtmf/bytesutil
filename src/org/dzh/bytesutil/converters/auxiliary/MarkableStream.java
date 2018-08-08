package org.dzh.bytesutil.converters.auxiliary;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * An input stream implementation that supports <tt>mark()</tt> and
 * <tt>reset()</tt> operations but does not maintain any internal buffer.
 * <p>
 * Due to the way how this library is used, the <tt>InputStream</tt> object
 * passed in by client code will continue to be used again by client code after
 * we finish our work. Classes like {@link java.io.BufferedInputStream
 * BufferedInputStream} effectively prevent such use
 * because of their internal buffering mechanics, which reads more data than
 * needed and those data are "detained" by them and cannot be "put back" to the
 * original stream.
 * 
 * @author dzh
 */
public final class MarkableStream extends InputStream implements AutoCloseable{
	
	private static final int MAX_BUFFER_SIZE = Integer.MAX_VALUE-8;
	private static final int INITIAL_BUFFER_SIZE = 8;
	private static final int[] SHARED_EMPTY_BUFFER = new int[0];
	private InputStream in;
	private int[] buffer;
	private int max;
	private int pos;
	private int cur;
	
	public MarkableStream(InputStream is) {
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
		if( ! expandIfNeeded()) {
			/*
			 * throw away all bytes recorded after last call to mark() 
			 * because more data than readlimit has been read
			 */
			reset0();
			return in.read();
		}else {
			if(pos<cur) {
				return buffer[pos++];
			}
			int b = in.read();
			if(b==-1) {
				return b;
			}
			buffer[pos++] = (byte) b;
			++cur;
			return b;
		}
	}

	@Override
	public void mark(int readlimit) {
		checkClosed();
		if(readlimit<INITIAL_BUFFER_SIZE) {
			readlimit = INITIAL_BUFFER_SIZE;
		}
		if(marked()) {
			/*
			 * mark again, throw away bytes before pos 
			 * and copy the remaining data to the beginning of buffer
			 */
			if(readlimit<buffer.length) {
				readlimit = buffer.length;
			}
			max = readlimit;
			System.arraycopy(buffer, pos, buffer, 0, buffer.length-pos);
			cur -= pos;
			pos = 0;
			return;
		}
		max = readlimit;
		buffer = Arrays.copyOf(buffer, INITIAL_BUFFER_SIZE);
		pos = 0;
		cur = 0;
	}

	@Override
	public void reset() throws IOException {
		if(marked()) {
			pos = 0;
		}else {
			throw new IOException("not marked or read passed max buffer size");
		}
	}

	@Override
	public int available() throws IOException {
		checkClosed();
		return marked() ? max-pos : 0;
	}
	
	private void reset0() {
		buffer = SHARED_EMPTY_BUFFER;
		max = -1;
		pos = -1;
		cur = -1;
	}
	public boolean marked() {
		return pos>=0;
	}
	private boolean expandIfNeeded() {
		int pos = this.pos;
		int length = buffer.length;
		if(pos>=length) {
			if(length == max) {
				return false;
			}
			length = length*2;
			if(length<0 || length>=MAX_BUFFER_SIZE) {
				throw new OutOfMemoryError();
			}
			length = Math.min(length, max);
			buffer = Arrays.copyOf(buffer, length);
		}
		return true;
	}
	private void checkClosed() {
		if(in==null)
			throw new IllegalStateException("Stream Closed");
	}
	public void close() throws IOException {
		reset0();
		in = null;
	}
	public int read(byte[] b) throws IOException {return super.read(b);}
	public int read(byte[] b, int off, int len) throws IOException {return super.read(b, off, len);}
	public boolean markSupported() {return true;}
	@Override
	public long skip(long n) throws IOException {
		if(n<=0) {
			return 0;
		}
		do{
			int b = read();
			if(b==-1) {
				break;
			}
		}while(n-->0);
		return n;
	}
}
