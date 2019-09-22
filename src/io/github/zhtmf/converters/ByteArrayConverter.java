package io.github.zhtmf.converters;

import java.io.IOException;
import java.io.OutputStream;

import io.github.zhtmf.ConversionException;
import io.github.zhtmf.converters.auxiliary.exceptions.ExtendedConversionException;

/**
 * Converters that handles serialization and deserialization of <tt>byte[].class</tt>
 * 
 * @author dzh
 */
class ByteArrayConverter implements Converter<byte[]>{
    
    @Override
    public void serialize(byte[] value, OutputStream dest, FieldInfo ctx, Object self)throws IOException, ConversionException {
        switch(ctx.dataType) {
        case RAW:
            int length = ctx.lengthForSerializingRAW(self);
            if(length<0) {
                StreamUtils.writeIntegerOfType(dest, ctx.lengthType(), value.length, ctx.bigEndian);
            }else if(length!=value.length) {
                throw new ExtendedConversionException(ctx,
                        "defined length "+length+" is not the same as length "+value.length+" of the array")
                            .withSiteAndOrdinal(ByteArrayConverter.class, 1);
            }
            //no checking here, negative values are interpreted as intended 
            //storing of unsigned values using signed Java types
            StreamUtils.writeBytes(dest, value);
            break;
        default:throw new Error("cannot happen");
        }
    }

    @Override
    public byte[] deserialize(java.io.InputStream is, FieldInfo ctx, Object self)
            throws IOException, ConversionException {
        switch(ctx.dataType) {
        case RAW:
            int length = ctx.lengthForDeserializingRAW(self, is);
            if(length<0) {
                length = StreamUtils.readIntegerOfType(is, ctx.lengthType(), ctx.bigEndian);
            }
            return StreamUtils.readBytes(is, length);
        default:throw new Error("cannot happen");
        }    
    }
}