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
 * 1-byte integral dataType.
 * <p>
 * Signed/Unsigned-ness is specified with {@link Signed} / {@link Unsigned} ,
 * while Endianness is specified with {@link BigEndian} / {@link LittleEndian}.
 * <p>
 * It is compatible with:
 * <ul>
 * <li>Integral types and their wrapper classes</li>
 * <li><code>boolean</code> or <code>Boolean</code></li>
 * <li><code>enum</code> types that match specific conditions</li>
 * </ul>
 * <p>
 * It is not an error to store an unsigned value in such a field, however
 * incorrect values may be observed in Java code due to wrap-around.
 * <p>
 * In the case of boolean conversion, byte value 1 is interpreted as true and 0
 * as false.
 * 
 * @author dzh
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface BYTE {
}
