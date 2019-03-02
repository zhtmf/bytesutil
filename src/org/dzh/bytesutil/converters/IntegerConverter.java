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

public class IntegerConverter implements Converter<Integer> {

	@Override
	public void serialize(Integer value, OutputStream dest, FieldInfo ctx, Object self)
			throws IOException,ConversionException {
		int val = value;
		switch(ctx.dataType) {
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
			StreamUtils.writeInt(dest, val, ctx.bigEndian);
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
	public Integer deserialize(MarkableInputStream is, FieldInfo ctx, Object self)
			throws IOException, ConversionException {
		switch(ctx.dataType) {
		case BYTE:{
			return StreamUtils.readByte(is, ctx.signed);
		}
		case SHORT:{
			return StreamUtils.readShort(is, ctx.signed, ctx.bigEndian);
		}
		case INT:{
			return (int)StreamUtils.readInt(is, ctx.signed, ctx.bigEndian);
		}
		case CHAR:{
			return (int)Utils.deserializeAsCHAR(is, ctx, self, DataType.INT);
		}
		case BCD:{
			long val = StreamUtils.readIntegerBCD(is, ctx.localAnnotation(BCD.class).value());
			Utils.checkRangeInContext(DataType.INT, val, ctx);
			return (int) val;
		}
		default:throw new Error("cannot happen");
		}
	}
}
