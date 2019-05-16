package org.dzh.bytesutil.converters.auxiliary;

import java.io.IOException;
import java.io.InputStream;

import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.Variant;

/**
 * Helper class which is called during runtime to initiate a field annotated
 * using {@link Variant}
 * <p>
 * It is a subclass of {@link ModifierHandler} while it is only called during
 * deserialization as field values are always supplied by user during
 * serialization.
 * 
 * @author dzh
 */
public abstract class EntityHandler extends ModifierHandler<DataPacket> {

    @Override
    public DataPacket handleDeserialize0(String fieldName, Object entity, InputStream is) throws IOException{
        return handle0(fieldName, entity, is);
    }

    @Override
    public DataPacket handleSerialize0(String fieldName, Object entity) {
        throw new UnsupportedOperationException("should not be called during serializing");
    }
    
    public abstract DataPacket handle0(String fieldName, Object entity, InputStream is) throws IOException;

}
