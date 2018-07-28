package org.dzh.bytesutil.converters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.dzh.bytesutil.annotations.types.BCD;
import org.dzh.bytesutil.converters.auxiliary.FieldInfo;
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
			throws IOException,UnsupportedOperationException {
		byte val = value==null ? 0 : (byte)value;
		switch(ctx.type) {
		case BYTE:
			Utils.checkRange(val,Byte.class,ctx.unsigned);
			StreamUtils.writeBYTE(dest, val);
			return;
		case BCD:
			StreamUtils.writeBCD(
					dest, Utils.checkAndConvertToBCD(val, ctx.localAnnotation(BCD.class).value()));
			return;
		default:
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public Byte deserialize(InputStream is, FieldInfo ctx, Object self)
			throws IOException,UnsupportedOperationException {
		switch(ctx.type) {
		case BYTE:{
			int value = ctx.signed ? StreamUtils.readSignedByte(is) : StreamUtils.readUnsignedByte(is);
			Utils.checkRange(value,Byte.class,false);
			return (byte)value;
		}
		case BCD:{
			long val = StreamUtils.readIntegerBCD(is, ctx.localAnnotation(BCD.class).value());
			Utils.checkRange(val,Byte.class,false);
			return (byte)val;
		}
		default:
			throw new UnsupportedOperationException();
		}
	}
}
