package org.dzh.bytesutil.converters;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.dzh.bytesutil.ConversionException;
import org.dzh.bytesutil.annotations.types.BCD;
import org.dzh.bytesutil.converters.auxiliary.FieldInfo;
import org.dzh.bytesutil.converters.auxiliary.MarkableInputStream;
import org.dzh.bytesutil.converters.auxiliary.StreamUtils;
import org.dzh.bytesutil.converters.auxiliary.Utils;

public class StringConverter implements Converter<String> {

	@Override
	public void serialize(String value, OutputStream dest, FieldInfo ctx, Object self)
			throws IOException, ConversionException {
		switch(ctx.type) {
		case CHAR:
			Utils.serializeAsCHAR(value, dest, ctx, self);
			break;
		case BCD:{
			Utils.serializeBCD(value, dest, ctx, self);
			break;
		}
		default:throw new Error("cannot happen");
		}
	}

	@Override
	public String deserialize(MarkableInputStream is, FieldInfo ctx, Object self)
			throws IOException, ConversionException {
		switch(ctx.type) {
		case CHAR:{
			Charset cs = Utils.charsetForDeserializingCHAR(ctx, self, is);
			int length = Utils.lengthForDeserializingCHAR(ctx, self, is);
			if(length<0) {
				length = StreamUtils.readIntegerOfType(is, ctx.lengthType(), ctx.bigEndian);
			}
			return new String(StreamUtils.readBytes(is, length),cs);
		}
		case BCD:
			return StreamUtils.readStringBCD(is, ctx.localAnnotation(BCD.class).value());
		default:throw new Error("cannot happen");
		}
	}
}
