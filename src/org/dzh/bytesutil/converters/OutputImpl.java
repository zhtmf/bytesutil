package org.dzh.bytesutil.converters;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.dzh.bytesutil.TypeConverter;
import org.dzh.bytesutil.TypeConverter.Output;
import org.dzh.bytesutil.converters.auxiliary.FieldInfo;
import org.dzh.bytesutil.converters.auxiliary.StreamUtils;
import org.dzh.bytesutil.converters.auxiliary.exceptions.UnsatisfiedIOException;

class OutputImpl implements TypeConverter.Output {
	private FieldInfo fieldInfo;
	private OutputStream dest;
	private int written;
	private int fastLength;
	private static final ThreadLocal<OutputImpl> threadLocal = new ThreadLocal<OutputImpl>() {
		protected OutputImpl initialValue() {
			return new OutputImpl();
		};
	};
	
	private OutputImpl() {}
	
	private void reset(FieldInfo fieldInfo,OutputStream dest, int length) {
		this.fieldInfo = fieldInfo;
		this.dest = dest;
		this.written = 0;
		this.fastLength = length;
	}
	private void checkBytesToWrite(int n) throws IOException {
		if(written+n>fastLength) {
			throw new UnsatisfiedIOException("attempting to write more than "+length()+" bytes")
					.withSiteAndOrdinal(Output.class, 1);
		}
		written += n;
	}
	
	public static OutputImpl getThreadLocalInstance(FieldInfo fieldInfo,OutputStream dest, int length) {
		OutputImpl ret = threadLocal.get();
		ret.reset(fieldInfo, dest, length);
		return ret;
	}

	@Override
	public boolean isUnsigned() {
		return fieldInfo.unsigned;
	}
	
	@Override
	public boolean isSigned() {
		return fieldInfo.signed;
	}
	
	@Override
	public boolean isLittleEndian() {
		return fieldInfo.littleEndian;
	}
	
	@Override
	public boolean isBigEndian() {
		return fieldInfo.bigEndian;
	}
	
	@Override
	public String getName() {
		return fieldInfo.name;
	}
	
	@Override
	public Class<?> getFieldClass() {
		return fieldInfo.getFieldType();
	}
	
	@Override
	public Class<?> getEntityClass() {
		return fieldInfo.getEntityType();
	}
	
	@Override
	public String getDatePattern() {
		return fieldInfo.datePattern;
	}
	
	@Override
	public Charset getCharset() {
		return fieldInfo.charset;
	}

	@Override
	public int length() {
		return fastLength;
	}

	@Override
	public void writeByte(byte n) throws IOException {
		checkBytesToWrite(1);
		StreamUtils.writeBYTE(dest, n);
	}

	@Override
	public void writeBytes(byte[] array) throws IOException {
		checkBytesToWrite(array.length);
		StreamUtils.writeBytes(dest, array);
	}

	@Override
	public void writeShort(short n) throws IOException {
		checkBytesToWrite(2);
		StreamUtils.writeSHORT(dest, n, isBigEndian());
	}

	@Override
	public void writeInt(int n) throws IOException {
		checkBytesToWrite(4);
		StreamUtils.writeInt(dest, n, isBigEndian());
	}

	@Override
	public void writeLong(long n) throws IOException {
		checkBytesToWrite(8);
		StreamUtils.writeLong(dest, n, isBigEndian());
	}

	@Override
	public long written() {
		return written;
	}

}
