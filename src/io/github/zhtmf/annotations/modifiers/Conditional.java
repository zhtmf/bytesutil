package io.github.zhtmf.annotations.modifiers;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.github.zhtmf.converters.auxiliary.ModifierHandler;

@Retention(RUNTIME)
@Target({FIELD})
public @interface Conditional {
    Class<? extends ModifierHandler<Boolean>> value();
}
