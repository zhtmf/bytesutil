package org.dzh.bytesutil.annotations.modifiers;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.dzh.bytesutil.converters.auxiliary.EntityHandler;

/**
 * Used to indicate instantiation of the class of a field needs a custom handler
 * rather than using plain reflection.
 * <p>
 * Typical uses of this annotation are when the object being constructed need to
 * carry over some properties from its "parent" or when the declaring type of a
 * field is an interface/abstract class and it needs to be instantiated to be
 * one of its implementations.
 * 
 * @author dzh
 *
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface Variant {
	/**
	 * Class of the {@link EntityHandler} whose instance be created during parsing
	 * and called during deserialization to initiate this field.
	 * 
	 * @return
	 */
	Class<? extends EntityHandler> value();
}