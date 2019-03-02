package org.dzh.bytesutil.converters;

import java.io.IOException;
import java.io.OutputStream;

import org.dzh.bytesutil.ConversionException;
import org.dzh.bytesutil.TypeConverter.Output;
import org.dzh.bytesutil.converters.auxiliary.FieldInfo;
import org.dzh.bytesutil.converters.auxiliary.MarkableInputStream;
import org.dzh.bytesutil.converters.auxiliary.Utils;
import org.dzh.bytesutil.converters.auxiliary.exceptions.ExtendedConversionException;

public class UserDefinedTypeConverter implements Converter<Object> {
	@SuppressWarnings("unchecked")
	@Override
	public void serialize(Object value, OutputStream dest, FieldInfo ctx, Object self)
			throws IOException, ConversionException {
		Output output = OutputImpl.getThreadLocalInstance(
				ctx, dest
				, Utils.lengthForSerializingUserDefinedType(ctx, self)
				, Utils.charsetForSerializingCHAR(ctx, self)
				);
		ctx.userDefinedConverter.serialize(value,output);
		if(output.written()!=output.length()) {
			throw new ExtendedConversionException(ctx,
					"should write exactly "+output.length()+" bytes to output for this user defined type")
						.withSiteAndOrdinal(UserDefinedTypeConverter.class, 1);
		}
	}

	@Override
	public Object deserialize(MarkableInputStream is, FieldInfo ctx, Object self)
			throws IOException, ConversionException {
		Object ret = ctx.userDefinedConverter.deserialize(
				InputImpl.getThreadLocalInstance(ctx, is
						, Utils.lengthForDeserializingUserDefinedType(ctx, self, is)
						, Utils.charsetForDeserializingCHAR(ctx, self, is)
						));
		if(ret==null) {
			throw new ExtendedConversionException(ctx,
					"should return non-null value from custom TypeConverters")
						.withSiteAndOrdinal(UserDefinedTypeConverter.class, 2);
		}
		return ret;
	}

}
