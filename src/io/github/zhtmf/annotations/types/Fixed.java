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
 * Exceptions will be thrown if the number overflows or underflows the defined
 * range or, when the field type is <tt>double</tt> or
 * <tt>java.lang.Double</tt>, the range of a Java double.
 * <p>
 * Due to inaccurate nature of float numbers, the value restored from the stream
 * may be different from the original one. But still can be considered equal by
 * {@link Double#compare(double, double) Double.compare} if enough fractional
 * bits are used. <br>
 * For example, consider the value <tt>255.12334</tt>. If it is serialized as a
 * signed number and then deserialized as another double value <tt>d</tt> using
 * 16 bits integer part and 16 bits fraction part, though its decimal value is
 * perfectly within that range, however <tt>Double.compare(255.12334, d)</tt>
 * will return <tt>1</tt>. But if the number of bits used are increased to 64
 * bits for fraction part, <tt>Double.compare(255.12334, d)</tt> will return
 * <tt>0</tt>, which means they are considered equal by this method.
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
