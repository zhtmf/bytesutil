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
 * <li><code>byte/Byte,short/Short,int/Integer</code>, if digits of the number is not even,
 * the leftmost bits remain zero</li>
 * </ul>
 * @author dzh
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface BCD {
	/**
	 * Specifies how many bytes that consists of this BCD data, which is typically
	 * half of the digits of the number it represents.
	 * 
	 * @return number of bytes
	 */
	int value();
}
