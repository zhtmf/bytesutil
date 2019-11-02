package io.github.zhtmf.converters.auxiliary;

import java.io.IOException;
import java.io.InputStream;

/**
 * Helper class whose instances are created during parsing and called during
 * runtime to obtain dynamic values such as length of list, charset of strings
 * etc.
 * <p>
 * Within the handler method, typically users can refer to another property of
 * the half-constructed object passed as the second parameter or read from input
 * stream if they need to look ahead to make some decisions.
 * <p>
 * The input stream passed as the third parameter is a special one that any
 * reads will be undone after handler method returns.
 * 
 * @author dzh
 *
 * @param <E>
 *            type of the return value of handler methods.
 */
public abstract class ModifierHandler<E> {
    
    public interface OffsetAccess{
        int offset();
    }
    static OffsetAccess access;
    public static void setAccess(OffsetAccess access) {
        if(ModifierHandler.access==null) {
            ModifierHandler.access = access;
        }
    }

    /**
     * Returns how many bytes have been processed relative to the beginning of this
     * DataPacket but not relative to beginning of the entire
     * serialization/deserialization process.
     * <p>
     * This is required to implement some data structures (typically strings) which
     * do not have a deterministic length but occupy all remaining space in a
     * packet.
     * 
     * @return how many bytes have been processed relative to the beginning of this
     *         DataPacket or -1 if this method is not called within
     *         {@link #handleDeserialize0(String, Object, InputStream)
     *         handleDeserialize0}
     */
    protected int offset() {
        return access.offset();
    }

    /**
     * Method called during deserialization to obtain some dynamic values.
     * 
     * @param fieldName
     *            name of the field currently being processed
     * @param entity
     *            the half-constructed object
     * @param in
     *            input stream object, it is a special one that any reads will be
     *            undone after this method returns
     * @return  dynamic values as required by various annotations
     * @throws  IOException Exception during deserialization
     */
    public abstract E handleDeserialize0(String fieldName, Object entity, InputStream in) throws IOException;
    /**
     * Method called during serialization to obtain some dynamic values.
     * 
     * @param fieldName
     *            name of the field currently being processed
     * @param entity
     *            the half-constructed object
     * @return dynamic values as required by various annotations
     */
    public abstract E handleSerialize0(String fieldName, Object entity);
}
