package org.dzh.bytesutil.converters.auxiliary;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Helper class which is called during runtime to obtain dynamic values such as
 * length of list, charset of strings etc.
 * <p>
 * Within the handler method, typically users can refer to another property of
 * the (incomplete) entity class object passed as the second parameter, or
 * directly look ahead from the input stream during deserialization for some
 * values.
 * <p>
 * The input stream object passed as the third parameter is a special one that
 * any reads will be undone after the method returns, so it is OK to read within
 * the method, however there is a limit on how many bytes users can read.
 * 
 * @author dzh
 *
 * @param <E>
 *            type of the return value of handler methods, as required by
 *            modifier annotations.
 */
public abstract class ModifierHandler<E> {
	
	/**
	 * buffer size of {@link BufferedInputStream} when calling 
	 * custom handlers
	 */
	static final int HANDLER_READ_BUFFER_SIZE = 256;
	
	public E handleDeserialize(String fieldName, Object entity, MarkableInputStream is){
		is.mark(HANDLER_READ_BUFFER_SIZE);
		E ret = null;
		try {
			ret = handleDeserialize0(fieldName, entity, is);
			if(ret==null) {
				throw new NullPointerException("should return non-null value from handler");
			}
			is.reset();
		} catch (IOException e) {
			throw new Error(e);
		}
		return ret;
	}
	
	public E handleSerialize(String fieldName, Object entity){
		E ret = handleSerialize0(fieldName, entity);
		if(ret==null) {
			throw new NullPointerException("should return non-null value from handler");
		}
		return ret;
	}
	
	public abstract E handleDeserialize0(String fieldName, Object entity, InputStream is) throws IOException;
	public abstract E handleSerialize0(String fieldName, Object entity);
}
