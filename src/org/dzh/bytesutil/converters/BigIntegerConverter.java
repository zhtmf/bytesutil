package org.dzh.bytesutil.converters;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;

import org.dzh.bytesutil.ConversionException;
import org.dzh.bytesutil.converters.auxiliary.DataType;
import org.dzh.bytesutil.converters.auxiliary.FieldInfo;
import org.dzh.bytesutil.converters.auxiliary.MarkableInputStream;
import org.dzh.bytesutil.converters.auxiliary.StreamUtils;
import org.dzh.bytesutil.converters.auxiliary.Utils;
import org.dzh.bytesutil.converters.auxiliary.exceptions.ExtendedConversionException;

public class BigIntegerConverter implements Converter<BigInteger>{

    @Override
    public void serialize(BigInteger value, OutputStream dest, FieldInfo ctx, Object self)
            throws IOException, ConversionException {
        switch(ctx.dataType) {
        case LONG:
            String error = null;
            if((error = DataType.LONG.checkRange(value, ctx.unsigned))!=null) {
                throw new ExtendedConversionException(ctx.enclosingEntityClass, ctx.name, error)
                        .withSiteAndOrdinal(BigIntegerConverter.class, 1);
            }
            StreamUtils.writeLong(dest, value.longValue(), ctx.bigEndian);
            return;
        case CHAR:
            Utils.serializeAsCHAR(value, dest, ctx, self);
            return;
        default: throw new Error("cannot happen");
        }
    }

    @Override
    public BigInteger deserialize(MarkableInputStream is, FieldInfo ctx, Object self)
            throws IOException, ConversionException {
        switch(ctx.dataType) {
        case LONG:
            BigInteger ret = null;
            if(ctx.signed) {
                ret = BigInteger.valueOf(StreamUtils.readLong(is, ctx.bigEndian));
            }else {
                ret = StreamUtils.readUnsignedLong(is, ctx.bigEndian);
            }
            String error = null;
            if((error = DataType.LONG.checkRange(ret, ctx.unsigned))!=null) {
                throw new ExtendedConversionException(ctx.enclosingEntityClass, ctx.name, error)
                        .withSiteAndOrdinal(BigIntegerConverter.class, 2);
            }
            return ret;
        case CHAR:
            return Utils.deserializeAsBigCHAR(is, ctx, self, ctx.dataType);
        default: throw new Error("cannot happen");
        }
    }
}
