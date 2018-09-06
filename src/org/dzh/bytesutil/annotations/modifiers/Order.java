package org.dzh.bytesutil.annotations.modifiers;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <p>
 * Specifies the order of a certain field in the enclosing java class during
 * serializing/deserializing process.
 * <p>
 * It is a must because the definition of {@link Class#getDeclaredFields()} says
 * the fields returned are not in any particular order. So fields not annotated
 * with this annotation are effectively ignored by this library.
 * <p>
 * Fields are sorted according to their {@link #value() value} in ascending
 * order. It is not mandatory to begin the order value from 0, as long as any
 * two <tt>Order</tt> annotations do not specify same order value.
 * 
 * @author dzh
 *
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface Order {

	/**
	 * Specify the order value, fields are 
	 * @return
	 */
	int value();
	
	/**
	 * Convenient value to denote the last field in a class
	 */
	public static final int LAST = Integer.MAX_VALUE;
}
