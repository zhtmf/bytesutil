package io.github.zhtmf.converters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.github.zhtmf.ConversionException;

public class DoubleConverter implements Converter<Double>{

    @Override
    public void serialize(Double value, OutputStream dest, FieldInfo ctx, Object self)
            throws IOException, ConversionException {
        switch(ctx.dataType) {
        case FIXED:
            byte[] data = StreamUtils.doubleToFixedPointBytes(value, ctx);
            if(ctx.littleEndian)
                StreamUtils.reverse(data);
            dest.write(data);
            break;
        default:throw new Error("should not reach here");
        }
    }

    @Override
    public Double deserialize(InputStream in, FieldInfo ctx, Object self) throws IOException, ConversionException {
        switch(ctx.dataType) {
        case FIXED:
            int[] lengths = ctx.fixedNumberLengths;
            int intLimit = lengths[0];
            int fractionLimit = lengths[1];
            byte[] src = new byte[intLimit + fractionLimit];
            in.read(src);
            if(ctx.littleEndian)
                StreamUtils.reverse(src);
            return StreamUtils.fixedPointBytesToDouble(src, ctx);
        default:throw new Error("should not reach here");
        }
    }
}
