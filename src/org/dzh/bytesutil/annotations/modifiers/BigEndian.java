package org.dzh.bytesutil.annotations.modifiers;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <p>
 * Specifies that all integral data types in a class or only the annotated field
 * should be interpreted as big-endian.
 * <p>
 * Annotations on a field always override annotations on the class.
 * <p>
 * big-endian is the default endian-ness, if no {@link BigEndian} or
 * {@link LittleEndian} is represent on the current field or enclosing class,
 * big-endian is assumed.
 * 
 * @author dzh
 */
@Retention(RUNTIME)
@Target({ TYPE, FIELD })
public @interface BigEndian {
}
