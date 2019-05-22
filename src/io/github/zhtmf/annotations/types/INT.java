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
 * 4-byte integral dataType.
 * <p>
 * Signed/Unsigned is specified with {@link Signed} / {@link Unsigned}
 * annotation. Endianness is specified with {@link BigEndian} /
 * {@link LittleEndian} annotation.
 * <p>
 * Convertible with <code>byte</code>, <code>short</code>,<code>int</code> and
 * their wrapper classes and {@link java.util.Date}.
 * <p>
 * When interpreted as a date, it is assumed that this integer stores seconds
 * since epoch. Java {@link java.util.Date} values are stripped of millisecond
 * part (divide by 1000) before serialized into streams and integer values in
 * streams are multiplied by 1000 before converted to a {@link java.util.Date}
 * value.
 * <p>
 * It is not an error to store an {@link Unsigned} value in such a field,
 * however incorrect values may be observed in Java code due to overflow.
 * 
 * @author dzh
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface INT {

}
