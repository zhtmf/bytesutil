package org.dzh.bytesutil.annotations.modifiers;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation used to denote the annotated <tt>CHAR</tt> data is of
 * indeterministic length and its end is detected by reading special characters
 * (like line feeds) in the stream.
 * <p>
 * When annotated with this annotation, the library will automatically append
 * specified end mark to the stream during serializing and strip of it from the
 * string during deserializing, so the user should not do it manually.
 * <p>
 * The library handles characters other than ASCII ones, it is ok to use
 * "complicated" characters other than line feeds, spaces etc. to mark end of
 * strings. However because it does not do any read-ahead so the end mark
 * specified here should not appear anywhere in the data, which will cause
 * parsing error at runtime.
 * <p>
 * This annotation should not be used together with {@link Length}.
 * 
 * @author dzh
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface EndsWith {
	/**
	 * Specify characters (may be more than one) which marks end of a piece of
	 * string, the value specified here should be non-empty.
	 * 
	 * @return
	 */
	String value();
}
