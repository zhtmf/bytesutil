package org.dzh.bytesutil.annotations.types;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <p>
 * Binary-Coded-Decimal data which uses one byte to represent two digits of
 * decimal.
 * <p>
 * Java types that are convertable to/from BCD data are
 * <ul>
 * <li><code>java.util.Date</code> (8 digits decimal in the form of
 * YYYYMMDD)</li>
 * <li><code>String</code> (numeric strings)</li>
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
