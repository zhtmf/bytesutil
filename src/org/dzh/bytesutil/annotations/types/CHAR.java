package org.dzh.bytesutil.annotations.types;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.dzh.bytesutil.annotations.modifiers.CHARSET;

/**
 * <p>
 * Consecutive bytes that are interpreted as human-readable characters.
 * <p>
 * Note that this data type has nothing to do with Java two-bytes <tt>char</tt>
 * type, as CHARs are in fact single-byte arrays.
 * <p>
 * Charset of the string CHAR data represents can be specified using
 * {@link CHARSET} annotation.
 * <p>
 * This data type can be converted to <tt>char</tt> or <tt>String</tt>.
 * @author dzh
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface CHAR {
	/**
	 * Specifies length (how many bytes) in the data.
	 * <p>
	 * It is ignored if this annotation is applied to a <tt>char</tt> field, which always assumes 
	 * the data is a single-byte ASCII character.
	 * TODO:
	 * @return number of bytes
	 */
	int value() default -1;
}
