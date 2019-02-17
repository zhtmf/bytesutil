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

public class LongConverter implements Converter<Long> {

	@Override
	public void serialize(Long value, OutputStream dest, FieldInfo ctx, Object self)
			throws IOException, ConversionException {
		long val = value;
		switch(ctx.type) {
		case BYTE:{
			Utils.checkRangeInContext(DataType.BYTE, val, ctx);
			StreamUtils.writeBYTE(dest, (byte)val);
			return;
		}
		case SHORT:{
			Utils.checkRangeInContext(DataType.SHORT, val, ctx);
			StreamUtils.writeSHORT(dest, (short) val, ctx.bigEndian);
			return;
		}
		case INT:{
			Utils.checkRangeInContext(DataType.INT, val, ctx);
			StreamUtils.writeInt(dest, (int) val, ctx.bigEndian);
			return;
		}
		case LONG:{
			StreamUtils.writeLong(dest, val, ctx.bigEndian);
			return;
		}
		case CHAR:
			Utils.serializeAsCHAR(val, dest, ctx, self);
			return;
		case BCD:
			StreamUtils.writeBCD(
					dest, Utils.checkAndConvertToBCD(val, ctx.localAnnotation(BCD.class).value()));
			return;
		default:throw new Error("cannot happen");
		}
	}

	@Override
	public Long deserialize(MarkableInputStream is, FieldInfo ctx, Object self)
			throws IOException, ConversionException {
		switch(ctx.type) {
		case BYTE:{
			return (long) (ctx.signed ? StreamUtils.readSignedByte(is) : StreamUtils.readUnsignedByte(is));
		}
		case SHORT:{
			return (long) (ctx.signed ? StreamUtils.readSignedShort(is, ctx.bigEndian) : StreamUtils.readUnsignedShort(is, ctx.bigEndian));
		}
		case INT:{
			return ctx.signed ? StreamUtils.readSignedInt(is, ctx.bigEndian) : StreamUtils.readUnsignedInt(is, ctx.bigEndian);
		}
		case LONG:{
			return StreamUtils.readLong(is, ctx.bigEndian);
		}
		case CHAR:{
			return Utils.deserializeAsCHAR(is, ctx, self, null);
		}
		case BCD:{
			return StreamUtils.readIntegerBCD(is, ctx.localAnnotation(BCD.class).value());
		}
		default:throw new Error("cannot happen");
		}
	}
}
