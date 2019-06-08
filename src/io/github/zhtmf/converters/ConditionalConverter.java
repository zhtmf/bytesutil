package io.github.zhtmf.converters;

import java.io.IOException;
import java.io.OutputStream;

import io.github.zhtmf.ConversionException;
import io.github.zhtmf.converters.auxiliary.FieldInfo;
import io.github.zhtmf.converters.auxiliary.MarkableInputStream;

public class ConditionalConverter implements Converter<Object>{
    
    private Converter<Object> wrappedConverter;
    
    public ConditionalConverter(Converter<Object> wrapped) {
        this.wrappedConverter = wrapped;
    }

    @Override
    public void serialize(Object value, OutputStream dest, FieldInfo ctx, Object self)
            throws IOException, ConversionException {
        if(ctx.conditionalHandler.handleSerialize(ctx.name, self).equals(Boolean.TRUE)) {
            wrappedConverter.serialize(value, dest, ctx, self);
        }
    }

    @Override
    public Object deserialize(MarkableInputStream is, FieldInfo ctx, Object self)
            throws IOException, ConversionException {
        if(ctx.conditionalHandler.handleDeserialize(ctx.name,self,is).equals(Boolean.TRUE)) {
            return wrappedConverter.deserialize(is, ctx, self);
        }
        return null;
    }

}
