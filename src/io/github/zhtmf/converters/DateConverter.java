package io.github.zhtmf.converters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Date;

import io.github.zhtmf.ConversionException;
import io.github.zhtmf.annotations.types.BCD;

import static io.github.zhtmf.converters.StreamUtils.*;

class DateConverter implements Converter<Date>{
    
    @Override
    public void serialize(Date value, OutputStream dest, FieldInfo ctx, Object self)
            throws IOException, ConversionException {
        switch(ctx.dataType) {
        case CHAR:
            serializeAsCHAR(FieldInfo.getThreadLocalDateFormatter(ctx.datePattern).format(value), dest, ctx, self);
            break;
        case BCD:
            serializeBCD(FieldInfo.getThreadLocalDateFormatter(ctx.datePattern).format(value), dest, ctx, self);
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
        default:throw new Error("should not reach here");
        }
    }

    @Override
    public Date deserialize(InputStream in, FieldInfo ctx, Object self)
            throws IOException, ConversionException {
        try {
            switch(ctx.dataType) {
            case CHAR:{
                int length = ctx.lengthForDeserializingCHAR(self, in);
                if(length<0) {
                    length = StreamUtils.readIntegerOfType(in, ctx.lengthType(), ctx.bigEndian);
                }
                return FieldInfo.getThreadLocalDateFormatter(ctx.datePattern)
                        .parse(new String(
                                StreamUtils.readBytes(
                                        in, length)
                                ,StandardCharsets.ISO_8859_1));
            }
            case BCD:
                    return FieldInfo.getThreadLocalDateFormatter(ctx.datePattern)
                            .parse(StreamUtils.readStringBCD(
                                    in,ctx.annotation(BCD.class).value()));
            case INT:{
                long val = StreamUtils.readInt(in, ctx.signed, ctx.bigEndian);
                return new Date(val*1000);
            }
            case LONG:{
                return new Date(StreamUtils.readLong(in, ctx.bigEndian));
            }
            default:throw new Error("should not reach here");
            }
        } catch (ParseException e) {
            throw new ExtendedConversionException(
                    ctx.enclosingEntityClass,ctx.name,
                    "parser error",e)
                        .withSiteAndOrdinal(DateConverter.class, 2);
        }
    }
}