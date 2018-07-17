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
 * Length of this type of data can be specified with either {@link #value()}
 * property or additional {@link Length} annotation on the same field. If the
 * <tt>value</tt> property is left unassigned, or assigned to a negative value,
 * the <tt>Length</tt> annotation is referred to, and if that annotation is not
 * present an exception is thrown.
 * <p>
 * This data type can be converted to <tt>char</tt> or <tt>String</tt>.
 * 
 * @author dzh
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface CHAR {
	/**
	 * Specifies length of this byte stream.
	 * 
	 * @return number of bytes
	 */
	int value() default -1;
}
