package io.github.zhtmf.converters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;

import io.github.zhtmf.ConversionException;

import static io.github.zhtmf.converters.StreamUtils.*;

class BigIntegerConverter implements Converter<BigInteger>{
    
    @Override
    public void serialize(BigInteger value, OutputStream dest, FieldInfo ctx, Object self)
            throws IOException, ConversionException {
        switch(ctx.dataType) {
        case LONG:
            String error = null;
            if((error = DataTypeOperations.LONG.checkRange(value, ctx))!=null) {
                throw new ExtendedConversionException(ctx.enclosingEntityClass, ctx.name, error)
                        .withSiteAndOrdinal(BigIntegerConverter.class, 1);
            }
            StreamUtils.writeLong(dest, value.longValue(), ctx.bigEndian);
            return;
        case CHAR:
            serializeAsCHAR(value, dest, ctx, self);
            return;
        default: throw new Error("should not reach here");
        }
    }

    @Override
    public BigInteger deserialize(InputStream in, FieldInfo ctx, Object self)
            throws IOException, ConversionException {
        switch(ctx.dataType) {
        case LONG:
            BigInteger ret = null;
            if(ctx.signed) {
                ret = BigInteger.valueOf(StreamUtils.readLong(in, ctx.bigEndian));
            }else {
                ret = StreamUtils.readUnsignedLong(in, ctx.bigEndian);
            }
            String error = null;
            if((error = DataTypeOperations.LONG.checkRange(ret, ctx))!=null) {
                throw new ExtendedConversionException(ctx.enclosingEntityClass, ctx.name, error)
                        .withSiteAndOrdinal(BigIntegerConverter.class, 2);
            }
            return ret;
        case CHAR:
            return deserializeBigIntegerAsCHAR(in, ctx, self, ctx.dataType);
        default: throw new Error("should not reach here");
        }
    }
}