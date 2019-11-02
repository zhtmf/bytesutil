package io.github.zhtmf.converters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.github.zhtmf.ConversionException;

interface Converter<T> {
    void serialize(T value, OutputStream dest
            , FieldInfo ctx, Object self) 
            throws IOException,ConversionException;
    T deserialize(InputStream in, FieldInfo ctx, Object self)
            throws IOException,ConversionException;
}
