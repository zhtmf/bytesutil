package org.dzh.bytesutil.converters.auxiliary;

import java.io.IOException;
import java.io.InputStream;

import org.dzh.bytesutil.converters.auxiliary.exceptions.UnsatisfiedConstraintException;

/**
 * Helper class which is called during runtime to obtain dynamic values such as
 * length of list, charset of strings etc.
 * <p>
 * Within the handler method, users can refer to another property of the
 * half-constructed entity class object passed as the second parameter, or read
 * from input stream if they need to look ahead to make some decisions.
 * <p>
 * The input stream passed as the third parameter is a special one that any
 * reads will be undone after handler method returns, however users cannot read
 * more than {@link #HANDLER_READ_BUFFER_SIZE} bytes. An exception will be
 * thrown <b>after</b> the handler method returns if they do.
 * 
 * @author dzh
 *
 * @param <E>
 *            dataType of the return value of handler methods, as required by
 *            modifier annotations.
 */
public abstract class ModifierHandler<E> {
    
    /**
     * limit of buffer
     */
    public static final int HANDLER_READ_BUFFER_SIZE = 32;
    
    //only set when this class is used as LengthHandler
    boolean checkLength = false;
    
    public E handleDeserialize(String fieldName, Object entity, MarkableInputStream is) throws IllegalArgumentException{
        is.mark(HANDLER_READ_BUFFER_SIZE);
        E ret = null;
        try {
            ret = handleDeserialize0(fieldName, entity, is);
            checkReturnValue(ret);
            /*
             * There is still a chance for errors that are hard to detect: remaining
             * processing of this entity does not use up all bytes marked after this
             * handler method returns, then some bytes in the original stream passed to
             * deserialize() are forever lost. But at least we limit the buffer size to a
             * quite small value and thus reduce the chance for such errors.
             */
            if( ! is.marked()) {
                throw new UnsatisfiedConstraintException("should not read more than  "+HANDLER_READ_BUFFER_SIZE+" in the handler")
                    .withSiteAndOrdinal(ModifierHandler.class, 4);
            }
            is.reset();
        } catch (IOException e) {
            throw new UnsatisfiedConstraintException(e)
                .withSiteAndOrdinal(ModifierHandler.class, 3);
        }
        return ret;
    }
    
    public E handleSerialize(String fieldName, Object entity)  throws IllegalArgumentException{
        E ret = handleSerialize0(fieldName, entity);
        checkReturnValue(ret);
        return ret;
    }
    
    private void checkReturnValue(E ret) {
        if(ret==null) {
            throw new UnsatisfiedConstraintException("should return non-null value from handler "+this.getClass())
                    .withSiteAndOrdinal(ModifierHandler.class, 1);
        }else if(checkLength && ((Integer)ret)<0) {
            throw new UnsatisfiedConstraintException("should return positive value from handler "+this.getClass())
            .withSiteAndOrdinal(ModifierHandler.class, 2);
        }
    }
    
    public abstract E handleDeserialize0(String fieldName, Object entity, InputStream is) throws IOException;
    public abstract E handleSerialize0(String fieldName, Object entity);
}
