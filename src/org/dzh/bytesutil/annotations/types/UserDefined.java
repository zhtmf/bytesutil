package org.dzh.bytesutil.annotations.types;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.dzh.bytesutil.TypeConverter;

@Retention(RUNTIME)
@Target(FIELD)
public @interface UserDefined {
	@SuppressWarnings("rawtypes")
	Class<? extends TypeConverter> value();
	int length() default -1;
}
