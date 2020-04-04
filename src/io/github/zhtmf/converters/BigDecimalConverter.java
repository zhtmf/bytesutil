package io.github.zhtmf.converters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;

import io.github.zhtmf.ConversionException;

public class BigDecimalConverter implements Converter<BigDecimal>{

    @Override
    public void serialize(BigDecimal value, OutputStream dest, FieldInfo ctx, Object self)
            throws IOException, ConversionException {
        switch(ctx.dataType) {
        case FIXED:
            int[] lengths = ctx.fixedNumberLengths;
            byte[] data = StreamUtils.bigDecimalToFixedPointBytes(value, lengths[0], lengths[1], ctx.signed);
            if(!ctx.bigEndian)
                StreamUtils.reverse(data);
            dest.write(data);
            break;
        default:throw new Error("should not reach here");
        }
    }

    @Override
    public BigDecimal deserialize(InputStream in, FieldInfo ctx, Object self) throws IOException, ConversionException {
        switch(ctx.dataType) {
        case FIXED:
            int[] lengths = ctx.fixedNumberLengths;
            int intLimit = lengths[0];
            int fractionLimit = lengths[1];
            byte[] src = new byte[intLimit + fractionLimit];
            in.read(src);
            if(!ctx.bigEndian)
                StreamUtils.reverse(src);
            return StreamUtils.fixedPointBytesToBigdecimal(src, intLimit, fractionLimit, ctx.signed);
        default:throw new Error("should not reach here");
        }
    }

}
