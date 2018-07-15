package org.dzh.bytesutil.annotations.types;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.dzh.bytesutil.annotations.modifiers.Length;

/**
 * <p>
 * Consecutive bytes that are read as-is and not specially interpreted.
 * <p>
 * This data type can only be "converted" to byte array. Its length is specified
 * using a separate annotation {@link Length} which handles static/dynamic
 * length.
 * 
 * @author dzh
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface RAW {
}
