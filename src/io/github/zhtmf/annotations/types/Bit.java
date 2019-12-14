package io.github.zhtmf.annotations.types;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.ListLength;
import io.github.zhtmf.annotations.modifiers.LittleEndian;

/**
 * Numeric type consisted with one or more bits within a byte.
 * <p>
 * This type is supported for bitmaps found in many protocols (like WebSocket)
 * to ease the pain of parsing them manually.
 * <p>
 * It is compatible with:
 * <ul>
 * <li><tt>java.lang.Byte</tt> and <tt>byte</tt></li>
 * <li><code>boolean</code> or <code>Boolean</code></li>
 * <li><code>enum</code> types that mapped to numbers</li>
 * </ul>
 * <p>
 * <p>
 * When mapped to boolean flags, the {@link #value()} must be 1. For
 * enumerations, the values enum members mapped to must be in the range of 0 to
 * maximum number representable by this many bits inclusively. For example if
 * one field is marked with <tt>@Bit(3)</tt> and is an enum, then mapped values
 * of all enum members should be in the range of 0 to 7 (2^3-1).
 * <p>
 * As byte is the smallest unit in a stream, fields of this type must appear
 * consecutively in the same class and in groups of 8-bits, thus form bytes in
 * whole.
 * <p>
 * This data type does not support dynamic length or conditional processing.
 * Number of bits is solely determined by {@link #value()} or separate
 * {@link Length#value()} and should be in the range from 1 to 7 inclusively. As
 * 8-bit is same as a byte and you should use {@linkplain BYTE} instead.
 * <p>
 * If a field of this type is a <tt>List</tt> itself, length of the List must
 * also be static and specified by {@link ListLength#value()}. In this case the
 * {@link #value()} is number of bits for each list component but not number of
 * bits in whole. For example if a field is declared as
 * <tt>List&lt;Boolean&gt;</tt> to represent a sequence of bit flags and its
 * length is 3, then the correct annotation is <tt>@Bit(1)</tt> but not
 * <tt>@Bit(3)</tt>.
 * <p>
 * Numbers represented by this type are always considered unsigned but
 * endianness rules still apply. If a field of this type is marked
 * {@link LittleEndian} and {@link #value()} is larger than 1, then order of
 * bits are reversed before conversion into a number. For example if a field is
 * marked with <tt>@Bit(3)</tt> and its little-endian, then three bits of
 * <tt>001</tt> in the stream will appear as 8 (100 in binary) in java codes.
 * 
 * @author dzh
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface Bit {
    /**
     * Number of bits associated with this field. The value should be in the range
     * from 1 to 7 inclusively.
     * <p>
     * The default value is 1, so <tt>@Bit(1)</tt> can be abbreviated as
     * <tt>@Bit</tt>.
     * 
     * @return Number of bits associated with this field
     */
    int value() default 1;
}
