package org.dzh.bytesutil.converters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.dzh.bytesutil.annotations.types.BCD;
import org.dzh.bytesutil.converters.auxiliary.Context;
import org.dzh.bytesutil.converters.auxiliary.DataType;
import org.dzh.bytesutil.converters.auxiliary.StreamUtils;

/**
 * Converter for a single byte
 * 
 * @author dzh
 *
 */
public class ByteConverter implements Converter<Byte> {

	@Override
	public void serialize(Byte value, DataType target, OutputStream dest, Context ctx, Object self)
			throws IOException,UnsupportedOperationException {
		byte val = value==null ? 0 : (byte)value;
		switch(target) {
		case BYTE:
			if(ctx.unsigned && val<0) {
				throw new IllegalArgumentException(
					String.format("value [%d] cannot be stored as unsigned value", val));
			}
			StreamUtils.writeBYTE(dest, val);
			return;
		case BCD:
			if(val<0) {
				throw new IllegalArgumentException("BCD cannot be negative");
			}
			StreamUtils.writeBCD(dest, val, ctx.localAnnotation(BCD.class).value());
			return;
		default:
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public Byte deserialize(DataType src, InputStream is, Context ctx, Object self)
			throws IOException,UnsupportedOperationException {
		switch(src) {
		case BYTE:{
			int val = StreamUtils.readBYTE(is);
			if(ctx.unsigned && val>Byte.MAX_VALUE) {
				throw new IllegalArgumentException(
						String.format("value [%d] cannot be stored in Java byte", val));
			}
			return (byte)val;
		}
		case BCD:{
			int digits = ctx.localAnnotation(BCD.class).value();
			long val = StreamUtils.readIntegerBCD(is, digits);
			if(val>Byte.MAX_VALUE) {
				throw new IllegalArgumentException(
						String.format("value [%d] cannot be stored in Java byte", val));
			}
			return (byte)val;
		}
		default:
			throw new UnsupportedOperationException();
		}
	}
}
