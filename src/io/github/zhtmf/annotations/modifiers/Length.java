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
 * {@link PlaceHolderHandler}, or there is a custom {@link #scripts() script},
 * it is used to specify a handler class which should be referred to at runtime
 * to retrieve the length value.</li>
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
    
    /**
	 * Use a {@link Script Script} to generate implementation of {@link #handler()
	 * handler}.
	 * <p>
	 * This property is defined as an array only for convenience of specifying
	 * default value. Only one {@link Script Script} is required and only the first
	 * element is used to generate the implementation.
	 * <p>
	 * Compilation of the script is done during initial parsing process and any
	 * syntax error will result in exceptions. Even the annotated
	 * property is never processed during actual serialization/deserialization.
	 * <p>
	 * If both {@link #handler() handler} and this property are assigned
	 * {@link #handler() handler} takes precedence.
	 * <p>
	 * For more information on how to write the script, please refer to comments on
	 * {@link Script Script} and the README file under
	 * <tt>io.github.zhtmf.script</tt> package.
	 * 
	 * @return an array of {@link Script Script} annotations. However only the first
	 *         one is used to generate handler implementation.
	 */
    Script[] scripts() default {};
}
