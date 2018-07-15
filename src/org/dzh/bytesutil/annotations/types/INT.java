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
 * 4-byte integral type, some language may use <tt>long</tt> to represent such
 * data type, while we use Java nomination.
 * <p>
 * Signed/Unsigned are specified using {@link Signed} / {@link Unsigned}
 * annotation. Endianness is specified using {@link BigEndian} / {@link LittleEndian}
 * annotation.
 * <p>
 * This data type can be converted to
 * <code>byte/Byte,short/Short,int/Integer</code>, note that if an
 * {@link Unsigned} INT value which is larger than {@link Integer#MAX_VALUE} is
 * converted to a java <code>int/Integer</code>, an exception will be thrown.
 * 
 * @author dzh
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface INT {

}
