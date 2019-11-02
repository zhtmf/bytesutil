package io.github.zhtmf.converters;

import java.io.IOException;
import java.io.OutputStream;

import io.github.zhtmf.ConversionException;
import io.github.zhtmf.annotations.types.BCD;
import io.github.zhtmf.converters.auxiliary.DataType;

import static io.github.zhtmf.converters.StreamUtils.*;

class ShortConverter implements Converter<Short> {

    @Override
    public void serialize(Short value, OutputStream dest, FieldInfo ctx, Object self)
            throws IOException,ConversionException {
        short val = (short)value;
        switch(ctx.dataType) {
        case BYTE:{
            checkRangeInContext(DataType.BYTE, val, ctx);
            writeBYTE(dest, (byte)val);
            return;
        }
        case SHORT:{
            writeSHORT(dest, value, ctx.bigEndian);
            return;
        }
        case CHAR:
            serializeAsCHAR(val, dest, ctx, self);
            return;
        case BCD:{
            writeBCD(
                    dest, checkAndConvertToBCD(val, ctx.localAnnotation(BCD.class).value()));
            return;
        }
        default:throw new Error("should not reach here");
        }
    }

    @Override
    public Short deserialize(java.io.InputStream in, FieldInfo ctx, Object self)
            throws IOException,ConversionException {
        switch(ctx.dataType) {
        case BYTE:{
            return (short)readByte(in, ctx.signed);
        }
        case SHORT:{
            return (short)readShort(in, ctx.signed, ctx.bigEndian);
        }
        case CHAR:{
            return (short)deserializeAsCHAR(in, ctx, self, DataType.SHORT);
        }
        case BCD:{
            long val = readIntegerBCD(in, ctx.localAnnotation(BCD.class).value());
            checkRangeInContext(DataType.SHORT, val, ctx);
            return (short)val;
        }
        default:throw new Error("should not reach here");
        }
    }
}
