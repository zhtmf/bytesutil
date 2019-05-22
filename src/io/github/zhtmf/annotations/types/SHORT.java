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
