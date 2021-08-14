package io.github.zhtmf.converters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.github.zhtmf.ConversionException;

class FloatConverter implements Converter<Float>{

    @Override
    public void serialize(Float value, OutputStream dest, FieldInfo ctx, Object self)
            throws IOException, ConversionException {
        switch(ctx.dataType) {
        case FLOAT:
        	StreamUtils.writeInt(dest, Float.floatToRawIntBits(value), ctx.bigEndian);
            break;
        default:throw new Error("should not reach here");
        }
    }

    @Override
    public Float deserialize(InputStream in, FieldInfo ctx, Object self) throws IOException, ConversionException {
        switch(ctx.dataType) {
        case FLOAT:
            return Float.intBitsToFloat((int) StreamUtils.readInt(in, true, ctx.bigEndian));
        default:throw new Error("should not reach here");
        }
    }
}
