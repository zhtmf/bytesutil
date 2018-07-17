package org.dzh.bytesutil.annotations.modifiers;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.dzh.bytesutil.converters.auxiliary.EntityHandler;

/**
 * Used to specify the type of a field can only be defined at runtime during
 * deserializing process by calling a {@link EntityHandler}
 * <p>
 * A typical use of this annotation is when different types of message body of a
 * protocol have properties in common, the field for message body in top-level
 * entity class can be declared as a base class (which may be abstract) holding
 * common properties and properties specific to one type of message body are
 * declared in subclasses. Then mark the field with this annotation and provide
 * a reasonable <tt>EntityHandler</tt> class and at runtime the handler class
 * will be instantiated and called obtain instances of concrete subclasses of
 * the message body.
 * 
 * @author dzh
 *
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface Variant {
	Class<? extends EntityHandler> value();
}