package org.dzh.bytesutil.annotations.types;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.dzh.bytesutil.annotations.modifiers.DatePattern;

/**
 * <p>
 * Binary-Coded-Decimal data which uses one byte to represent two decimal
 * digits.
 * <p>
 * Following Java types are convertible to/from it:
 * <ul>
 * <li><code>java.util.Date</code>, format for such date or date-values are
 * defined by {@link DatePattern} annotation. Only date formats that produces
 * numeric characters are considered valid in this situation.</li>
 * <li><code>String</code> composed of only numeric characters</li>
 * <li>Integral types</li>
 * </ul>
 * 
 * @see https://en.wikipedia.org/wiki/Binary-coded_decimal
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
