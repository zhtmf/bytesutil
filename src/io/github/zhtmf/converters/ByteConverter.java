package io.github.zhtmf.converters;

import java.io.IOException;
import java.io.OutputStream;

import io.github.zhtmf.ConversionException;
import io.github.zhtmf.annotations.types.BCD;
import io.github.zhtmf.converters.auxiliary.DataType;
import io.github.zhtmf.converters.auxiliary.FieldInfo;
import io.github.zhtmf.converters.auxiliary.MarkableInputStream;
import io.github.zhtmf.converters.auxiliary.StreamUtils;
import io.github.zhtmf.converters.auxiliary.Utils;

/**
 * Converter for a single byte
 * 
 * @author dzh
 *
 */
public class ByteConverter implements Converter<Byte> {

    @Override
    public void serialize(Byte value, OutputStream dest, FieldInfo ctx, Object self)
            throws IOException,ConversionException {
        byte val = (byte)value;
        switch(ctx.dataType) {
        case BYTE:
            StreamUtils.writeBYTE(dest, val);
            return;
        case CHAR:
            Utils.serializeAsCHAR(val, dest, ctx, self);
            return;
        case BCD:{
            StreamUtils.writeBCD(
                    dest, Utils.checkAndConvertToBCD(val, ctx.localAnnotation(BCD.class).value()));
            return;
        }
        default:throw new Error("cannot happen");
        }
    }

    @Override
    public Byte deserialize(MarkableInputStream is, FieldInfo ctx, Object self)
            throws IOException,ConversionException {
        switch(ctx.dataType) {
        case BYTE:{
            return (byte)(StreamUtils.readByte(is, ctx.signed));
        }
        case CHAR:{
            return (byte)Utils.deserializeAsCHAR(is, ctx, self, DataType.BYTE);
        }
        case BCD:{
            long val = StreamUtils.readIntegerBCD(is, ctx.localAnnotation(BCD.class).value());
            Utils.checkRangeInContext(DataType.BYTE, val, ctx);
            return (byte)val;
        }
        default:throw new Error("cannot happen");
        }
    }
}
