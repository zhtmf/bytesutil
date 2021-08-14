package io.github.zhtmf.annotations.types;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.github.zhtmf.annotations.modifiers.BigEndian;
import io.github.zhtmf.annotations.modifiers.LittleEndian;

/**
 * <p>
 * 64-bit IEEE 754 double-precision floating number which corresponds with
 * <code>double</code> type in Java. It is compatible with:
 * <ul>
 * <li><code>float</code> and its wrapper <code>java.lang.Float</code> class</li>
 * <li><code>double</code> and its wrapper <code>java.lang.Double</code> class</li>
 * </ul>
 * <p>
 * However if the user intends to use a <code>float</code> to store a
 * <code>double</code> value and the double value meets any of the following
 * condition, an exception will be thrown due to potential precision loss:
 * <ul>
 * <li>it is larger than <code>Float.MAX_VALUE</code>.</li>
 * <li>it is smaller than <code>-Float.MAX_VALUE</code>.</li>
 * <li>it is too small to represent in single-precision floating number, that is
 * to say, it is positive and more close to zero than
 * <code>Float.MIN_VALUE</code>.</li>
 * </ul>
 * <p>
 * Endianness is specified with {@link BigEndian} / {@link LittleEndian} and
 * normal endianness rules apply.
 * 
 * @author dzh
 *
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface DOUBLE {

}
