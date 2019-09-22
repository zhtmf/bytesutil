package io.github.zhtmf.converters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import io.github.zhtmf.ConversionException;
import io.github.zhtmf.converters.auxiliary.exceptions.ExtendedConversionException;

abstract class AbstractListConverter {

    @SuppressWarnings("rawtypes")
    protected int lengthForSerialize(List value, OutputStream dest, FieldInfo fi, Object self) throws ConversionException {
        @SuppressWarnings("unchecked")
        List<Object> listValue = (List<Object>)value;
        //validity check is done in ClassInfo
        int length = fi.lengthForList(self);
        if(length<0) {
            length = listValue.size();
            try {
                StreamUtils.writeIntegerOfType(dest, fi.lengthType(), listValue.size(), fi.bigEndian);
            } catch (IOException e) {
                throw new ExtendedConversionException(self.getClass(),fi.name,e)
                    .withSiteAndOrdinal(AbstractListConverter.class, 1);
            }
        }
        //do not use the actual length of the list but the length obtained above
        if(length!=listValue.size()) {
            throw new ExtendedConversionException(
                    self.getClass(),fi.name,
                    String.format(
                            "defined list length [%d] is not the same as length [%d] of list value"
                            ,length,listValue.size()))
                        .withSiteAndOrdinal(AbstractListConverter.class, 2);
        }
        return length;
    }
    
    protected int lengthForDeserialize(InputStream is, FieldInfo fi, Object self) throws ConversionException {
        int length = fi.lengthForDeserializingListLength(self, is);
        if(length<0) {
            length = fi.lengthForDeserializingLength(self, is);
        }
        if(length<0) {
            try {
                length = StreamUtils.readIntegerOfType(is, fi.lengthType(), fi.bigEndian);
            } catch (IOException e) {
                throw new ExtendedConversionException(self.getClass(),fi.name,e)
                        .withSiteAndOrdinal(AbstractListConverter.class, 12);
            }
        }
        return length;
    }
}