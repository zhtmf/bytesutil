package io.github.zhtmf.annotations.types;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.github.zhtmf.TypeConverter;
import io.github.zhtmf.annotations.modifiers.Length;

/**
 * A special type which is processed solely by the user.
 * <p>
 * Any java type can be a {@code UserDefined} as long as the user supplies a
 * reasonable {@link TypeConverter}.
 * <p>
 * Except for the {@link TypeConverter} users must as well supply a reasonable
 * length value (number of bytes) for this field, either through
 * {@link #length()} property or a separate {@link Length} annotation, same as
 * what requires by other similar data types. However write-ahead length is not
 * and cannot be supported.
 *
 * @author dzh
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface UserDefined {
    
    /**
     * Class of {@code TypeConverter} implementation.
     * 
     * @return class object of {@code TypeConverter} implementation class.
     */
    @SuppressWarnings("rawtypes")
    Class<? extends TypeConverter> value();

    /**
     * Length in bytes of this user defined object
     * 
     * @return length in bytes
     */
    int length() default -1;
}
