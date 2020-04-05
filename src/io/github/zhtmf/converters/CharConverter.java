package io.github.zhtmf.converters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import io.github.zhtmf.ConversionException;

import static io.github.zhtmf.converters.StreamUtils.*;

class CharConverter implements Converter<Character> {

    @Override
    public void serialize(Character value, OutputStream dest, FieldInfo ctx, Object self)
            throws IOException, ConversionException {
        switch(ctx.dataType) {
        case CHAR:{
            serializeAsCHAR(value.toString(), dest, ctx, self);
            break;
        }
        default:throw new Error("should not reach here");
        }
    }

    @Override
    public Character deserialize(InputStream in, FieldInfo ctx, Object self)
            throws IOException, ConversionException {
        switch(ctx.dataType) {
        case CHAR:{
            Charset cs = ctx.charsetForDeserializingCHAR(self, in);
            int length = ctx.lengthForDeserializingCHAR(self, in);
            if(length<0) {
                length = StreamUtils.readIntegerOfType(in, ctx);
            }
            String str = new String(StreamUtils.readBytes(in, length),cs);
            if(str.length()!=1) {
                throw new ExtendedConversionException(ctx.enclosingEntityClass,ctx.name,
                        "length of decoded string ["+str+"] using declared charset ["+cs+"] is not 1")
                            .withSiteAndOrdinal(CharConverter.class, 1);
            }
            return str.charAt(0);
        }
        default:throw new Error("should not reach here");
        }
    }
}
