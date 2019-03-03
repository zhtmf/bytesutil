package org.dzh.bytesutil.converters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;

import org.dzh.bytesutil.ConversionException;
import org.dzh.bytesutil.TypeConverter;
import org.dzh.bytesutil.TypeConverter.Input;
import org.dzh.bytesutil.TypeConverter.Output;
import org.dzh.bytesutil.converters.auxiliary.FieldInfo;
import org.dzh.bytesutil.converters.auxiliary.MarkableInputStream;
import org.dzh.bytesutil.converters.auxiliary.StreamUtils;
import org.dzh.bytesutil.converters.auxiliary.Utils;
import org.dzh.bytesutil.converters.auxiliary.exceptions.ExtendedConversionException;
import org.dzh.bytesutil.converters.auxiliary.exceptions.UnsatisfiedIOException;

public class UserDefinedTypeConverter implements Converter<Object> {
	@SuppressWarnings("unchecked")
	@Override
	public void serialize(Object value, OutputStream dest, FieldInfo ctx, Object self)
			throws IOException, ConversionException {
		Output output = OutputImpl.getThreadLocalInstance(
				ctx, dest
				, Utils.lengthForSerializingUserDefinedType(ctx, self)
				, Utils.charsetForSerializingCHAR(ctx, self)
				);
		ctx.userDefinedConverter.serialize(value,output);
		if(output.written()!=output.length()) {
			throw new ExtendedConversionException(ctx,
					"should write exactly "+output.length()+" bytes to output for this user defined type")
						.withSiteAndOrdinal(UserDefinedTypeConverter.class, 1);
		}
	}

	@Override
	public Object deserialize(MarkableInputStream is, FieldInfo ctx, Object self)
			throws IOException, ConversionException {
		Object ret = ctx.userDefinedConverter.deserialize(
				InputImpl.getThreadLocalInstance(ctx, is
						, Utils.lengthForDeserializingUserDefinedType(ctx, self, is)
						, Utils.charsetForDeserializingCHAR(ctx, self, is)
						));
		if(ret==null) {
			throw new ExtendedConversionException(ctx,
					"should return non-null value from custom TypeConverters")
						.withSiteAndOrdinal(UserDefinedTypeConverter.class, 2);
		}
		return ret;
	}

	private static class OutputImpl implements TypeConverter.Output {
		private FieldInfo fieldInfo;
		private OutputStream dest;
		private Charset fastCharset;
		private int written;
		private int fastLength;
		private static final ThreadLocal<OutputImpl> threadLocal = new ThreadLocal<OutputImpl>() {
			protected OutputImpl initialValue() {
				return new OutputImpl();
			};
		};
		
		private OutputImpl() {}
		
		private void reset(FieldInfo fieldInfo,OutputStream dest, int length, Charset cs) {
			this.fieldInfo = fieldInfo;
			this.dest = dest;
			this.written = 0;
			this.fastLength = length;
			this.fastCharset = cs;
		}
		private void checkBytesToWrite(int n) throws IOException {
			if(written+n>fastLength) {
				throw new UnsatisfiedIOException("attempting to write more than "+length()+" bytes")
						.withSiteAndOrdinal(Output.class, 1);
			}
			written += n;
		}
		
		public static OutputImpl getThreadLocalInstance(FieldInfo fieldInfo,OutputStream dest, int length, Charset cs) {
			OutputImpl ret = threadLocal.get();
			ret.reset(fieldInfo, dest, length, cs);
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
			return fastCharset;
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

	private static class InputImpl implements TypeConverter.Input{
		private FieldInfo fieldInfo;
		private InputStream src;
		private Charset fastCharset;
		private int read;
		private int fastLength;
		private static final ThreadLocal<InputImpl> threadLocal = new ThreadLocal<InputImpl>() {
			protected InputImpl initialValue() {
				return new InputImpl();
			};
		};
		
		private InputImpl() {}
		
		private void reset(FieldInfo fieldInfo,InputStream src, int length, Charset cs) {
			this.fieldInfo = fieldInfo;
			this.src = src;
			this.read = 0;
			this.fastLength = length;
			this.fastCharset = cs;
		}
		private void checkBytesToRead(int n) throws IOException {
			if(read+n>fastLength) {
				throw new UnsatisfiedIOException("attempting to read more than "+length()+" bytes")
						.withSiteAndOrdinal(Input.class, 1);
			}
			read += n;
		}
		public static InputImpl getThreadLocalInstance(FieldInfo fieldInfo,InputStream src, int length, Charset cs) {
			InputImpl ret = threadLocal.get();
			ret.reset(fieldInfo, src, length, cs);
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
			return fastCharset;
		}

		@Override
		public int length() {
			return fastLength;
		}

		@Override
		public byte readByte() throws IOException {
			checkBytesToRead(1);
			return (byte) StreamUtils.readByte(src, true);
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
}
