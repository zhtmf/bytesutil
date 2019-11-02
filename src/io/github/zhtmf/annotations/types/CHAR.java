package io.github.zhtmf.annotations.types;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.github.zhtmf.annotations.modifiers.CHARSET;
import io.github.zhtmf.annotations.modifiers.Length;

/**
 * <p>
 * Consecutive bytes that are interpreted as human-readable characters, with
 * nothing to do with Java two-byte <tt>char</tt>.
 * <p>
 * Charsets are specified using {@link CHARSET}.
 * <p>
 * Length of CHARs should be specified with either positive {@link #value()
 * value} property or an additional {@link Length} annotation on the same field
 * but not both. If neither of them is present, an exception will be thrown
 * during initial parsing.
 * <p>
 * It is compatible with:
 * <ul>
 * <li>Integral types and their wrapper classes</li>
 * <li><code>java.util.Date</code></li>
 * <li><code>java.math.BigInteger</code></li>
 * <li><code>enum</code> types that match specific conditions</li>
 * </ul>
 * However, the string itself should be in valid format for corresponding Java
 * type (for example, can be parsed by <code>Integer.valueOf</code> when
 * converted as an <code>Integer</code>).
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
