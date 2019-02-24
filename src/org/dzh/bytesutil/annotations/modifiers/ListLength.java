package org.dzh.bytesutil.annotations.modifiers;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.dzh.bytesutil.converters.auxiliary.DataType;
import org.dzh.bytesutil.converters.auxiliary.ModifierHandler;
import org.dzh.bytesutil.converters.auxiliary.PlaceHolderHandler;

/**
 * Same as {@link Length} but it is solely used to specify length of a
 * {@link List} field.
 * <p>
 * To achieve backwards compatibility, {@link Length} can also be used to
 * specify length of a list but only when the component class is not a data dataType
 * that can be used together with a {@link Length} annotation to indicate dynamic
 * length. To avoid ambiguity, an exception will be thrown in this case.
 * 
 * @author dzh
 *
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface ListLength {
	/**
	 * Specify static length value
	 * @return
	 */
	int value() default -1;
	/**
	 * How the length value itself is stored in the stream.<br/>
	 * by default it is treated as a single byte value.
	 * @return	the data dataType which describes how the length value itself is stored in the stream
	 */
	DataType type() default DataType.BYTE;
	
	/**
	 * Specify a handler class which should be referred to at runtime to retrieve
	 * the length value
	 * 
	 * @return
	 */
	Class<? extends ModifierHandler<Integer>> handler() default PlaceHolderHandler.DefaultLengthHandler.class;
}
