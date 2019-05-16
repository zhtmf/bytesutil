package org.dzh.bytesutil.converters;

import java.io.IOException;
import java.io.OutputStream;

import org.dzh.bytesutil.ConversionException;
import org.dzh.bytesutil.converters.auxiliary.FieldInfo;
import org.dzh.bytesutil.converters.auxiliary.MarkableInputStream;

public interface Converter<T> {
    void serialize(T value, OutputStream dest
            , FieldInfo ctx, Object self) 
            throws IOException,ConversionException;
    T deserialize(MarkableInputStream is, FieldInfo ctx, Object self)
            throws IOException,ConversionException;
}
