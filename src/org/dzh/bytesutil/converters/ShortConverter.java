package org.dzh.bytesutil.converters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.dzh.bytesutil.annotations.types.BCD;
import org.dzh.bytesutil.converters.auxiliary.Context;
import org.dzh.bytesutil.converters.auxiliary.DataType;
import org.dzh.bytesutil.converters.auxiliary.StreamUtils;

public class ShortConverter implements Converter<Short> {

	@Override
	public void serialize(Short value, DataType target, OutputStream dest, Context ctx, Object self)
			throws IOException,UnsupportedOperationException {
		short val = value==null ? 0 : (short)value;
		switch(target) {
		case BYTE:{
			boolean unsigned = ctx.unsigned;
			short min = unsigned ? 0 : Byte.MIN_VALUE;
			short max = unsigned ? (short)255 : Byte.MAX_VALUE;
			if(val > max || val<min) {
				throw new IllegalArgumentException(
						String.format("value [%d] cannot be stored in %s single byte"
								,val,unsigned?"unsigned":"signed"));
			}
			StreamUtils.writeBYTE(dest, (byte)val);
			return;
		}
		case SHORT:{
			boolean unsigned = ctx.unsigned;
			int min = unsigned ? 0 : Short.MIN_VALUE;
			int max = unsigned ? Character.MAX_VALUE : Short.MAX_VALUE;
			if(val > max || val < min) {
				throw new IllegalArgumentException(
						String.format("value [%d] cannot be stored in %s 2-byte value"
								,val,unsigned?"unsigned":"signed"));
			}
			StreamUtils.writeSHORT(dest, val, ctx.bigEndian);
			return;
		}
		case BCD:
			if(val<0) {
				throw new IllegalArgumentException("BCD cannot be negative");
			}
			int digits = ctx.localAnnotation(BCD.class).value();
			StreamUtils.writeBCD(dest, val, digits);
			return;
		default:
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public Short deserialize(DataType src, InputStream is, Context ctx, Object self)
			throws IOException,UnsupportedOperationException {
		switch(src) {
		case BYTE:{
			int value = StreamUtils.readBYTE(is);
			return (short)value;
		}
		case SHORT:{
			int value = StreamUtils.readSHORT(is, ctx.bigEndian);
			if(ctx.unsigned && value > Short.MAX_VALUE) {
				throw new IllegalArgumentException(
						String.format("value [%d] cannot be stored in Java short", value));
			}
			return (short)value;
		}
		case BCD:{
			int digits = ctx.localAnnotation(BCD.class).value();
			long value = StreamUtils.readIntegerBCD(is, digits);
			if(value>Short.MAX_VALUE) {
				throw new IllegalArgumentException(
						String.format("value [%d] cannot be stored in Java short", value));
			}
			return (short)value;
		}
		default:
			throw new UnsupportedOperationException();
		}
	}
}
