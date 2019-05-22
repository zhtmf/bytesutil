package io.github.zhtmf.converters;

import java.io.IOException;
import java.io.OutputStream;

import io.github.zhtmf.ConversionException;
import io.github.zhtmf.converters.auxiliary.FieldInfo;
import io.github.zhtmf.converters.auxiliary.MarkableInputStream;

public interface Converter<T> {
    void serialize(T value, OutputStream dest
            , FieldInfo ctx, Object self) 
            throws IOException,ConversionException;
    T deserialize(MarkableInputStream is, FieldInfo ctx, Object self)
            throws IOException,ConversionException;
}
