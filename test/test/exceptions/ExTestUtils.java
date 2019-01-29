package test.exceptions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.dzh.bytesutil.converters.auxiliary.exceptions.ExactException;

public class ExTestUtils {
	public static boolean assertException(Throwable leaf, Class<? extends Throwable> expected) {
		Throwable original = leaf;
		while(leaf!=null) {
			if(leaf.getClass() == expected) {
				return true;
			}
			leaf = leaf.getCause();
		}
		throw new IllegalArgumentException(expected+" not found",original);
	}
	public static boolean assertExactException(Throwable ex,Class<?> site, int ordinal) {
		if(!((ex instanceof ExactException)
		&& ((ExactException)ex).getSite() == site
		&& ((ExactException)ex).getOrdinal() == ordinal)){
			throw new IllegalArgumentException(ex+" not expected");
		}
		return true;
	}
	public static ByteArrayOutputStream newByteArrayOutputStream() {
		return new ByteArrayOutputStream();
	}
	public static InputStream newZeroLengthInputStream() {
		return new ByteArrayInputStream(new byte[0]);
	}
	public static OutputStream newThrowOnlyOutputStream() {
		return new OutputStream() {
			
			@Override
			public void write(int b) throws IOException {
				throw new IOException("throws");
			}
		};
	}
}
