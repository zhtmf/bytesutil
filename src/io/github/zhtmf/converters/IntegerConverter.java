package io.github.zhtmf.converters;

import java.io.IOException;
import java.io.OutputStream;

import io.github.zhtmf.ConversionException;
import io.github.zhtmf.annotations.types.BCD;
import io.github.zhtmf.converters.auxiliary.DataType;

import static io.github.zhtmf.converters.StreamUtils.*;

class IntegerConverter implements Converter<Integer> {

    @Override
    public void serialize(Integer value, OutputStream dest, FieldInfo ctx, Object self)
            throws IOException,ConversionException {
        int val = value;
        switch(ctx.dataType) {
        case BYTE:{
            checkRangeInContext(DataType.BYTE, val, ctx);
            StreamUtils.writeBYTE(dest, (byte)val);
            return;
        }
        case SHORT:{
            checkRangeInContext(DataType.SHORT, val, ctx);
            StreamUtils.writeSHORT(dest, (short) val, ctx.bigEndian);
            return;
        }
        case INT:{
            writeInt(dest, val, ctx.bigEndian);
            return;
        }
        case INT3:{
            checkRangeInContext(DataType.INT3, val, ctx);
            StreamUtils.writeInt3(dest, val, ctx.bigEndian);
            return;
        }
        case CHAR:
            serializeAsCHAR(val, dest, ctx, self);
            return;
        case BCD:
            writeBCD(
                    dest, checkAndConvertToBCD(val, ctx.localAnnotation(BCD.class).value()));
            return;
        default:throw new Error("cannot happen");
        }
    }

    @Override
    public Integer deserialize(java.io.InputStream is, FieldInfo ctx, Object self)
            throws IOException, ConversionException {
        switch(ctx.dataType) {
        case BYTE:{
            return StreamUtils.readByte(is, ctx.signed);
        }
        case SHORT:{
            return StreamUtils.readShort(is, ctx.signed, ctx.bigEndian);
        }
        case INT:{
            return (int)StreamUtils.readInt(is, ctx.signed, ctx.bigEndian);
        }
        case INT3:{
            return StreamUtils.readInt3(is, ctx.signed, ctx.bigEndian);
        }
        case CHAR:{
            return (int)deserializeAsCHAR(is, ctx, self, DataType.INT);
        }
        case BCD:{
            long val = StreamUtils.readIntegerBCD(is, ctx.localAnnotation(BCD.class).value());
            checkRangeInContext(DataType.INT, val, ctx);
            return (int) val;
        }
        default:throw new Error("cannot happen");
        }
    }
}
