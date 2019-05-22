package io.github.zhtmf.converters;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Date;

import io.github.zhtmf.ConversionException;
import io.github.zhtmf.annotations.types.BCD;
import io.github.zhtmf.converters.auxiliary.FieldInfo;
import io.github.zhtmf.converters.auxiliary.MarkableInputStream;
import io.github.zhtmf.converters.auxiliary.StreamUtils;
import io.github.zhtmf.converters.auxiliary.Utils;
import io.github.zhtmf.converters.auxiliary.exceptions.ExtendedConversionException;

public class DateConverter implements Converter<Date>{
    
    @Override
    public void serialize(Date value, OutputStream dest, FieldInfo ctx, Object self)
            throws IOException, ConversionException {
        switch(ctx.dataType) {
        case CHAR:
            Utils.serializeAsCHAR(Utils.getThreadLocalDateFormatter(ctx.datePattern).format(value), dest, ctx, self);
            break;
        case BCD:
            Utils.serializeBCD(Utils.getThreadLocalDateFormatter(ctx.datePattern).format(value), dest, ctx, self);
            break;
        case INT:{
            long millis = value.getTime();
            StreamUtils.writeInt(dest, (int)(millis/1000), ctx.bigEndian);
            break;
        }
        case LONG:{
            long millis = value.getTime();
            StreamUtils.writeLong(dest, millis, ctx.bigEndian);
            break;
        }
        default:throw new Error("cannot happen");
        }
    }

    @Override
    public Date deserialize(MarkableInputStream is, FieldInfo ctx, Object self)
            throws IOException, ConversionException {
        try {
            switch(ctx.dataType) {
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
            case INT:{
                long val = StreamUtils.readInt(is, ctx.signed, ctx.bigEndian);
                return new Date(val*1000);
            }
            case LONG:{
                return new Date(StreamUtils.readLong(is, ctx.bigEndian));
            }
            default:throw new Error("cannot happen");
            }
        } catch (ParseException e) {
            throw new ExtendedConversionException(ctx,
                    "parser error",e)
                        .withSiteAndOrdinal(DateConverter.class, 2);
        }
    }
}