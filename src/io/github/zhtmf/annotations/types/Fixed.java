package io.github.zhtmf.annotations.types;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.github.zhtmf.annotations.modifiers.BigEndian;
import io.github.zhtmf.annotations.modifiers.LittleEndian;
import io.github.zhtmf.annotations.modifiers.Signed;
import io.github.zhtmf.annotations.modifiers.Unsigned;

/**
 * Fixed point floating number representation which typically mapped to
 * <tt>double</tt> in Java.
 * <p>
 * Signed/Unsigned-ness is specified with {@link Signed} / {@link Unsigned}.
 * while Endianness is specified with {@link BigEndian} / {@link LittleEndian}.
 * <p>
 * The {@link #value() value} property is an array whose first and second
 * elements are referred to as number of <tt>bit</tt>s for integer and fraction
 * part of this float number. It is defined as number of bits but not bytes only
 * to be consistent as original definition in various binary schemes. Currently
 * this data type does not support float number with integer or fraction part
 * not stored in a whole byte.
 * <p>
 * It is compatible with:
 * <ul>
 * <li><tt>double</tt> and <tt>java.lang.Double</tt></li>
 * <li><code>java.math.BigDecimal</code></li>
 * </ul>
 * <p>
 * Exceptions will be thrown if the number are out of range of a Java double or
 * integer part of the java value cannot be stored in such many bytes as
 * specified by {@link #value() value}. But for the fraction part this library
 * will silently ignore extra digits.
 * <p>
 * Due to inaccurate nature of float numbers insufficient fraction bits may
 * cause difference between the original number and the one restored from the
 * stream as considered by {@link Double#compare(double, double)}.
 * 
 * @author dzh
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface Fixed {
    
    /**
     * Length of integer and fraction parts in <tt>bit</tt>s. Only the first two
     * elements are used.
     * <p>
     * Length of those two parts must be greater than zero and multiples of 8.
     * 
     * @return Length of integer and fraction parts in <tt>bit</tt>s
     */
    int[] value();
}
