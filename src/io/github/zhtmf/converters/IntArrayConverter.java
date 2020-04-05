package io.github.zhtmf.converters;

import static io.github.zhtmf.converters.StreamUtils.checkRangeInContext;
import static io.github.zhtmf.converters.StreamUtils.writeIntegerOfType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.github.zhtmf.ConversionException;
import io.github.zhtmf.converters.auxiliary.DataType;

/**
 * Converters that handles serialization and deserialization of <tt>int[].class</tt>
 * 
 * @author dzh
 */
class IntArrayConverter implements Converter<int[]>{
    
    @Override
    public void serialize(int[] value, OutputStream dest, FieldInfo ctx, Object self)throws IOException, ConversionException {
        switch(ctx.dataType) {
        case RAW:
            int length = ctx.lengthForSerializingRAW(self);
            if(length<0) {
                writeIntegerOfType(dest, value.length, ctx);
            }else if(length!=value.length) {
                throw new ExtendedConversionException(
                        ctx.enclosingEntityClass,ctx.name,
                        "defined length "+length+" is not the same as length "+value.length+" of the array")
                            .withSiteAndOrdinal(IntArrayConverter.class, 1);
            }
            for(int i:value) {
                checkRangeInContext(DataType.BYTE, i, ctx);
            }
            StreamUtils.writeBytes(dest, value);
            break;
        default:throw new Error("should not reach here");
        }
    }

    @Override
    public int[] deserialize(InputStream in, FieldInfo ctx, Object self)throws IOException, ConversionException {
        switch(ctx.dataType) {
        case RAW:
            int length = ctx.lengthForDeserializingRAW(self, in);
            if(length<0) {
                length = StreamUtils.readIntegerOfType(in, ctx);
            }
            byte[] raw = StreamUtils.readBytes(in, length);
            int[] ret = new int[length];
            if(ctx.unsigned) {
                for(int i=0;i<raw.length;++i) {
                    ret[i] = ((int)raw[i]) & 0xFF;
                }
            }else {
                for(int i=0;i<raw.length;++i) {
                    ret[i] = raw[i];
                }
            }
            return ret;
        default:throw new Error("should not reach here");
        }    
    }
}