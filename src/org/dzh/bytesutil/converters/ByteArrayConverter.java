package org.dzh.bytesutil.converters;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.dzh.bytesutil.DataType;
import org.dzh.bytesutil.converters.auxiliary.Context;
import org.dzh.bytesutil.converters.auxiliary.StreamUtils;

/**
 * Converters that handles serialization and deserialization of <tt>byte[].class</tt>
 * 
 * @author dzh
 */
public class ByteArrayConverter implements Converter<byte[]>{
	
	@Override
	public void serialize(byte[] value, DataType target, OutputStream dest, Context ctx, Object self)
			throws IOException,UnsupportedOperationException {
		value = value == null ? new byte[0] : value;
		switch(target) {
		case RAW:
			int length = ctx.length;
			if(length<0) {
				if(ctx.lengthHandler!=null) {
					length = (Integer)ctx.lengthHandler.handleSerialize(ctx.name, self);
				}else {
					length = value.length;
					StreamUtils.writeIntegerOfType(dest, ctx.lengthType, length, ctx.bigEndian);
				}
			}
			if(length!=value.length) {
				throw new RuntimeException(
						String.format("field [%s] is defined as a byte array,"
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
	public byte[] deserialize(DataType src, InputStream is, Context ctx, Object self)
			throws IOException,UnsupportedOperationException {
		switch(src) {
		case RAW:
			int length = ctx.length;
			if(length<0) {
				if(ctx.lengthHandler!=null) {
					length = (Integer)ctx.lengthHandler.handleDeserialize(
							ctx.name, self, (BufferedInputStream) is);
				}else {
					length = StreamUtils.readIntegerOfType(is, ctx.lengthType, ctx.bigEndian);
				}
			}
			return StreamUtils.readBytes(is, length);
		default:
			throw new UnsupportedOperationException();
		}	
	}

}
