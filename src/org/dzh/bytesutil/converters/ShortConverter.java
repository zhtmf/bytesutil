package org.dzh.bytesutil.converters;

import java.io.IOException;
import java.io.OutputStream;

import org.dzh.bytesutil.ConversionException;
import org.dzh.bytesutil.annotations.types.BCD;
import org.dzh.bytesutil.converters.auxiliary.DataType;
import org.dzh.bytesutil.converters.auxiliary.FieldInfo;
import org.dzh.bytesutil.converters.auxiliary.MarkableInputStream;
import org.dzh.bytesutil.converters.auxiliary.StreamUtils;
import org.dzh.bytesutil.converters.auxiliary.Utils;

public class ShortConverter implements Converter<Short> {

    @Override
    public void serialize(Short value, OutputStream dest, FieldInfo ctx, Object self)
            throws IOException,ConversionException {
        short val = (short)value;
        switch(ctx.dataType) {
        case BYTE:{
            Utils.checkRangeInContext(DataType.BYTE, val, ctx);
            StreamUtils.writeBYTE(dest, (byte)val);
            return;
        }
        case SHORT:{
            StreamUtils.writeSHORT(dest, value, ctx.bigEndian);
            return;
        }
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
    public Short deserialize(MarkableInputStream is, FieldInfo ctx, Object self)
            throws IOException,ConversionException {
        switch(ctx.dataType) {
        case BYTE:{
            return (short)StreamUtils.readByte(is, ctx.signed);
        }
        case SHORT:{
            return (short)StreamUtils.readShort(is, ctx.signed, ctx.bigEndian);
        }
        case CHAR:{
            return (short)Utils.deserializeAsCHAR(is, ctx, self, DataType.SHORT);
        }
        case BCD:{
            long val = StreamUtils.readIntegerBCD(is, ctx.localAnnotation(BCD.class).value());
            Utils.checkRangeInContext(DataType.SHORT, val, ctx);
            return (short)val;
        }
        default:throw new Error("cannot happen");
        }
    }
}
