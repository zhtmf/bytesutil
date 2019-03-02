package org.dzh.bytesutil.converters;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;

import org.dzh.bytesutil.TypeConverter;
import org.dzh.bytesutil.TypeConverter.Input;
import org.dzh.bytesutil.converters.auxiliary.FieldInfo;
import org.dzh.bytesutil.converters.auxiliary.StreamUtils;
import org.dzh.bytesutil.converters.auxiliary.exceptions.UnsatisfiedIOException;

public class InputImpl implements TypeConverter.Input{
	private FieldInfo fieldInfo;
	private InputStream src;
	private int read;
	private int fastLength;
	private static final ThreadLocal<InputImpl> threadLocal = new ThreadLocal<InputImpl>() {
		protected InputImpl initialValue() {
			return new InputImpl();
		};
	};
	
	private InputImpl() {}
	
	private void reset(FieldInfo fieldInfo,InputStream src, int length) {
		this.fieldInfo = fieldInfo;
		this.src = src;
		this.read = 0;
		this.fastLength = length;
	}
	private void checkBytesToRead(int n) throws IOException {
		if(read+n>fastLength) {
			throw new UnsatisfiedIOException("attempting to read more than "+length()+" bytes")
					.withSiteAndOrdinal(Input.class, 1);
		}
		read += n;
	}
	public static InputImpl getThreadLocalInstance(FieldInfo fieldInfo,InputStream src, int length) {
		InputImpl ret = threadLocal.get();
		ret.reset(fieldInfo, src, length);
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
	public int readByte() throws IOException {
		checkBytesToRead(1);
		return StreamUtils.readByte(src, true);
	}

	@Override
	public byte[] readBytes(int n) throws IOException {
		checkBytesToRead(n);
		return StreamUtils.readBytes(src, n);
	}

	@Override
	public short readShort() throws IOException {
		checkBytesToRead(2);
		return (short) StreamUtils.readShort(src, true, isBigEndian());
	}

	@Override
	public int readInt() throws IOException {
		checkBytesToRead(4);
		return (int)StreamUtils.readInt(src, true, isBigEndian());
	}

	@Override
	public long readLong() throws IOException {
		checkBytesToRead(8);
		return StreamUtils.readLong(src, isBigEndian());
	}

	@Override
	public int readUnsignedByte() throws IOException {
		checkBytesToRead(1);
		return StreamUtils.readByte(src, false);
	}

	@Override
	public int readUnsignedShort() throws IOException {
		checkBytesToRead(2);
		return (short) StreamUtils.readShort(src, false, isBigEndian());
	}

	@Override
	public long readUnsignedInt() throws IOException {
		checkBytesToRead(4);
		return (int)StreamUtils.readInt(src, false, isBigEndian());
	}

	@Override
	public BigInteger readUnsignedLong() throws IOException {
		checkBytesToRead(8);
		return StreamUtils.readUnsignedLong(src, isBigEndian());
	}

	@Override
	public long available() {
		return fastLength - read;
	}

}
