package org.dzh.bytesutil.annotations.modifiers;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.dzh.bytesutil.converters.auxiliary.EntityHandler;

/**
 * Used to indicate instantiation of the class of a field (or its components if
 * it is declared as a list of entities) needs custom logic rather than plain
 * reflection.
 * <p>
 * Typical uses of this annotation are when the object being constructed need to
 * carry over some properties from its "parent" or when the declaring type of a
 * field is an interface/abstract class and intended to be instantiated as one
 * concrete implementation.
 * <p>
 * Without this annotation, this library will try to instantiate entity classes
 * with its no-arg constructor.
 * <p>
 * The confusing name <code>Variant</code> is from the fact that historically it
 * is only used when creating subclasses of field types which are declared as
 * interfaces or abstract classes and there are many possibilities for the
 * actual type being instantiated to. Its use of merely providing a custom
 * creator has only been added recently.
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