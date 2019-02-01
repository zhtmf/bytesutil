package org.dzh.bytesutil.converters;

import java.io.IOException;
import java.io.OutputStream;

import org.dzh.bytesutil.ConversionException;
import org.dzh.bytesutil.annotations.types.BCD;
import org.dzh.bytesutil.converters.auxiliary.DataType;
import org.dzh.bytesutil.converters.auxiliary.FieldInfo;
import org.dzh.bytesutil.converters.auxiliary.MarkableInputStream;
import org.dzh.bytesutil.converters.auxiliary.StreamUtils;
import org.dzh.bytesutil.converters.auxiliary.Utils;
import org.dzh.bytesutil.converters.auxiliary.exceptions.ExtendedConversionException;

/**
 * Converter for a single byte
 * 
 * @author dzh
 *
 */
public class ByteConverter implements Converter<Byte> {

	@Override
	public void serialize(Byte value, OutputStream dest, FieldInfo ctx, Object self)
			throws IOException,ConversionException {
		byte val = (byte)value;
		switch(ctx.type) {
		case BYTE:
			Utils.checkRangeInContext(DataType.BYTE, val, ctx);
			StreamUtils.writeBYTE(dest, val);
			return;
		case CHAR:
			if(val<0) {
				//implementation choice
				throw new ExtendedConversionException(ctx,"negative number cannot be converted to CHAR")
							.withSiteAndOrdinal(ByteConverter.class, 0);
			}
			String str = Long.toString(val);
			int length = Utils.lengthForSerializingCHAR(ctx, self);
			if(length<0) {
				length = str.length();
				StreamUtils.writeIntegerOfType(dest, ctx.lengthType(), length, ctx.bigEndian);
			}else if(length!=str.length()) {
				throw new ExtendedConversionException(ctx,
						String.format("length of string representation [%s] of number [%d] not equals with declared CHAR length [%d]"
									,str, val,length))
							.withSiteAndOrdinal(ByteConverter.class, 1);
			}
			StreamUtils.writeBytes(dest, str.getBytes());
			return;
		case BCD:{
			StreamUtils.writeBCD(
					dest, Utils.checkAndConvertToBCD(val, ctx.localAnnotation(BCD.class).value()));
			return;
		}
		default:throw new Error("cannot happen");
		}
	}

	@Override
	public Byte deserialize(MarkableInputStream is, FieldInfo ctx, Object self)
			throws IOException,ConversionException {
		switch(ctx.type) {
		case BYTE:{
			return (byte)(ctx.signed ? StreamUtils.readSignedByte(is) : StreamUtils.readUnsignedByte(is));
		}
		case CHAR:{
			int length = Utils.lengthForDeserializingCHAR(ctx, self, is);
			if(length<0) {
				length = StreamUtils.readIntegerOfType(is, ctx.lengthType(), ctx.bigEndian);
			}
			int ret;
			try {
				ret = Utils.numericCharsToNumber(StreamUtils.readBytes(is, length));
			} catch (IllegalArgumentException e) {
				throw new ExtendedConversionException(ctx, e.getMessage())
						.withSiteAndOrdinal(ByteConverter.class, 2);
			}
			Utils.checkRangeInContext(DataType.BYTE, ret, ctx);
			return (byte)ret;
		}
		case BCD:{
			long val = StreamUtils.readIntegerBCD(is, ctx.localAnnotation(BCD.class).value());
			Utils.checkRangeInContext(DataType.BYTE, val, ctx);
			return (byte)val;
		}
		default:throw new Error("cannot happen");
		}
	}
}
