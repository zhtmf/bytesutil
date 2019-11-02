package io.github.zhtmf.annotations.modifiers;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.github.zhtmf.annotations.types.CHAR;

/**
 * <p>
 * Indicates that a <code>CHAR</code> field is of indeterministic length and its
 * end is marked by a specific sequence of bytes as returned by
 * {@link #value()}.
 * <p>
 * Typical use of this annotation is implementing NULL terminated or line-feed
 * terminated strings.
 * <p>
 * When deserialized, the ending sequence is discarded and not included in the
 * string value. Similarly, user must not include the ending sequence in the
 * field value during serialization.
 * <p>
 * This annotation must not be use in tandem with {@link Length} or
 * {@link CHAR#value()}
 * 
 * @author dzh
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface EndsWith {
    /**
     * Byte sequence which indicates termination of a string. 
     * <p>
     * A zero-length array is treated as an error.
     * 
     * @return  byte sequence which indicates termination of a string
     */
    byte[] value();
}
