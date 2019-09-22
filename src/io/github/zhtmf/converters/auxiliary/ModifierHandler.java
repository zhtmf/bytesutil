package io.github.zhtmf.converters.auxiliary;

import java.io.IOException;
import java.io.InputStream;

/**
 * Helper class which is called during runtime to obtain dynamic values such as
 * length of list, charset of strings etc.
 * <p>
 * Within the handler method, users can refer to another property of the
 * half-constructed entity class object passed as the second parameter, or read
 * from input stream if they need to look ahead to make some decisions.
 * <p>
 * The input stream passed as the third parameter is a special one that any
 * reads will be undone after handler method returns.
 * 
 * @author dzh
 *
 * @param <E>
 *            dataType of the return value of handler methods, as required by
 *            modifier annotations.
 */
public abstract class ModifierHandler<E> {
    
    public ThreadLocal<Integer> currentPosition = new ThreadLocal<>();
    {
        currentPosition.set(-1);
    }
    
    /**
     * Returns how many bytes have been processed relative to the beginning of this
     * DataPacket but not to beginning of the whole serialization/deserialization
     * process.
     * <p>
     * This is required to implement some data structures (typically strings) which
     * do not have a deterministic length but occupy all rest space in a packet.
     * <p>
     * This method is only meant to be used in subclasses.
     * 
     * @return how many bytes have been processed relative to the beginning of this
     *         DataPacket or -1 if this method is not called within
     *         {@link #handleDeserialize0(String, Object, InputStream)
     *         handleDeserialize0}
     */
    protected int currentPosition() {
        return currentPosition.get();
    }
    
    public abstract E handleDeserialize0(String fieldName, Object entity, InputStream is) throws IOException;
    public abstract E handleSerialize0(String fieldName, Object entity);
}
