package io.github.zhtmf.converters;

import java.io.IOException;
import java.io.OutputStream;

import io.github.zhtmf.ConversionException;
import io.github.zhtmf.converters.auxiliary.DataType;
import io.github.zhtmf.converters.auxiliary.exceptions.ExtendedConversionException;
import static io.github.zhtmf.converters.StreamUtils.*;

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
                writeIntegerOfType(dest, ctx.lengthType(), value.length, ctx.bigEndian);
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
        default:throw new Error("cannot happen");
        }
    }

    @Override
    public int[] deserialize(java.io.InputStream is, FieldInfo ctx, Object self)throws IOException, ConversionException {
        switch(ctx.dataType) {
        case RAW:
            int length = ctx.lengthForDeserializingRAW(self, is);
            if(length<0) {
                length = StreamUtils.readIntegerOfType(is, ctx.lengthType(), ctx.bigEndian);
            }
            byte[] raw = StreamUtils.readBytes(is, length);
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
        default:throw new Error("cannot happen");
        }    
    }
}