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
 * The pattern string has the same rule as those used by
 * {@link SimpleDateFormat}, however as <tt>Date</tt> can only be converted to
 * {@link BCD}, parsing with the the pattern string can only result in
 * numeric string.
 * 
 * @author dzh
 *
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface DatePattern {
	String value();
}
