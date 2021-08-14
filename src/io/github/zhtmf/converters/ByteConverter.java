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
        switch(ctx.dataType) {
        case BYTE:
            StreamUtils.writeBYTE(dest, (byte)value);
            return;
        case CHAR:
            serializeAsCHAR((byte)value, dest, ctx, self);
            return;
        case BCD:{
            StreamUtils.writeBCD(
                    dest, checkAndConvertToBCD((byte)value, ctx.localAnnotation(BCD.class).value()));
            return;
        }
        case BIT:{
            StreamUtils.writeBit((BitOutputStream) dest, (byte)value, ctx.bitCount, ctx.bigEndian);
            return;
        }
        default:throw new Error("should not reach here");
        }
    }

    @Override
    public Byte deserialize(java.io.InputStream in, FieldInfo ctx, Object self)
            throws IOException,ConversionException {
        switch(ctx.dataType) {
        case BYTE:{
            return (byte)(StreamUtils.readByte(in, ctx.signed));
        }
        case CHAR:{
            return (byte)deserializeAsCHAR(in, ctx, self, DataType.BYTE);
        }
        case BCD:{
            long val = StreamUtils.readIntegerBCD(in, ctx);
            checkRangeInContext(DataType.BYTE, val, ctx);
            return (byte)val;
        }
        case BIT:{
            return StreamUtils.readBit((MarkableInputStream) in, ctx.bitCount, ctx.bigEndian);
        }
        default:throw new Error("should not reach here");
        }
    }
}
