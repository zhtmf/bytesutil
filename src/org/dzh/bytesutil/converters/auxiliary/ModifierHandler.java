package org.dzh.bytesutil.converters.auxiliary;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.dzh.bytesutil.converters.auxiliary.exceptions.UnsatisfiedConstraintException;

/** * Helper class which is called during runtime to obtain dynamic values such as
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
	
	boolean checkLength = false;
	
	public E handleDeserialize(String fieldName, Object entity, MarkableInputStream is) throws IllegalArgumentException{
		is.mark(HANDLER_READ_BUFFER_SIZE);
		E ret = null;
		try {
			ret = handleDeserialize0(fieldName, entity, is);
			checkReturnValue(ret);
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
