package org.dzh.bytesutil.converters;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.dzh.bytesutil.annotations.types.BCD;
import org.dzh.bytesutil.converters.auxiliary.FieldInfo;
import org.dzh.bytesutil.converters.auxiliary.StreamUtils;
import org.dzh.bytesutil.converters.auxiliary.Utils;

public class StringConverter implements Converter<String> {

	@Override
	public void serialize(String value, OutputStream dest, FieldInfo ctx, Object self)
			throws IOException, UnsupportedOperationException {
		value = value == null ? "" : value;
		switch(ctx.type) {
		case CHAR:{
			
			Charset cs = ctx.charset;
			if(cs==null) {
				cs = (Charset) ctx.charsetHandler.handleSerialize(ctx.name,self);
			}
			byte[] bytes = value.getBytes(cs);
			
			int length = Utils.lengthForSerializingCHAR(ctx, self);
			if(length<0) {
				//due to the pre-check in Context class, Length must be present at this point
				length = bytes.length;
				StreamUtils.writeIntegerOfType(dest, ctx.lengthType, length, ctx.bigEndian);
				
			}else if(length!=bytes.length) {
				throw new IllegalArgumentException(
						String.format("encoded byte array length [%d] not equals with declared CHAR length [%d]"
									,bytes.length,length));
			}
			
			StreamUtils.writeBytes(dest, bytes);
			break;
		}
		case BCD:{
			int length = ctx.localAnnotation(BCD.class).value();
			Utils.checkBCDLength(value, length);
			int[] values = new int[length*2];
			for(int i=0;i<values.length;++i) {
				char c = value.charAt(i);
				if(c<'0' || c>'9') {
					throw new IllegalArgumentException("only numeric characters are allowed in BCD");
				}
				values[i] = c-'0';
			}
			StreamUtils.writeBCD(dest, values);
			break;
		}
		default:
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public String deserialize(InputStream is, FieldInfo ctx, Object self)
			throws IOException, UnsupportedOperationException {
		switch(ctx.type) {
		case CHAR:{
			int length = Utils.lengthForDeserializingCHAR(ctx, self, (BufferedInputStream) is);
			if(length<0) {
				length = StreamUtils.readIntegerOfType(is, ctx.lengthType, ctx.bigEndian);
			}
			Charset cs = ctx.charset;
			if(cs==null) {
				cs = ctx.charsetHandler.handleDeserialize(
						ctx.name,self,(BufferedInputStream) is);
			}
			return new String(StreamUtils.readBytes(is, length),cs);
		}
		case BCD:{
			return StreamUtils.readStringBCD(is, ctx.localAnnotation(BCD.class).value());
		}
		default:
			throw new UnsupportedOperationException();
		}
	}
}
