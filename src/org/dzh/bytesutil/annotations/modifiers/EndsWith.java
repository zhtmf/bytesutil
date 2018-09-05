package org.dzh.bytesutil.annotations.modifiers;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation used to denote the annotated <tt>CHAR</tt> data is of
 * indeterministic length and its end is determined by special characters (like
 * line feeds) in the stream.
 * <p>
 * When annotated with this annotation, the library will automatically append
 * specified end mark to the stream during serializing and strip of it from the
 * string during deserializing, so the user should not do it manually.
 * <p>
 * The library handles characters other than ASCII ones, it is OK to use
 * "complicated" characters.
 * <p>
 * This annotation should not be used together with {@link Length}.
 * 
 * @author dzh
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface EndsWith {
	/**
	 * A string (may contains only a single character) which marks end of a
	 * piece of string, should be non-empty.
	 * 
	 * @return
	 */
	String value();
}
