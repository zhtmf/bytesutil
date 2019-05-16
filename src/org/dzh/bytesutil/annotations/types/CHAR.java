package org.dzh.bytesutil.annotations.types;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.dzh.bytesutil.annotations.modifiers.CHARSET;

/**
 * <p>
 * Consecutive bytes that are interpreted as human-readable characters. It has
 * nothing to do with Java two-bytes <tt>char</tt> dataType.
 * <p>
 * Char sets are specified using {@link CHARSET}.
 * <p>
 * Length of CHARs should be specified with either positive {@link #value()
 * value} property or an additional {@link Length} annotation on the same field,
 * but not both. If neither of them is present, an exception will be thrown
 * during initial parsing.
 * <p>
 * CHARs are convertible from/to <tt>char</tt>,<tt>Character</tt> or
 * <tt>String</tt>.
 * 
 * @author dzh
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface CHAR {
	/**
	 * Specifies length of this string.
	 * 
	 * @return number of bytes
	 */
	int value() default -1;
}
