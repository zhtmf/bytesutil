package io.github.zhtmf.annotations.modifiers;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.github.zhtmf.converters.auxiliary.DataType;
import io.github.zhtmf.converters.auxiliary.ModifierHandler;

/**
 * Annotation used to specify length of various data types. It is used in the
 * following cases:
 * <ul>
 * <li>Specify length of a field declared as a {@link java.util.List}.</li>
 * <li>Specify length of a {@link io.github.zhtmf.annotations.types.CHAR CHAR}
 * field, if using {@link io.github.zhtmf.annotations.types.CHAR#value value} is
 * not preferred.</li>
 * <li>Specify length of a {@link io.github.zhtmf.annotations.types.RAW RAW}
 * field, if using {@link io.github.zhtmf.annotations.types.RAW#value value} is
 * not preferred.</li>
 * </ul>
 * <p>
 * And it is used in three different ways:
 * <ul>
 * <li>If {@link #value() value} is equal to or greater than 0, it is used to
 * declare a static length. In this case, it is the same as the <tt>value</tt>
 * property of <tt>CHAR</tt>, <tt>RAW</tt> etc.</li>
 * <li>If {@link #value() value} is left unassigned or assigned with a negative
 * value and {@link #handler() handler} is set to a class other than
 * {@link PlaceHolderHandler}, it is used to specify a handler class which
 * should be referred to at runtime to retrieve the length value.</li>
 * <li>If {@link #value() value} is left unassigned or assigned with a negative
 * value and {@link #handler() handler} is left unassigned, it serves as a
 * marker annotation which instructs the library to dynamically read from/write
 * to the stream the length value prior to processing field value itself
 * (write-ahead length). In this case, the {@link #type() type} property is also
 * referred to to determine how the length value itself is stored in the
 * stream.</li>
 * </ul>
 * 
 * @author dzh
 *
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface Length {
    /**
     * Specify static length value
     * @return  static length value
     */
    int value() default -1;
    /**
     * How the length value itself is stored in the stream.<br>
     * by default it is treated as a single byte value.
     * @return    the data type which indicates how the length value itself is stored in the stream
     */
    DataType type() default DataType.BYTE;
    
    /**
     * Specify a handler class which should be referred to at runtime to retrieve
     * the length value
     * 
     * @return  handler class
     */
    Class<? extends ModifierHandler<Integer>> handler() default PlaceHolderHandler.DefaultLengthHandler.class;
    
    Script[] scripts() default {};
}
