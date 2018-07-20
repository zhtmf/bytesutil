package org.dzh.bytesutil.converters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.dzh.bytesutil.annotations.types.BCD;
import org.dzh.bytesutil.converters.auxiliary.FieldInfo;
import org.dzh.bytesutil.converters.auxiliary.StreamUtils;

public class IntegerConverter implements Converter<Integer> {

	@Override
	public void serialize(Integer value, OutputStream dest, FieldInfo ctx, Object self)
			throws IOException,UnsupportedOperationException {
		int val = value==null ? 0 : (int)value;
		switch(ctx.type) {
		case BYTE:{
			boolean unsigned = ctx.unsigned;
			int min = unsigned ? 0 : Byte.MIN_VALUE;
			int max = unsigned ? 255 : Byte.MAX_VALUE;
			if (val > max || val < min) {
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
			StreamUtils.writeSHORT(dest, (short) val, ctx.bigEndian);
			return;
		}
		case INT:{
			boolean unsigned = ctx.unsigned;
			long min = unsigned ? 0 : Integer.MIN_VALUE;
			long max = unsigned ? ((long)Integer.MAX_VALUE)*2 : Integer.MAX_VALUE;
			long _val = (long)val;
			if(_val > max || _val < min) {
				throw new IllegalArgumentException(
						String.format("value [%d] cannot be stored in %s 4-byte value"
								,val,unsigned?"unsigned":"signed"));
			}
			StreamUtils.writeInt(dest, val, ctx.bigEndian);
			return;
		}
		case BCD:
			if(val<0) {
				throw new IllegalArgumentException("BCD cannot be negative");
			}
			StreamUtils.writeBCD(dest, val, ctx.localAnnotation(BCD.class).value()*2);
			return;
		default:
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public Integer deserialize(InputStream is, FieldInfo ctx, Object self)
			throws IOException,UnsupportedOperationException {
		switch(ctx.type) {
		case BYTE:{
			int value = StreamUtils.readBYTE(is);
			if(ctx.signed) {
				value = (int)(byte)value;
			}
			return value;
		}
		case SHORT:{
			int value = StreamUtils.readSHORT(is, ctx.bigEndian);
			if(ctx.signed) {
				value = (int)(short)value;
			}
			return value;
		}
		case INT:{
			long value = StreamUtils.readInt(is, ctx.bigEndian);
			if(ctx.unsigned && value>Integer.MAX_VALUE) {
				throw new IllegalArgumentException(
						String.format("value [%d] cannot be stored in Java int", value));
			}
			return (int)value;
		}
		case BCD:{
			int digits = ctx.localAnnotation(BCD.class).value();
			long value = StreamUtils.readIntegerBCD(is, digits);
			if(value>Integer.MAX_VALUE) {
				throw new IllegalArgumentException(
						String.format("value [%d] cannot be stored in Java int", value));
			}
			return (int) value;
		}
		default:
			throw new UnsupportedOperationException();
		}
	}
}
