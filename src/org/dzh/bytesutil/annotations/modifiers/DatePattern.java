package org.dzh.bytesutil.annotations.modifiers;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.text.SimpleDateFormat;

/**
 * <p>
 * Specifies date pattern string for {@link java.util.Date} fields.
 * <p>
 * The pattern string has the same rule as that used by {@link SimpleDateFormat}
 * 
 * @author dzh
 *
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface DatePattern {
	String value();
}
