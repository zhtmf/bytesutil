package org.dzh.bytesutil.annotations.modifiers;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.dzh.bytesutil.annotations.types.CHAR;

/**
 * <p>
 * Specifies a <code>CHAR</code> type field is of indeterministic length and its
 * end is marked by specific sequence of bytes as returned by {@link #value()}
 * of this annotation.
 * <p>
 * Typical use of this annotation is implementing NULL terminated or line-feed
 * terminated strings.
 * <p>
 * When deserialized, the ending array is discarded and not included in the
 * field value. Similarly when serialized, user must not include the ending
 * array in the field value.
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
	 * TODO:
	 */
	public static final byte[] EOF = new byte[] {-1};

	/**
	 * Byte sequence used which indicates termination of a string. 
	 * <p>
	 * A zero-length array is treated as an error.
	 * 
	 * @return
	 */
	byte[] value();
}
