package org.dzh.bytesutil.converters;

import java.io.IOException;
import java.io.OutputStream;

import org.dzh.bytesutil.ConversionException;
import org.dzh.bytesutil.converters.auxiliary.DataType;
import org.dzh.bytesutil.converters.auxiliary.FieldInfo;
import org.dzh.bytesutil.converters.auxiliary.MarkableInputStream;
import org.dzh.bytesutil.converters.auxiliary.StreamUtils;
import org.dzh.bytesutil.converters.auxiliary.Utils;
import org.dzh.bytesutil.converters.auxiliary.exceptions.ExtendedConversionException;

/**
 * Converters that handles serialization and deserialization of <tt>int[].class</tt>
 * 
 * @author dzh
 */
public class IntArrayConverter implements Converter<int[]>{
	
	@Override
	public void serialize(int[] value, OutputStream dest, FieldInfo ctx, Object self)throws IOException, ConversionException {
		switch(ctx.dataType) {
		case RAW:
			int length = Utils.lengthForSerializingRAW(ctx, self);
			if(length<0) {
				StreamUtils.writeIntegerOfType(dest, ctx.lengthType(), value.length, ctx.bigEndian);
			}else if(length!=value.length) {
				throw new ExtendedConversionException(ctx,
						"defined length "+length+" is not the same as length "+value.length+" of the array")
							.withSiteAndOrdinal(IntArrayConverter.class, 1);
			}
			for(int i:value) {
				Utils.checkRangeInContext(DataType.BYTE, i, ctx);
			}
			StreamUtils.writeBytes(dest, value);
			break;
		default:throw new Error("cannot happen");
		}
	}

	@Override
	public int[] deserialize(MarkableInputStream is, FieldInfo ctx, Object self)throws IOException, ConversionException {
		switch(ctx.dataType) {
		case RAW:
			int length = Utils.lengthForDeserializingRAW(ctx, self, is);
			if(length<0) {
				length = StreamUtils.readIntegerOfType(is, ctx.lengthType(), ctx.bigEndian);
			}
			byte[] raw = StreamUtils.readBytes(is, length);
			int[] ret = new int[length];
			if(ctx.unsigned) {
				for(int i=0;i<raw.length;++i) {
					ret[i] = ((int)raw[i]) & 0xFF;
				}
			}else {
				for(int i=0;i<raw.length;++i) {
					ret[i] = raw[i];
				}
			}
			return ret;
		default:throw new Error("cannot happen");
		}	
	}
}