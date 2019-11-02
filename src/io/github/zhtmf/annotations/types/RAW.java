package io.github.zhtmf.annotations.types;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.github.zhtmf.annotations.modifiers.Length;

/**
 * <p>
 * Consecutive bytes that are used as-is and not specially interpreted.
 * <p>
 * Length of RAW sequences should be specified with either positive
 * {@link #value() value} property or an additional {@link Length} annotation on
 * the same field but not both. If neither of them is present, an exception will
 * be thrown during initial parsing.
 * <p>
 * Compatible with <code>byte</code> array or <code>int</code> array. The latter
 * one can be used to store unsigned 1-byte values to avoid ambiguity in Java
 * codes.
 * 
 * @author dzh
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface RAW {
    /**
     * Specifies length of this block of byte stream.
     * 
     * @return number of bytes
     */
    int value() default -1;
}
