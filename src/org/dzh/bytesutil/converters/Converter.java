package org.dzh.bytesutil.converters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.dzh.bytesutil.converters.auxiliary.FieldInfo;

public interface Converter<T> {
	void serialize(T value, OutputStream dest
			, FieldInfo fi, Object self) 
			throws IOException,UnsupportedOperationException;
	T deserialize(InputStream is, FieldInfo fi, Object self)
			throws IOException,UnsupportedOperationException;
}
