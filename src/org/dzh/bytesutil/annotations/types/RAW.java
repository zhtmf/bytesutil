package org.dzh.bytesutil.annotations.types;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.dzh.bytesutil.annotations.modifiers.Length;

/**
 * <p>
 * Consecutive bytes that are read as-is and not specially interpreted. It
 * exists to represent non-integral and non-string byte data.
 * <p>
 * Length of RAW sequences should be specified with either positive
 * {@link #value() value} property or an additional {@link Length} annotation on
 * the same field. If neither of them is present, an exception will be thrown
 * during initial parsing.
 * <p>
 * This data type can be "converted" from/to a byte array or an
 * <code>int</code> array.
 * 
 * @author dzh
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface RAW {
	/**
	 * Specifies length of this block of byte stream.
	 * 
	 * @return number of bytes
	 */
	int value() default -1;
}
