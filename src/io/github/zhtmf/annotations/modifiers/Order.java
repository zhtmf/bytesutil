package io.github.zhtmf.annotations.modifiers;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.github.zhtmf.DataPacket;

/**
 * <p>
 * Mark a field as needed to be processed by this library, as well as specifying
 * its order in the stream.
 * <p>
 * It is a must because the definition of {@link Class#getDeclaredFields()
 * getDeclaredFields} says the fields returned are not in any particular order.
 * Fields not annotated with this annotation are effectively ignored.
 * <p>
 * Non-final, non-static fields are processed while other fields are simply
 * ignored. Fields are processed according to their {@link #value() value} in
 * ascending order. It is neither mandatory to begin the order value from 0 nor
 * to make them consecutive as long as any two <code>Order</code> annotations in
 * the same class do not specify same order value.
 * <p>
 * Fields in super classes are considered independent with fields in sub
 * classes, it is not an error for fields in super classes and sub classes be
 * annotated with same order value. Super class fields are always processed
 * before sub class ones.
 * <p>
 * Besides this annotation, other annotations in {@code types} package should
 * also be used to define what type of data this field is mapped to. Annotations
 * in {@code modifiers} package may also be utilized to define other properties.
 * <p>
 * Fields declared as subclass of {@link DataPacket} do not need to be marked
 * with an annotation in {@code types} package, however they should declare a
 * no-arg constructor and its type should be an accessible (not a non-static
 * inner class, a private inner class or a local class). Such fields will always
 * be assigned with newly created objects during deserialization. Any value
 * associated with them before will be overwritten.
 * 
 * @author dzh
 *
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface Order {

    /**
     * Specify the order value
     * @return  numeric order value
     */
    int value();
    
    /**
     * Convenient value for the last field
     */
    public static final int LAST = Integer.MAX_VALUE;
}
