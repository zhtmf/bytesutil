package org.dzh.bytesutil.converters;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.dzh.bytesutil.ConversionException;
import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.converters.auxiliary.AbstractListConverter;
import org.dzh.bytesutil.converters.auxiliary.FieldInfo;
import org.dzh.bytesutil.converters.auxiliary.MarkableInputStream;
import org.dzh.bytesutil.converters.auxiliary.exceptions.ExtendedConversionException;

public class DataPacketListConverter extends AbstractListConverter implements Converter<List<DataPacket>>{

	@Override
	public void serialize(List<DataPacket> value, OutputStream dest, FieldInfo fi, Object self)
			throws IOException, ConversionException {
		List<DataPacket> listValue = value;
		//validity check is done in ClassInfo
		int length = lengthForSerialize(listValue, dest, fi, self);
		for(int i=0;i<length;++i) {
			Object elem = listValue.get(i);
			if(elem==null) {
				throw new ExtendedConversionException(
						self.getClass(),fi.name,
						"list contains null value")
						.withSiteAndOrdinal(DataPacketListConverter.class, -2);
			}
			((DataPacket)elem).serialize(dest);
		}
	}

	@Override
	public List<DataPacket> deserialize(MarkableInputStream is, FieldInfo fi, Object self) throws IOException, ConversionException {
		int length = lengthForDeserialize(is, fi, self);
		List<DataPacket> tmp = null;
		try {
			tmp = new ArrayList<>(length);
			while(length-->0) {
				DataPacket object = (DataPacket) fi.entityCreator.handleDeserialize(fi.name, self, is);
				object.deserialize(is);
				tmp.add(object);
			}
		} catch (Exception e) {
			throw new ExtendedConversionException(
					self.getClass(),fi.name,
					String.format(
					"instance of component class [%s] cannot be created by calling no-arg constructor"
					, fi.listComponentClass),e)
				.withSiteAndOrdinal(DataPacketListConverter.class, 15);
		}
		return tmp;
	}
}