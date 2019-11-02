package io.github.zhtmf.converters.auxiliary;

import java.io.IOException;
import java.io.InputStream;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Variant;

/**
 * Helper class whose instances are created during parsing and called during
 * runtime to initiate a field annotated with {@link Variant}
 * <p>
 * It is a subclass of {@link ModifierHandler} but it is only called during
 * deserialization as field values are always supplied by user before
 * serialization.
 * 
 * @author dzh
 */
public abstract class EntityHandler extends ModifierHandler<DataPacket> {

    @Override
    public DataPacket handleDeserialize0(String fieldName, Object entity, InputStream in) throws IOException{
        return handle0(fieldName, entity, in);
    }

    @Override
    public DataPacket handleSerialize0(String fieldName, Object entity) {
        throw new UnsupportedOperationException("should not be called during serializing");
    }
    
    public abstract DataPacket handle0(String fieldName, Object entity, InputStream in) throws IOException;

}
