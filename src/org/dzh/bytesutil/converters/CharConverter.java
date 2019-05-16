package org.dzh.bytesutil.converters;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.dzh.bytesutil.ConversionException;
import org.dzh.bytesutil.converters.auxiliary.FieldInfo;
import org.dzh.bytesutil.converters.auxiliary.MarkableInputStream;
import org.dzh.bytesutil.converters.auxiliary.StreamUtils;
import org.dzh.bytesutil.converters.auxiliary.Utils;
import org.dzh.bytesutil.converters.auxiliary.exceptions.ExtendedConversionException;

public class CharConverter implements Converter<Character> {

    @Override
    public void serialize(Character value, OutputStream dest, FieldInfo ctx, Object self)
            throws IOException, ConversionException {
        switch(ctx.dataType) {
        case CHAR:{
            Utils.serializeAsCHAR(value.toString(), dest, ctx, self);
            break;
        }
        default:throw new Error("cannot happen");
        }
    }

    @Override
    public Character deserialize(MarkableInputStream is, FieldInfo ctx, Object self)
            throws IOException, ConversionException {
        switch(ctx.dataType) {
        case CHAR:{
            Charset cs = Utils.charsetForDeserializingCHAR(ctx, self, is);
            int length = Utils.lengthForDeserializingCHAR(ctx, self, is);
            if(length<0) {
                length = StreamUtils.readIntegerOfType(is, ctx.lengthType(), ctx.bigEndian);
            }
            String str = new String(StreamUtils.readBytes(is, length),cs);
            if(str.length()!=1) {
                throw new ExtendedConversionException(ctx,
                        "length of decoded string ["+str+"] using declared charset ["+cs+"] is not 1")
                            .withSiteAndOrdinal(CharConverter.class, 1);
            }
            return str.charAt(0);
        }
        default:throw new Error("cannot happen");
        }
    }
}
