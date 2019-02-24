package org.dzh.bytesutil.converters;

import java.io.IOException;
import java.io.OutputStream;

import org.dzh.bytesutil.ConversionException;
import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.converters.auxiliary.FieldInfo;
import org.dzh.bytesutil.converters.auxiliary.MarkableInputStream;
import org.dzh.bytesutil.converters.auxiliary.exceptions.ExtendedConversionException;

/**
 * Wrapper class for eliminating branches in {@link DataPacket} when converting
 * DataPacket values
 * 
 * @author dzh
 */
public class DataPacketConverter implements Converter<DataPacket> {

	@Override
	public void serialize(DataPacket value, OutputStream dest, FieldInfo fi, Object self)
			throws IOException, ConversionException {
		((DataPacket)value).serialize(dest);
	}

	@Override
	public DataPacket deserialize(MarkableInputStream is, FieldInfo fi, Object self)
			throws IOException, ConversionException {
		DataPacket object = ((DataPacket)fi.get(self));
		if(object==null) {
			try {
				object = fi.entityCreator.handleDeserialize(fi.name, self, is);
			} catch (Exception e) {
				throw new ExtendedConversionException(
						self.getClass(),fi.name,
						String.format("field value is null and"
								+ " instance of the entity class [%s] cannot be created"
								, fi.name, fi.getFieldType()),e)
						.withSiteAndOrdinal(DataPacketConverter.class, 11);
			}
		}
		object.deserialize(is);
		return object;
	}
}
