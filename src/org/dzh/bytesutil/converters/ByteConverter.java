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
			StreamUtils.writeBYTE(dest, val);
			return;
		case CHAR:
			Utils.serializeAsCHAR(val, dest, ctx, self);
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
			return (byte)Utils.deserializeAsCHAR(is, ctx, self, DataType.BYTE);
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
