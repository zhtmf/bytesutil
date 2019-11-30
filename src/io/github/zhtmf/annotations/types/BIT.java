package io.github.zhtmf.annotations.types;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * TODO:
 * @author dzh
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface BIT {
    /**
     * TODO
     * @return
     */
    int value() default 1;
}
