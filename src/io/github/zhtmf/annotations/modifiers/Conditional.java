package io.github.zhtmf.annotations.modifiers;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.github.zhtmf.annotations.modifiers.PlaceHolderHandler.DefaultConditionalHandler;
import io.github.zhtmf.converters.auxiliary.ModifierHandler;

/**
 * Used to indicate that a field should be omitted under certain situations.
 * <p>
 * Users should always supply a <code>ModifierHandler&lt;Boolean&gt;</code>
 * class whose instance will always be called in prior to and in addition to
 * other conversions that normally would apply. If this handler returns
 * <code>false</code>, no conversion will happen and this field will be omitted
 * during serialization/deserialization progress.
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
     * would apply
     * 
     * @return whether this field should be omitted
     */
    //TODO: incompatible with previous versions
    Class<? extends ModifierHandler<Boolean>> value() default DefaultConditionalHandler.class;

    /**
     * Whether result of calling the handler specified by {@link #value()} should be
     * reversed before referred to.
     * 
     * @return whether result of calling the handler should be reversed.
     */
    boolean negative() default false;
    
    Script[] scripts() default {};
}
