package io.github.zhtmf.annotations.modifiers;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.github.zhtmf.annotations.types.CHAR;
import io.github.zhtmf.converters.auxiliary.DataType;
import io.github.zhtmf.converters.auxiliary.ModifierHandler;

/**
 * Same as {@link Length} but it is solely used to specify length of a
 * {@link java.util.List} field.
 * <p>
 * To achieve backwards compatibility, {@link Length} can also be used to
 * specify length of a list but only when the component class is not a data type
 * that also utilizes {@link Length} annotation (such as {@link CHAR}). To avoid
 * ambiguity, an exception will be raised in this case.
 * 
 * @author dzh
 *
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface ListLength {
    /**
     * Specify static length value
     * 
     * @return static length value
     */
    int value() default -1;

    /**
     * How the length value itself is stored in the stream.<br>
     * by default it is treated as a single byte value.
     * 
     * @return the data type which describes how the length value itself is stored
     *         in the stream
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
     * Use a {@link Script Script} to generate implementation of {@link #value()
     * value}.
     * <p>
     * This property is defined as an array only for convenience of specifying
     * default value. Only one {@link Script Script} is required and only the first
     * element is used to generate the implementation.
     * <p>
     * Compilation of the script is done during initial parsing process and any
     * syntax error will result in exceptions thrown. Even the annotated property is
     * never processed during actual serialization/deserialization.
     * <p>
     * If both {@link #value() value} and this property are assigned
     * {@link #value() value} takes precedence.
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
