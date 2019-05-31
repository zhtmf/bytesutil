package io.github.zhtmf.annotations.modifiers;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.github.zhtmf.converters.auxiliary.DataType;
import io.github.zhtmf.converters.auxiliary.ModifierHandler;
import io.github.zhtmf.converters.auxiliary.PlaceHolderHandler;

/**
 * Same as {@link Length} but it is solely used to specify length of a
 * {@link java.util.List} field.
 * <p>
 * To achieve backwards compatibility, {@link Length} can also be used to
 * specify length of a list but only when the component class is not a data type
 * that utilizes {@link Length} annotation. To avoid ambiguity, an exception
 * will be thrown in such case.
 * 
 * @author dzh
 *
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface ListLength {
    /**
     * Specify static length value
     * @return  static length value
     */
    int value() default -1;
    /**
     * How the length value itself is stored in the stream.<br>
     * by default it is treated as a single byte value.
     * @return    the data dataType which describes how the length value itself is stored in the stream
     */
    DataType type() default DataType.BYTE;
    
    /**
     * Specify a handler class which should be referred to at runtime to retrieve
     * the length value
     * 
     * @return  handler class
     */
    Class<? extends ModifierHandler<Integer>> handler() default PlaceHolderHandler.DefaultLengthHandler.class;
}