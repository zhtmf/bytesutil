package org.dzh.bytesutil.converters;

import java.io.IOException;
import java.io.OutputStream;

import org.dzh.bytesutil.annotations.types.BCD;
import org.dzh.bytesutil.converters.auxiliary.FieldInfo;
import org.dzh.bytesutil.converters.auxiliary.MarkableStream;
import org.dzh.bytesutil.converters.auxiliary.StreamUtils;
import org.dzh.bytesutil.converters.auxiliary.Utils;

public class LongConverter implements Converter<Long> {

	@Override
	public void serialize(Long value, OutputStream dest, FieldInfo ctx, Object self)
			throws IOException,UnsupportedOperationException {
		long val = value==null ? 0 : value;
		switch(ctx.type) {
		case BYTE:{
			Utils.checkRange(val, Byte.class, ctx.unsigned);
			StreamUtils.writeBYTE(dest, (byte)val);
			return;
		}
		case SHORT:{
			Utils.checkRange(val, Short.class, ctx.unsigned);
			StreamUtils.writeSHORT(dest, (short) val, ctx.bigEndian);
			return;
		}
		case CHAR:
			if(val<0) {
				throw new IllegalArgumentException("negative number cannot be converted to CHAR");
			}
			String str = Long.toString(val);
			int length = Utils.lengthForSerializingCHAR(ctx, self);
			if(length<0) {
				length = str.length();
				StreamUtils.writeIntegerOfType(dest, ctx.lengthType, length, ctx.bigEndian);
			}else if(length!=str.length()) {
				throw new IllegalArgumentException(
						String.format("length of string representation [%s] of number [%d] not equals with declared CHAR length [%d]"
									,str, val,length));
			}
			StreamUtils.writeBytes(dest, str.getBytes());
			return;
		case INT:{
			Utils.checkRange(val, Integer.class, ctx.unsigned);
			StreamUtils.writeInt(dest, (int) val, ctx.bigEndian);
			return;
		}
		case BCD:
			StreamUtils.writeBCD(
					dest, Utils.checkAndConvertToBCD(val, ctx.localAnnotation(BCD.class).value()));
			return;
		default:
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public Long deserialize(MarkableStream is, FieldInfo ctx, Object self)
			throws IOException,UnsupportedOperationException {
		switch(ctx.type) {
		case BYTE:{
			int value = ctx.signed ? StreamUtils.readSignedByte(is) : StreamUtils.readUnsignedByte(is);
			return (long) value;
		}
		case SHORT:{
			int value = ctx.signed ? StreamUtils.readSignedShort(is, ctx.bigEndian) : StreamUtils.readUnsignedShort(is, ctx.bigEndian);
			return (long) value;
		}
		case INT:{
			long value = ctx.signed ? StreamUtils.readSignedInt(is, ctx.bigEndian) : StreamUtils.readUnsignedInt(is, ctx.bigEndian);
			return value;
		}
		case CHAR:{
			int length = Utils.lengthForDeserializingCHAR(ctx, self, is);
			if(length<0) {
				length = StreamUtils.readIntegerOfType(is, ctx.lengthType, ctx.bigEndian);
			}
			byte[] numChars = StreamUtils.readBytes(is, length);
			return Long.valueOf(new String(numChars));
		}
		case BCD:{
			long value = StreamUtils.readIntegerBCD(is, ctx.localAnnotation(BCD.class).value());
			return value;
		}
		default:
			throw new UnsupportedOperationException();
		}
	}
}
