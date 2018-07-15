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
 * Specified charset for all CHAR type data in a class or for a specific field.
 * <p>
 * Annotations on a field always override annotations on the class.
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
	 * Name of the charset, which must be a valid charset for the current platform.
	 * 
	 * @return
	 */
	String value() default "UTF-8";

	/**
	 * <p>
	 * Class of implementation of {@link ModifierHandler} to consult with when the charset of current
	 * target cannot be determined prior to parsing.
	 * <p>
	 * If this property is set to a value other than the default value, the
	 * {@link #value()} property is ignored.
	 * 
	 * @return	class of implementation of ModifierHandler
	 */
	Class<? extends ModifierHandler<Charset>> handler() default PlaceHolderHandler.DefaultCharsetHandler.class;
	public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
}
