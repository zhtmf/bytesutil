package io.github.zhtmf.annotations.modifiers;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.github.zhtmf.annotations.modifiers.PlaceHolderHandler.DefaultConditionalHandler;
import io.github.zhtmf.converters.auxiliary.ModifierHandler;

/**
 * Used to indicate that a field should be omitted under certain situations.
 * <p>
 * Users should always supply an implementation of
 * <code>ModifierHandler&lt;Boolean&gt;</code> or a custom script to generate
 * one which will be called in prior to and in addition to other conversions
 * that normally would apply. If this handler returns <code>false</code>, no
 * conversion will happen and this field will be omitted during
 * serialization/deserialization progress.
 * <p>
 * If {@link #negative()} is true, result of calling the handler specified by
 * {@link #value()} is reversed before referred to.
 * 
 * @author dzh
 */
@Retention(RUNTIME)
@Target({FIELD})
public @interface Conditional {
    /**
     * <code>ModifierHandler&lt;Boolean&gt;</code> class whose instance will always
     * be called in prior to and in addition to other conversions that normally
     * would apply.
     * <p>
     * This is incompatible with previous versions, as previously there is only one 
     * <tt>value</tt> property in this annotation and it is marked as required.
     * 
     * @return whether this field should be omitted
     */
    Class<? extends ModifierHandler<Boolean>> value() default DefaultConditionalHandler.class;

    /**
     * Whether result of calling the handler specified by {@link #value()} should be
     * reversed before referred to.
     * 
     * @return whether result of calling the handler should be reversed.
     */
    boolean negative() default false;
    
    /**
     * Use a {@link Script Script} to generate implementation of {@link #value()
     * value}.
     * <p>
     * This property is defined as an array only for convenience of specifying
     * default value. Only one {@link Script Script} is required and only the first
     * element is used to generate the implementation.
     * <p>
     * Compilation of the script is done during initial parsing process and any
     * syntax error will result in exceptions. Even the annotated property is
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
