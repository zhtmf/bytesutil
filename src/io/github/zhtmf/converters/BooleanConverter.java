package io.github.zhtmf.converters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.github.zhtmf.ConversionException;

/**
 * Converter for boolean value
 * 
 * @author dzh
 *
 */
class BooleanConverter implements Converter<Boolean> {

    @Override
    public void serialize(Boolean value, OutputStream dest, FieldInfo ctx, Object self)
            throws IOException,ConversionException {
        switch(ctx.dataType) {
        case BYTE:
            byte val = (byte) (value.booleanValue() == true ? 1 : 0);
            StreamUtils.writeBYTE(dest, val);
            return;
        default:throw new Error("should not reach here");
        }
    }

    @Override
    public Boolean deserialize(InputStream in, FieldInfo ctx, Object self)
            throws IOException,ConversionException {
        byte val;
        switch(ctx.dataType) {
        case BYTE:{
            val = (byte) StreamUtils.readByte(in, ctx.signed);break;
        }
        default:throw new Error("should not reach here");
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
