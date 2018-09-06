package org.dzh.bytesutil.annotations.modifiers;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.dzh.bytesutil.converters.auxiliary.EntityHandler;

/**
 * Used to specify the field should be initiated to another sub-class as returned
 * by its {@link EntityHandler}.
 * <p>
 * Typically data packets have a "message body" part which has many different
 * types and formats, so the corresponding field can be declared as an abstract
 * super class which holds common properties, then during serialization it is
 * initiated to a concrete subclass for that type of message body.
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