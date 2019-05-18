package org.dzh.bytesutil.annotations.modifiers;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.dzh.bytesutil.converters.auxiliary.ModifierHandler;
import org.dzh.bytesutil.converters.auxiliary.PlaceHolderHandler;

/**
 * <p>
 * Specified charset for all CHAR dataType fields in a class or for a single field.
 * <p>
 * Annotations on a specific field always override annotation at the class
 * level.
 * <p>
 * {@link #DEFAULT_CHARSET} is assumed if no CHARSET annotation is specified for
 * the current target.
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
    String value() default "UTF-8";

    /**
     * <p>
     * Implementation class of {@link ModifierHandler} to be referred to when the
     * charset of current target cannot be determined statically.
     * <p>
     * If this property is set, {@link #value() value} is ignored.
     * 
     * @return Implementation class of ModifierHandler
     */
    Class<? extends ModifierHandler<Charset>> handler() default PlaceHolderHandler.DefaultCharsetHandler.class;
    
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
}
