package org.dzh.bytesutil.converters;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.dzh.bytesutil.ConversionException;
import org.dzh.bytesutil.converters.auxiliary.AbstractListConverter;
import org.dzh.bytesutil.converters.auxiliary.FieldInfo;
import org.dzh.bytesutil.converters.auxiliary.MarkableInputStream;
import org.dzh.bytesutil.converters.auxiliary.exceptions.ExtendedConversionException;

/**
 * Wrapper class for eliminating branches in {@link org.dzh.bytesutil.DataPacket} when converting
 * List values
 * 
 * @author dzh
 */
@SuppressWarnings("rawtypes")
public class ListConverter extends AbstractListConverter implements Converter<List> {

    @Override
    public void serialize(List value, OutputStream dest, FieldInfo fi, Object self)
            throws IOException, ConversionException {
        @SuppressWarnings("unchecked")
        List<Object> listValue = (List<Object>)value;
        //validity check is done in ClassInfo
        int length = lengthForSerialize(listValue, dest, fi, self);
        
        try {
            for(int i=0;i<length;++i) {
                Object elem = listValue.get(i);
                if(elem==null) {
                    throw new ExtendedConversionException(
                            self.getClass(),fi.name,
                            "list contains null value")
                            .withSiteAndOrdinal(ListConverter.class, -1);
                }
                @SuppressWarnings("unchecked")
                Converter<Object> cv = (Converter<Object>)fi.innerConverter;
                cv.serialize(elem, dest, fi, self);
            }
        } catch(ConversionException e) {
            throw e;
        } catch (Exception e) {
            throw new ExtendedConversionException(self.getClass(),fi.name,e)
                            .withSiteAndOrdinal(ListConverter.class, 4);
        }
    }

    @Override
    public List deserialize(MarkableInputStream is, FieldInfo fi, Object self) throws IOException, ConversionException {
        int length = lengthForDeserialize(is, fi, self);
        List<Object> tmp = null;
        //cv cannot be null as we lifted checking for validity of DataType<>JavaType mapping
        //to constructor of FieldInfo
        @SuppressWarnings("unchecked")
        Converter<Object> cv = (Converter<Object>)fi.innerConverter;
        try {
            tmp = new ArrayList<>(length);
            while(length-->0) {
                tmp.add(cv.deserialize(is, fi, self));
            }
        } catch(ConversionException e) {
            throw e;
        } catch (Exception e) {
            throw new ExtendedConversionException(self.getClass(),fi.name,e)
                    .withSiteAndOrdinal(ListConverter.class, 14);
        }
        return tmp;
    }
}