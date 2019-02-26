package org.dzh.bytesutil.converters;

import java.io.IOException;
import java.io.OutputStream;

import org.dzh.bytesutil.ConversionException;
import org.dzh.bytesutil.converters.auxiliary.FieldInfo;
import org.dzh.bytesutil.converters.auxiliary.MarkableInputStream;
import org.dzh.bytesutil.converters.auxiliary.StreamUtils;
import org.dzh.bytesutil.converters.auxiliary.Utils;

public class UserDefinedTypeConverter implements Converter<Object> {
	@Override
	public void serialize(Object value, OutputStream dest, FieldInfo ctx, Object self)
			throws IOException, ConversionException {
		byte[] bytes = ctx.userDefinedConverter.serialize(value,ctx.context);
		int length = Utils.lengthForSerializingUserDefinedType(ctx, self);
		if(bytes.length != length) {
			throw new RuntimeException();
		}
		StreamUtils.writeBytes(dest, bytes);
	}

	@Override
	public Object deserialize(MarkableInputStream is, FieldInfo ctx, Object self)
			throws IOException, ConversionException {
		Object ret = ctx.userDefinedConverter.deserialize(
				StreamUtils.readBytes(is, Utils.lengthForSerializingUserDefinedType(ctx, self)),ctx.context);
		if(ret==null) {
			throw new RuntimeException();
		}
		return ret;
	}

}
