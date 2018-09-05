package org.dzh.bytesutil.annotations.modifiers;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation used to declare that the annotated list value is of
 * indeterministic length and its end is detected by reaching end of the stream.
 * <p>
 * This annotation should not be used together with {@link Length} or
 * {@link ListLength}
 * TODO:
 * @author dzh
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface EOF {

}
