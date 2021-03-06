package io.github.zhtmf.annotations.types;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.github.zhtmf.annotations.modifiers.DatePattern;

/**
 * <p>
 * Binary-Coded-Decimal data which uses one byte to represent two decimal
 * digits.
 * <p>
 * It is compatible with:
 * <ul>
 * <li><code>java.util.Date</code>, format for such date or date-values are
 * defined by {@link DatePattern} annotation. Only date formats that produces
 * numeric characters are considered valid in this case.</li>
 * <li><code>String</code> composed of only numeric characters</li>
 * <li>Integral types and their wrapper classes</li>
 * </ul>
 * 
 * @author dzh
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface BCD {
    /**
     * Specifies how many <b>bytes</b> that make up of this BCD data, which is half of the
     * digits of the number it represents.
     * 
     * @return number of bytes
     */
    int value();
}
