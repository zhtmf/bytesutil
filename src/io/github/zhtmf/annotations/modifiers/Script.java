package io.github.zhtmf.annotations.modifiers;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

@Retention(RUNTIME)
public @interface Script {

    String value();
    
    String deserialize() default "";
}
