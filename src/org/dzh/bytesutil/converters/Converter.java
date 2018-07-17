package org.dzh.bytesutil.converters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.dzh.bytesutil.converters.auxiliary.Context;
import org.dzh.bytesutil.converters.auxiliary.DataType;

public interface Converter<T> {
	void serialize(T value, DataType target, OutputStream dest
			, Context ctx, Object self) 
			throws IOException,UnsupportedOperationException;
	T deserialize(DataType src
			, InputStream is, Context ctx, Object self)
			throws IOException,UnsupportedOperationException;
}
