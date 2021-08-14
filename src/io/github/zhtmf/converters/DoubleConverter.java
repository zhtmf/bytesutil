package io.github.zhtmf.converters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.github.zhtmf.ConversionException;

class DoubleConverter implements Converter<Double>{

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
        case DOUBLE:
        	StreamUtils.writeLong(dest, Double.doubleToRawLongBits(value), ctx.bigEndian);
            break;
        case FLOAT:
        	double val = value.doubleValue();
        	if(Double.compare(val, Float.MAX_VALUE) > 0 || Double.compare(val, -Float.MAX_VALUE) < 0 || (val > 0 && Double.compare(val, Float.MIN_VALUE) < 0))
        		throw new ExtendedConversionException(
        				ctx.enclosingEntityClass, ctx.name, "double value " + val +" exceeds range of a float")
                .			withSiteAndOrdinal(DoubleConverter.class, 1); 
        	StreamUtils.writeInt(dest, Float.floatToRawIntBits(value.floatValue()), ctx.bigEndian);
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
        case DOUBLE:
            return Double.longBitsToDouble(StreamUtils.readLong(in, ctx.bigEndian));
        case FLOAT:
        	return (double) Float.intBitsToFloat((int) StreamUtils.readInt(in, true, ctx.bigEndian));
        default:throw new Error("should not reach here");
        }
    }
}
