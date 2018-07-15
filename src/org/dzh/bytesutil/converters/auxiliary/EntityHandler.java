package org.dzh.bytesutil.converters.auxiliary;

import java.io.IOException;
import java.io.InputStream;

import org.dzh.bytesutil.DataPacket;

public abstract class EntityHandler extends ModifierHandler<DataPacket> {

	@Override
	public DataPacket handleDeserialize0(String fieldName, Object entity, InputStream is) {
		try {
			return handle0(fieldName, entity, is);
		} catch (IOException e) {
			throw new Error(e);
		}
	}

	@Override
	public DataPacket handleSerialize0(String fieldName, Object entity) {
		throw new UnsupportedOperationException("should not be called during serializing");
	}
	
	public abstract DataPacket handle0(String fieldName, Object entity, InputStream is) throws IOException;

}
