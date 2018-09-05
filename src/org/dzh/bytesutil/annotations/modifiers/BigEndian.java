package org.dzh.bytesutil.annotations.modifiers;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <p>
 * Specifies that a single integral field or all integral fields in a class
 * should be interpreted as big-endian.
 * <p>
 * Annotations on a specific field always override annotation at the class
 * level.
 * <p>
 * Big-endian is the default, so if neither {@link BigEndian} nor
 * {@link LittleEndian} is represent big-endian is assumed.
 * 
 * @author dzh
 */
@Retention(RUNTIME)
@Target({ TYPE, FIELD })
public @interface BigEndian {
}
