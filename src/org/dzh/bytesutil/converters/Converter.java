package org.dzh.bytesutil.converters;

import java.io.IOException;
import java.io.OutputStream;

import org.dzh.bytesutil.converters.auxiliary.FieldInfo;
import org.dzh.bytesutil.converters.auxiliary.MarkableInputStream;

public interface Converter<T> {
	void serialize(T value, OutputStream dest
			, FieldInfo fi, Object self) 
			throws IOException,UnsupportedOperationException;
	T deserialize(MarkableInputStream is, FieldInfo fi, Object self)
			throws IOException,UnsupportedOperationException;
}
