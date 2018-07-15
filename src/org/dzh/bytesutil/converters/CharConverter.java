package org.dzh.bytesutil.converters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.dzh.bytesutil.DataType;
import org.dzh.bytesutil.annotations.types.CHAR;
import org.dzh.bytesutil.converters.auxiliary.Context;
import org.dzh.bytesutil.converters.auxiliary.StreamUtils;

public class CharConverter implements Converter<Character> {

	@Override
	public void serialize(Character value, DataType target, OutputStream dest, Context ctx, Object self)
			throws IOException,UnsupportedOperationException {
		value = value == null ? 0 : value;
		Charset cs = ctx.charset;
		if(cs==null) {
			cs = (Charset) ctx.charsetHandler.handleSerialize(ctx.name, self);
		}
		byte[] bytes = value.toString().getBytes(cs);
		switch(target) {
		case CHAR:{
			int length = ctx.annotation(CHAR.class).value();
			if(bytes.length!=length) {
				throw new IllegalArgumentException(
						String.format(
								"byte array encoded using declared charset is of different length %d"
										+ " with declared length %d of CHAR annotation"
										,bytes.length,length));
			}
			StreamUtils.writeBytes(dest, bytes);
			break;
		}
		default:
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public Character deserialize(DataType src, InputStream is, Context ctx, Object self)
			throws IOException,UnsupportedOperationException {
		Charset cs = ctx.charset;
		if(cs==null) {
			cs = (Charset) ctx.charsetHandler.handleSerialize(ctx.name, self);
		}
		switch(src) {
		case CHAR:{
			int length = ctx.annotation(CHAR.class).value();
			String str = new String(StreamUtils.readBytes(is, length),cs);
			if(str.length()!=1) {
				throw new IllegalArgumentException(
						String.format("length of decoded string [%s] using declared charset is not 1", str));
			}
			return str.charAt(0);
		}
		default:
			throw new UnsupportedOperationException();
		}
	}
}
