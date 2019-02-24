package org.dzh.bytesutil.annotations.types;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.dzh.bytesutil.annotations.modifiers.BigEndian;
import org.dzh.bytesutil.annotations.modifiers.LittleEndian;
import org.dzh.bytesutil.annotations.modifiers.Signed;
import org.dzh.bytesutil.annotations.modifiers.Unsigned;

/**
 * 2-byte integral dataType.
 * <p>
 * Signed/Unsigned is specified with {@link Signed} / {@link Unsigned}
 * annotation. Endianness is specified with {@link BigEndian} /
 * {@link LittleEndian} annotation.
 * <p>
 * Convertible with <code>byte</code>, <code>short</code> and their wrapper classes.
 * <p>
 * It is not an error to store an {@link Unsigned} value in such a field,
 * however incorrect values may be observed in Java code due to overflow.
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface SHORT {
}
