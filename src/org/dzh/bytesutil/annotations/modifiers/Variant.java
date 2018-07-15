package org.dzh.bytesutil.annotations.modifiers;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.dzh.bytesutil.converters.auxiliary.EntityHandler;

@Retention(RUNTIME)
@Target(FIELD)
public @interface Variant {
	Class<? extends EntityHandler> value();
}