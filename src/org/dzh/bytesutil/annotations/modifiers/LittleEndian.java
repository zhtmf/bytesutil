package org.dzh.bytesutil.annotations.modifiers;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <p>
 * Specifies that a single integral field or all integral fields in a class
 * should be interpreted as little-endian.
 * <p>
 * Annotations on a specific field always override annotation at the class
 * level.
 * 
 * @author dzh
 *
 */
@Retention(RUNTIME)
@Target({ TYPE, FIELD })
public @interface LittleEndian {
}
