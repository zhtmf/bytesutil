package org.dzh.bytesutil.converters;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.dzh.bytesutil.converters.auxiliary.FieldInfo;
import org.dzh.bytesutil.converters.auxiliary.MarkableInputStream;
import org.dzh.bytesutil.converters.auxiliary.StreamUtils;
import org.dzh.bytesutil.converters.auxiliary.Utils;

public class CharConverter implements Converter<Character> {

	@Override
	public void serialize(Character value, OutputStream dest, FieldInfo ctx, Object self)
			throws IOException,UnsupportedOperationException {
		value = value == null ? 0 : value;
		Charset cs = ctx.charset;
		if(cs==null) {
			cs = (Charset) ctx.charsetHandler.handleSerialize(ctx.name, self);
		}
		byte[] bytes = value.toString().getBytes(cs);
		switch(ctx.type) {
		case CHAR:{
			
			int length = Utils.lengthForSerializingCHAR(ctx, self);
			if(length<0) {
				//due to the pre-check in Context class, Length must be present at this point
				length = bytes.length;
				StreamUtils.writeIntegerOfType(dest, ctx.lengthType(), length, ctx.bigEndian);
				
			}else if(length!=bytes.length) {
				throw new IllegalArgumentException(
						String.format("encoded byte array length [%d] not equals with declared CHAR length [%d]"
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
	public Character deserialize(MarkableInputStream is, FieldInfo ctx, Object self)
			throws IOException,UnsupportedOperationException {
		Charset cs = ctx.charset;
		if(cs==null) {
			cs = (Charset) ctx.charsetHandler.handleSerialize(ctx.name, self);
		}
		switch(ctx.type) {
		case CHAR:{
			
			int length = Utils.lengthForDeserializingCHAR(ctx, self, is);
			if(length<0) {
				length = StreamUtils.readIntegerOfType(is, ctx.lengthType(), ctx.bigEndian);
			}
			
			String str = new String(StreamUtils.readBytes(is, length),cs);
			if(str.length()!=1) {
				throw new IllegalArgumentException(
						String.format("length of decoded string [%s] using declared charset [%s] is not 1", str, cs));
			}
			return str.charAt(0);
		}
		default:
			throw new UnsupportedOperationException();
		}
	}
}
