package org.dzh.bytesutil.converters;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Date;

import org.dzh.bytesutil.ConversionException;
import org.dzh.bytesutil.annotations.types.BCD;
import org.dzh.bytesutil.converters.auxiliary.FieldInfo;
import org.dzh.bytesutil.converters.auxiliary.MarkableInputStream;
import org.dzh.bytesutil.converters.auxiliary.StreamUtils;
import org.dzh.bytesutil.converters.auxiliary.Utils;
import org.dzh.bytesutil.converters.auxiliary.exceptions.ExtendedConversionException;

public class DateConverter implements Converter<Date>{
	
	@Override
	public void serialize(Date value, OutputStream dest, FieldInfo ctx, Object self)
			throws IOException, ConversionException {
		String str = Utils.getThreadLocalDateFormatter(ctx.datePattern).format(value);
		switch(ctx.type) {
		case CHAR:
			Utils.serializeAsCHAR(str, dest, ctx, self);
			break;
		case BCD:
			Utils.serializeBCD(str, dest, ctx, self);
			break;
		default:throw new Error("cannot happen");
		}
	}

	@Override
	public Date deserialize(MarkableInputStream is, FieldInfo ctx, Object self)
			throws IOException, ConversionException {
		try {
			switch(ctx.type) {
			case CHAR:{
				int length = Utils.lengthForDeserializingCHAR(ctx, self, is);
				if(length<0) {
					length = StreamUtils.readIntegerOfType(is, ctx.lengthType(), ctx.bigEndian);
				}
				return Utils.getThreadLocalDateFormatter(ctx.datePattern)
						.parse(new String(
								StreamUtils.readBytes(
										is, length)
								,StandardCharsets.ISO_8859_1));
			}
			case BCD:
					return Utils.getThreadLocalDateFormatter(ctx.datePattern)
							.parse(StreamUtils.readStringBCD(
									is,ctx.annotation(BCD.class).value()));
				
			default:throw new Error("cannot happen");
			}
		} catch (ParseException e) {
			throw new ExtendedConversionException(ctx,
					"parser error",e)
						.withSiteAndOrdinal(DateConverter.class, 2);
		}
	}
}
