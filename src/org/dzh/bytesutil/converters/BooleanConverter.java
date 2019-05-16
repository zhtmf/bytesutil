package org.dzh.bytesutil.converters;

import java.io.IOException;
import java.io.OutputStream;

import org.dzh.bytesutil.ConversionException;
import org.dzh.bytesutil.converters.auxiliary.FieldInfo;
import org.dzh.bytesutil.converters.auxiliary.MarkableInputStream;
import org.dzh.bytesutil.converters.auxiliary.StreamUtils;
import org.dzh.bytesutil.converters.auxiliary.exceptions.ExtendedConversionException;

/**
 * Converter for boolean value
 * 
 * @author dzh
 *
 */
public class BooleanConverter implements Converter<Boolean> {

    @Override
    public void serialize(Boolean value, OutputStream dest, FieldInfo ctx, Object self)
            throws IOException,ConversionException {
        byte val = (byte) (value.booleanValue() == true ? 1 : 0);
        switch(ctx.dataType) {
        case BYTE:
            StreamUtils.writeBYTE(dest, val);
            return;
        default:throw new Error("cannot happen");
        }
    }

    @Override
    public Boolean deserialize(MarkableInputStream is, FieldInfo ctx, Object self)
            throws IOException,ConversionException {
        byte val;
        switch(ctx.dataType) {
        case BYTE:{
            val = (byte) StreamUtils.readByte(is, ctx.signed);break;
        }
        default:throw new Error("cannot happen");
        }
        switch(val) {
        case 1:return Boolean.TRUE;
        case 0:return Boolean.FALSE;
        default: 
            throw new ExtendedConversionException(
                            ctx.enclosingEntityClass, ctx.name,
                            "only 0 or 1 can be reasonably deserialized as a boolean")
                            .withSiteAndOrdinal(BooleanConverter.class, 0);
        }
    }
}
