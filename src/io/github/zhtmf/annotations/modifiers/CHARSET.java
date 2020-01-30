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
    
    Script[] scripts() default {};
    
    public static final String DEFAULT_CHARSET = "UTF-8";
}
