package org.dzh.bytesutil.annotations.types;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.dzh.bytesutil.annotations.modifiers.Unsigned;

/**
 * <p>
 * Data type that represents a single byte.
 * <p>
 * Java types that are convertable to/from BCD data are
 * <ul>
 * <li><code>byte/Byte,short/Short,int/Integer</code>, note that if an {@link Unsigned}
 * byte value which is larger than 127 is converted to a java <code>byte</code>,
 * an exception will be thrown.</li>
 * </ul>
 * 
 * @author dzh
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface BYTE {
}
