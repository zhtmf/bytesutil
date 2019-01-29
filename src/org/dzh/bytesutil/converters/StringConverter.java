package org.dzh.bytesutil.converters;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.dzh.bytesutil.annotations.types.BCD;
import org.dzh.bytesutil.converters.auxiliary.FieldInfo;
import org.dzh.bytesutil.converters.auxiliary.MarkableInputStream;
import org.dzh.bytesutil.converters.auxiliary.StreamUtils;
import org.dzh.bytesutil.converters.auxiliary.Utils;

public class StringConverter implements Converter<String> {

	@Override
	public void serialize(String value, OutputStream dest, FieldInfo ctx, Object self)
			throws IOException, UnsupportedOperationException {
		value = value == null ? "" : value;
		switch(ctx.type) {
		case CHAR:{
			Charset cs = Utils.charsetForSerializingCHAR(ctx, self);
			byte[] bytes = null;
			
			int length = Utils.lengthForSerializingCHAR(ctx, self);
			if(length<0) {
				bytes = value.getBytes(cs);
				//due to the pre-check in Context class, Length must be present at this point
				length = bytes.length;
				StreamUtils.writeIntegerOfType(dest, ctx.lengthType(), length, ctx.bigEndian);
			}else if(length!=(bytes = value.getBytes(cs)).length) {
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
	public String deserialize(MarkableInputStream is, FieldInfo ctx, Object self)
			throws IOException, UnsupportedOperationException {
		switch(ctx.type) {
		case CHAR:{
			Charset cs = Utils.charsetForDeserializingCHAR(ctx, self, is);
			int length = Utils.lengthForDeserializingCHAR(ctx, self, is);
			if(length<0) {
				length = StreamUtils.readIntegerOfType(is, ctx.lengthType(), ctx.bigEndian);
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
