package org.dzh.bytesutil.converters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.dzh.bytesutil.annotations.types.BCD;
import org.dzh.bytesutil.converters.auxiliary.FieldInfo;
import org.dzh.bytesutil.converters.auxiliary.StreamUtils;
import org.dzh.bytesutil.converters.auxiliary.Utils;

public class ShortConverter implements Converter<Short> {

	@Override
	public void serialize(Short value, OutputStream dest, FieldInfo ctx, Object self)
			throws IOException,UnsupportedOperationException {
		short val = value==null ? 0 : (short)value;
		switch(ctx.type) {
		case BYTE:{
			Utils.checkRange(val, Byte.class, ctx.unsigned);
			StreamUtils.writeBYTE(dest, (byte)val);
			return;
		}
		case SHORT:{
			Utils.checkRange(val, Short.class, ctx.unsigned);
			StreamUtils.writeSHORT(dest, val, ctx.bigEndian);
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
	public Short deserialize(InputStream is, FieldInfo ctx, Object self)
			throws IOException,UnsupportedOperationException {
		switch(ctx.type) {
		case BYTE:{
			int value = ctx.signed ? StreamUtils.readSignedByte(is) : StreamUtils.readUnsignedByte(is);
			return (short)value;
		}
		case SHORT:{
			int value = ctx.signed ? StreamUtils.readSignedShort(is, ctx.bigEndian) : StreamUtils.readUnsignedShort(is, ctx.bigEndian);
			Utils.checkRange(value, Short.class, false);
			return (short)value;
		}
		case BCD:{
			long value = StreamUtils.readIntegerBCD(is, ctx.localAnnotation(BCD.class).value());
			Utils.checkRange(value, Short.class, false);
			return (short)value;
		}
		default:
			throw new UnsupportedOperationException();
		}
	}
}
