package io.github.zhtmf.annotations.modifiers;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.nio.charset.Charset;

import io.github.zhtmf.converters.auxiliary.ModifierHandler;

/**
 * <p>
 * Specify charset for all CHAR fields in a class or for a single field.
 * <p>
 * Annotations applied to a field always override annotation at the class level.
 * <p>
 * {@link #DEFAULT_CHARSET} is assumed if no CHARSET annotation is specified.
 * 
 * @author dzh
 */
@Retention(RUNTIME)
@Target({ TYPE, FIELD })
public @interface CHARSET {

    public static final String DEFAULT_CHARSET = "UTF-8";
    
    /**
     * Name of the charset, which must be a valid charset name.
     * 
     * @return  name of the charset
     */
    String value() default DEFAULT_CHARSET;

    /**
     * <p>
     * Implementation class of {@link ModifierHandler} to be referred to when the
     * charset should be determined at runtime.
     * <p>
     * If this property is set, {@link #value() value} is ignored.
     * 
     * @return  Implementation class of ModifierHandler
     */
    Class<? extends ModifierHandler<Charset>> handler() default PlaceHolderHandler.DefaultCharsetHandler.class;
    
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
