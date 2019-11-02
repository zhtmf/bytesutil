package io.github.zhtmf.converters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.github.zhtmf.ConversionException;

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
                throw new ExtendedConversionException(ctx.enclosingEntityClass,ctx.name,
                        "defined length "+length+" is not the same as length "+value.length+" of the array")
                            .withSiteAndOrdinal(ByteArrayConverter.class, 1);
            }
            //no checking here, negative values are interpreted as intended 
            //storing of unsigned values using signed Java types
            StreamUtils.writeBytes(dest, value);
            break;
        default:throw new Error("should not reach here");
        }
    }

    @Override
    public byte[] deserialize(InputStream in, FieldInfo ctx, Object self)
            throws IOException, ConversionException {
        switch(ctx.dataType) {
        case RAW:
            int length = ctx.lengthForDeserializingRAW(self, in);
            if(length<0) {
                length = StreamUtils.readIntegerOfType(in, ctx.lengthType(), ctx.bigEndian);
            }
            return StreamUtils.readBytes(in, length);
        default:throw new Error("should not reach here");
        }    
    }
}