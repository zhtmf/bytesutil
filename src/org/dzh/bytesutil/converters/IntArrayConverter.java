package org.dzh.bytesutil.converters;

import java.io.IOException;
import java.io.OutputStream;

import org.dzh.bytesutil.converters.auxiliary.FieldInfo;
import org.dzh.bytesutil.converters.auxiliary.MarkableStream;
import org.dzh.bytesutil.converters.auxiliary.StreamUtils;
import org.dzh.bytesutil.converters.auxiliary.Utils;

/**
 * Converters that handles serialization and deserialization of <tt>int[].class</tt>
 * 
 * @author dzh
 */
public class IntArrayConverter implements Converter<int[]>{
	
	@Override
	public void serialize(int[] value, OutputStream dest, FieldInfo ctx, Object self)
			throws IOException,UnsupportedOperationException {
		value = value == null ? new int[0] : value;
		switch(ctx.type) {
		case RAW:
			int length = Utils.lengthForSerializingRAW(ctx, self);
			if(length<0) {
				//due to the pre-check in Context class, Length must be present at this point
				length = value.length;
				StreamUtils.writeIntegerOfType(dest, ctx.lengthType(), length, ctx.bigEndian);
				
			}else if(length!=value.length) {
				throw new RuntimeException(
						String.format("field [%s] is defined as a int array,"
								+ " but the defined length [%d] is not the same as length [%d] of the list"
								, ctx.name,length,value.length));
			}
			StreamUtils.writeBytes(dest, value);
			break;
		default:
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public int[] deserialize(MarkableStream is, FieldInfo ctx, Object self)
			throws IOException,UnsupportedOperationException {
		switch(ctx.type) {
		case RAW:
			int length = Utils.lengthForDeserializingRAW(ctx, self, is);
			if(length<0) {
				length = StreamUtils.readIntegerOfType(is, ctx.lengthType(), ctx.bigEndian);
			}
			byte[] raw = StreamUtils.readBytes(is, length);
			int[] ret = new int[length];
			for(int i=0;i<raw.length;++i) {
				ret[i] = raw[i];
			}
			return ret;
		default:
			throw new UnsupportedOperationException();
		}	
	}

}
