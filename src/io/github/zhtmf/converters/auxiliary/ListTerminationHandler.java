package io.github.zhtmf.converters.auxiliary;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import io.github.zhtmf.annotations.modifiers.ListEndsWith;

/**
 * The dedicated <tt>ModifierHandler</tt> subclass used by {@link ListEndsWith}.
 * <p>
 * Users should only implement the overloaded
 * {@link ListTerminationHandler#handleDeserialize0(String, Object, java.io.InputStream, java.util.List)
 * handleDeserialize0} method to implement custom logic.
 * 
 * @author dzh
 */
public abstract class ListTerminationHandler extends ModifierHandler<Boolean>{
	@Override
	@SuppressWarnings("unchecked")
    public Boolean handleDeserialize0(String fieldName, Object entity, InputStream in) throws IOException{
        return handleDeserialize0(fieldName, entity, in, (List<Object>) access.context());
    }

    @Override
    public Boolean handleSerialize0(String fieldName, Object entity) {
        throw new UnsupportedOperationException("should not be called during serializing");
    }
    
    /**
     * Overloaded method to implement custom list termination logic.
     * <p>
     * It is invoked before every element is deserialized and added to the list,
     * which means during the first invocation the list object passed as the last
     * argument is empty.
     * 
     * @param fieldName name of the field currently being processed
     * @param entity    the half-constructed object, the list object passed as the
     *                  last argument is not assigned to the corresponding field
     *                  during this stage.
     * @param in        input stream object, it is a special one that any reads will
     *                  be undone after this method returns
     * @param list      the temporary and half-constructed list object.
     * @return whether the deserialization process of this field should terminate.
     * @throws IOException
     */
    public abstract boolean handleDeserialize0(String fieldName, Object entity, InputStream in, List<Object> list) throws IOException;
}
