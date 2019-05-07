package org.dzh.bytesutil.annotations.modifiers;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.dzh.bytesutil.DataPacket;

/**
 * <p>
 * Mark a specific field should be processed by this library, as well as
 * specifying its order during serializing/deserializing process.
 * <p>
 * It is a must because the definition of {@link Class#getDeclaredFields()} says
 * the fields returned are not in any particular order. So fields not annotated
 * with this annotation are effectively ignored by this library.
 * <p>
 * Non-final, non-static fields annotated are processed while other fields are
 * effectively ignored even marked by this annotation. Fields are processed
 * according to their {@link #value() value} in ascending order. It is neither
 * mandatory to begin the order value from 0 nor make them consecutive as long
 * as any two <code>Order</code> annotations in the same class do not specify
 * same order value.
 * <p>
 * Fields in super classes are considered independent with fields of sub
 * classes, it is not an error for fields in super classes and sub classes be
 * annotated with same order value. Super class fields are always processed
 * before sub class ones.
 * <p>
 * Besides this annotation, other annotations in {@code types} package should
 * also be used to define what type of data this field is mapped to. Annotations
 * in {@code modifiers} package may also be utilized to define other properties.
 * <p>
 * Fields declared as subclass of {@link DataPacket} does not need to be marked
 * with an annotation in {@code types} package, however they should declare a
 * no-arg constructor and type of it should be accessible (not a non-static
 * inner class, a private inner class or a local class).
 * 
 * @author dzh
 *
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface Order {

	/**
	 * Specify the order value
	 * @return
	 */
	int value();
	
	/**
	 * Convenient value for the last field
	 */
	public static final int LAST = Integer.MAX_VALUE;
}
