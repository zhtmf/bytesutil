package org.dzh.bytesutil.annotations.types;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.dzh.bytesutil.TypeConverter;

/**
 * A special type which is serialized/deserialized entirely by the user.
 * <p>
 * Any java type can be a {@code UserDefined} as long as the user supplies a
 * reasonable {@link TypeConverter}.
 * <p>
 * Except for the {@link TypeConverter}, users must as well supply a reasonable
 * length value (number of bytes) for this field, either through
 * {@link #length()} property or separate {@link Length} annotation, same as
 * what requires by other similar data types.
 * <p>
 * However write-ahead length is not supported and cannot be supported.
 *
 * @author dzh
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface UserDefined {
	/**
	 * Implementation class of {@code TypeConverter} used in
	 * serializing/desrializing of this field.
	 * 
	 * @return	class object of {@code TypeConverter} implementing class.
	 */
	@SuppressWarnings("rawtypes")
	Class<? extends TypeConverter> value();
	/**
	 * Length in bytes of this user defined object
	 * @return	length in bytes
	 */
	int length() default -1;
}
