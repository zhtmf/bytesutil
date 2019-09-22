package io.github.zhtmf.converters;

import java.io.IOException;
import java.io.OutputStream;

import io.github.zhtmf.ConversionException;
import io.github.zhtmf.annotations.types.BCD;
import io.github.zhtmf.converters.auxiliary.DataType;

import static io.github.zhtmf.converters.StreamUtils.*;

/**
 * Converter for a single byte
 * 
 * @author dzh
 *
 */
class ByteConverter implements Converter<Byte> {

    @Override
    public void serialize(Byte value, OutputStream dest, FieldInfo ctx, Object self)
            throws IOException,ConversionException {
        byte val = (byte)value;
        switch(ctx.dataType) {
        case BYTE:
            StreamUtils.writeBYTE(dest, val);
            return;
        case CHAR:
            serializeAsCHAR(val, dest, ctx, self);
            return;
        case BCD:{
            StreamUtils.writeBCD(
                    dest, checkAndConvertToBCD(val, ctx.localAnnotation(BCD.class).value()));
            return;
        }
        default:throw new Error("cannot happen");
        }
    }

    @Override
    public Byte deserialize(java.io.InputStream is, FieldInfo ctx, Object self)
            throws IOException,ConversionException {
        switch(ctx.dataType) {
        case BYTE:{
            return (byte)(StreamUtils.readByte(is, ctx.signed));
        }
        case CHAR:{
            return (byte)deserializeAsCHAR(is, ctx, self, DataType.BYTE);
        }
        case BCD:{
            long val = StreamUtils.readIntegerBCD(is, ctx.localAnnotation(BCD.class).value());
            checkRangeInContext(DataType.BYTE, val, ctx);
            return (byte)val;
        }
        default:throw new Error("cannot happen");
        }
    }
}
