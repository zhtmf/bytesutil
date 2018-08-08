package org.dzh.bytesutil.converters.auxiliary;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderMalfunctionError;
import java.nio.charset.CoderResult;

/**
 * A replacement for {@link java.io.InputStreamReader InputStreamReader}.
 * <p>
 * For the same reason as {@link MarkableStream}, <tt>InputStreamReader</tt>s
 * which maintains internal read-ahead buffer have negative impact on our
 * processing.
 * <p>
 * This class works by reading bytes from incoming stream, one at a time, until
 * decoder of specified charset successfully decodes a valid char from bytes
 * read so far.
 * 
 * @author dzh
 */
public class CharDecoder{
	private Charset cs;
	private CharsetDecoder cd;
	private ByteBuffer buffers[] = new ByteBuffer[] {
			ByteBuffer.allocate(0),
			ByteBuffer.allocate(1),
			ByteBuffer.allocate(2),
			ByteBuffer.allocate(3),
			ByteBuffer.allocate(4),
	};
	private CharBuffer out = CharBuffer.allocate(1);
	private int pos = 1;
	private int ch = -1;
	public CharDecoder(Charset cs) {
		this.cs = cs;
		this.cd = cs.newDecoder();
	}
	public char decodeAndReset(InputStream is) throws IOException {
		while( ! decode(is));
		char ret = getChar();
		reset();
		return ret;
	}
	public boolean decode(InputStream is) throws IOException {
		if(pos==buffers.length) {
			throw new IllegalStateException("invalid byte sequence for charset："+cs);
		}
		ByteBuffer buffer = buffers[pos];
		CharBuffer out = this.out;
		CharsetDecoder cd = this.cd;
		buffer.put(buffers[pos-1]);
		int tmp = is.read();
		if(tmp==-1) {
			throw new EOFException();
		}
		buffer.put((byte)tmp);
		buffer.position(0);
		try {
			CoderResult cr = cd.decode(buffer, out, true);
			if(cr.isUnderflow()) {
				cd.flush(out);
				ch = out.get(0);
				return true;
			}
		} catch (CoderMalfunctionError e) {
		}
		++pos;
		return false;
	}
	public char getChar() {
		if(ch==-1) {
			throw new IllegalStateException();
		}else {
			return (char)ch;
		}
	}
	public void reset() {
		ch = -1;
		cd.reset();
		out.clear();
		pos = 1;
		for(ByteBuffer bb:buffers) {
			bb.clear();
		}
	}
}
