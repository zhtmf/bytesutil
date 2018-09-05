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
 * type, as CHARs are in fact single-byte data.
 * <p>
 * Charsets of CHAR sequences are specified using {@link CHARSET}.
 * <p>
 * Length of CHARs should be specified with either positive {@link #value()
 * value} property or an additional {@link Length} annotation on the same field.
 * If neither of them is present, an exception will be thrown during initial
 * parsing.
 * <p>
 * CHARs can be converted from/to <tt>char</tt> or <tt>String</tt>.
 * 
 * @author dzh
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface CHAR {
	/**
	 * Specifies length of this block of byte stream.
	 * 
	 * @return number of bytes
	 */
	int value() default -1;
}
