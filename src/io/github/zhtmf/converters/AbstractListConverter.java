package io.github.zhtmf.converters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import io.github.zhtmf.ConversionException;

abstract class AbstractListConverter {

    @SuppressWarnings("rawtypes")
    protected int lengthForSerialize(List value, OutputStream dest, FieldInfo ctx, Object self) throws ConversionException {
        @SuppressWarnings("unchecked")
        List<Object> listValue = (List<Object>)value;
        //validity check is done in ClassInfo
        int length = ctx.lengthForList(self);
        if(length<0) {
            length = listValue.size();
            try {
                StreamUtils.writeIntegerOfType(dest, ctx.lengthType(), listValue.size(), ctx.bigEndian);
            } catch (IOException e) {
                throw new ExtendedConversionException(self.getClass(),ctx.name,e)
                    .withSiteAndOrdinal(AbstractListConverter.class, 1);
            }
        }
        //do not use the actual length of the list but the length obtained above
        if(length!=listValue.size()) {
            throw new ExtendedConversionException(
                    self.getClass(),ctx.name,
                    String.format(
                            "defined list length [%d] is not the same as length [%d] of list value"
                            ,length,listValue.size()))
                        .withSiteAndOrdinal(AbstractListConverter.class, 2);
        }
        return length;
    }
    
    protected int lengthForDeserialize(InputStream in, FieldInfo ctx, Object self) throws ConversionException {
        int length = ctx.lengthForDeserializingListLength(self, in);
        if(length<0) {
            length = ctx.lengthForDeserializingLength(self, in);
        }
        if(length<0) {
            try {
                length = StreamUtils.readIntegerOfType(in, ctx.lengthType(), ctx.bigEndian);
            } catch (IOException e) {
                throw new ExtendedConversionException(self.getClass(),ctx.name,e)
                        .withSiteAndOrdinal(AbstractListConverter.class, 12);
            }
        }
        return length;
    }
}
