package org.dzh.bytesutil;

import java.nio.charset.Charset;

public abstract class TypeConverter {
	
	public abstract byte[] serialize(Object obj,Context context);
	public abstract Object deserialize(byte[] data,Context context);
	
	public static interface Context{
		Class<?> getEntityClass();
		Class<?> getFieldClass();
		String getName();
		Charset getCharset();
		String getDatePattern();
		boolean isSigned();
		boolean isUnsigned();
		boolean isLittleEndian();
		boolean isBigEndian();
	}
}
