package org.dzh.bytesutil.converters;

import java.io.IOException;
import java.io.OutputStream;

import org.dzh.bytesutil.ConversionException;
import org.dzh.bytesutil.TypeConverter.Output;
import org.dzh.bytesutil.converters.auxiliary.FieldInfo;
import org.dzh.bytesutil.converters.auxiliary.MarkableInputStream;
import org.dzh.bytesutil.converters.auxiliary.Utils;

public class UserDefinedTypeConverter implements Converter<Object> {
	@Override
	public void serialize(Object value, OutputStream dest, FieldInfo ctx, Object self)
			throws IOException, ConversionException {
		Output output = OutputImpl.getThreadLocalInstance(
				ctx, dest, Utils.lengthForSerializingUserDefinedType(ctx, self));
		ctx.userDefinedConverter.serialize(value,output);
		if(output.written()!=output.length()) {
			//detect underflow
			//TODO:
			throw new RuntimeException();
		}
	}

	@Override
	public Object deserialize(MarkableInputStream is, FieldInfo ctx, Object self)
			throws IOException, ConversionException {
		Object ret = ctx.userDefinedConverter.deserialize(
				InputImpl.getThreadLocalInstance(ctx, is, Utils.lengthForDeserializingUserDefinedType(ctx, self, is)));
		if(ret==null) {
			throw new RuntimeException();
		}
		return ret;
	}

}
