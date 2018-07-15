package org.dzh.bytesutil.annotations.modifiers;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.dzh.bytesutil.DataType;
import org.dzh.bytesutil.converters.auxiliary.ModifierHandler;
import org.dzh.bytesutil.converters.auxiliary.PlaceHolderHandler;

/*
 * if value()>0 : static length,
 * if value()<0 (by default) and handler class is PlaceHolderModiferHandler: read length value ahead in the stream,
 * if value()>0 and handler class is not PlaceHolderModiferHandler, use handler to determine the length 
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface Length {
	int value() default -1;
	/**
	 * how the length value itself is stored in the stream.<br/>
	 * by default it is treated as a single byte value.
	 * @return	the data type which describes how the length value itself is stored in the stream
	 */
	DataType type() default DataType.BYTE;
	Class<? extends ModifierHandler<Integer>> handler() default PlaceHolderHandler.DefaultLengthHandler.class;
}
