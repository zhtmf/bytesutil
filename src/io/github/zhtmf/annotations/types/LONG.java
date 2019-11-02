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
 * 8-byte integral dataType.
 * <p>
 * Signed/Unsigned-ness is specified with {@link Signed} / {@link Unsigned} ,
 * while Endianness is specified with {@link BigEndian} / {@link LittleEndian}.
 * <p>
 * It is compatible with:
 * <ul>
 * <li><code>long</code> and its wrapper class</li>
 * <li><code>java.util.Date</code></li>
 * <li><code>java.math.BigInteger</code></li>
 * <li><code>enum</code> types that match specific conditions</li>
 * </ul>
 * <p>
 * When interpreted as a date, it is assumed that this integer stores
 * milliseconds since epoch.
 * <p>
 * It is not an error to store an unsigned value in such a field, however
 * incorrect values may be observed in Java code due to wrap-around.
 * 
 * @author dzh
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface LONG {

}
