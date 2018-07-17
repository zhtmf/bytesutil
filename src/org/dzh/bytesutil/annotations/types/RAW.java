package org.dzh.bytesutil.annotations.types;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.dzh.bytesutil.annotations.modifiers.Length;

/**
 * <p>
 * Consecutive bytes that are read as-is and not specially interpreted.
 * <p>
 * Length of this type of data can be specified with either {@link #value()}
 * property or additional {@link Length} annotation on the same field. If the
 * <tt>value</tt> property is left unassigned, or assigned to a negative value,
 * the <tt>Length</tt> annotation is referred to, and if that annotation is not
 * present an exception is thrown.
 * <p>
 * This data type can only be "converted" to a byte array or an int array.
 * @author dzh
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface RAW {
	/**
	 * Specifies length of this byte stream.
	 * 
	 * @return
	 */
	int value() default -1;
}
