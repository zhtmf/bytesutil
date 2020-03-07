package io.github.zhtmf.annotations.modifiers;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

@Retention(RUNTIME)
public @interface Script {
    
    public static final String DEFAULT = "\0";

    String value();
    
    String deserialize() default DEFAULT;
}
