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
 * 2-byte integral type.
 * <p>
 * Signed/Unsigned are specified using {@link Signed} / {@link Unsigned}
 * annotation. Endianness is specified using {@link BigEndian} / {@link LittleEndian}
 * annotation.
 * <p>
 * Java integral types are convertible to/from it.
 * <p>
 * Note that as Java does not have unsigned integral types, if large unsigned
 * values are to be converted to corresponding Java integral types, overflow
 * exception may be raised by this library or incorrect values may be produced, depends on
 * whether the field is declared {@link Signed} or {@link Unsigned}. So it is
 * generally preferable to use "larger" types to store numeric values from
 * arbitrary byte streams.
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface SHORT {
}
