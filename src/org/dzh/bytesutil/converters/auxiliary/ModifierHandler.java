package org.dzh.bytesutil.converters.auxiliary;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public abstract class ModifierHandler<E> {
	
	/**
	 * buffer size of {@link BufferedInputStream} when calling 
	 * custom handlers
	 */
	static final int HANDLER_READ_BUFFER_SIZE = 256;
	
	public E handleDeserialize(String fieldName, Object entity, MarkableStream is){
		is.mark(HANDLER_READ_BUFFER_SIZE);
		E ret = handleDeserialize0(fieldName, entity, is);
		if(ret==null) {
			throw new NullPointerException("should return non-null value from handler");
		}
		try {
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
	
	public abstract E handleDeserialize0(String fieldName, Object entity, InputStream is);
	public abstract E handleSerialize0(String fieldName, Object entity);
}
