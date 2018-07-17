package org.dzh.bytesutil.converters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.dzh.bytesutil.converters.auxiliary.Context;
import org.dzh.bytesutil.converters.auxiliary.DataType;
import org.dzh.bytesutil.converters.auxiliary.StreamUtils;

public class LongConverter implements Converter<Long> {

	@Override
	public void serialize(Long value, DataType target, OutputStream dest, Context ctx, Object self)
			throws IOException,UnsupportedOperationException {
		long val = value==null ? 0 : value;
		switch(target) {
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
			if(val > max || val < min) {
				throw new IllegalArgumentException(
						String.format("value [%d] cannot be stored in %s 4-byte value"
								,val,unsigned?"unsigned":"signed"));
			}
			StreamUtils.writeInt(dest, (int) val, ctx.bigEndian);
			return;
		}
		default:
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public Long deserialize(DataType src, InputStream is, Context ctx, Object self)
			throws IOException,UnsupportedOperationException {
		switch(src) {
		case BYTE:{
			int value = StreamUtils.readBYTE(is);
			if(ctx.signed) {
				value = (int)(byte)value;
			}
			return (long) value;
		}
		case SHORT:{
			int value = StreamUtils.readSHORT(is, ctx.bigEndian);
			if(ctx.signed) {
				value = (int)(short)value;
			}
			return (long) value;
		}
		case INT:{
			return StreamUtils.readInt(is, ctx.bigEndian);
		}
		default:
			throw new UnsupportedOperationException();
		}
	}
}
