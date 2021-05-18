package io.github.zhtmf.annotations.types;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.github.zhtmf.annotations.modifiers.BigEndian;
import io.github.zhtmf.annotations.modifiers.LittleEndian;

/**
 * <p>
 * 32-bit IEEE 754 single-precision floating number which corresponds with
 * <code>float</code> type in Java. It is compatible with:
 * <ul>
 * <li><code>float</code> and its wrapper <code>java.lang.Float class</li>.
 * </ul>
 * <p>
 * Endianness is specified with {@link BigEndian} / {@link LittleEndian} and
 * normal endianness rules apply.
 * 
 * 
 * @author dzh
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface FLOAT {
	
}
