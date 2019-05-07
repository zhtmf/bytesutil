package org.dzh.bytesutil.annotations.modifiers;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <p>
 * Specifies that a single integral-typed field or all integral-typed fields in
 * a class should be interpreted as unsigned.
 * <p>
 * Annotations on a specific field always override annotation at the class
 * level.
 * <p>
 * Unsigned is the default, so if neither {@link Unsigned} nor {@link Signed} is
 * represent, unsigned is assumed.
 * 
 * @author dzh
 */
@Retention(RUNTIME)
@Target({ TYPE, FIELD })
public @interface Unsigned {

}
