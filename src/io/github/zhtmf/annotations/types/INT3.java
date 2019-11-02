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
 * 3-byte integral dataType.
 * <p>
 * Signed/Unsigned-ness is specified with {@link Signed} / {@link Unsigned} ,
 * while Endianness is specified with {@link BigEndian} / {@link LittleEndian}.
 * <p>
 * It is compatible with:
 * <ul>
 * <li><code>int, long</code> and their wrapper classes</li>
 * </ul>
 * <p>
 * It is not an error to store an unsigned value in such a field, however
 * incorrect values may be observed in Java code due to wrap-around.
 * <p>
 * This type, as well as other non-standard integer types are here for protocols
 * which supports non-standard systems like MySQL's binary client-server
 * protocol.
 * 
 * @author dzh
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface INT3 {
}
