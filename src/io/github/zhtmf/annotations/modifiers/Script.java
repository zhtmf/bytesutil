package io.github.zhtmf.annotations.modifiers;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import io.github.zhtmf.converters.auxiliary.ModifierHandler;

/**
 * Annotation containing script text.
 * <p>
 * This annotation is not used directly anywhere but indirectly through
 * properties of various annotations like {@link Length}, {@link CHARSET} etc.
 * <p>
 * Scripts in this annotation are used to generate implementations of
 * {@link ModifierHandler}. If only the {@link #value() value} property is
 * assigned, it is used for both
 * {@link ModifierHandler#handleSerialize0(String, Object) handleSerialize0} and
 * {@link ModifierHandler#handleDeserialize0(String, Object, java.io.InputStream)
 * handleDeserialize0}. And {@link #deserialize() deserialize} can be used to
 * override the deserialization part if logic does differ for these two
 * procedures.
 * <p>
 * Empty string has special meaning in this annotation. It means the only
 * outcome of the script is an error, like implement <tt>ModifierHandler</tt> by
 * deliberately throwing an exception. This can be used to implement such logic
 * if an entity class is only meant to be serialized or deserialized.
 * <p>
 * Within the script, several "global objects" are available:
 * <ul>
 * <li><b>fieldName</b>: name of the current class field, a string.</li>
 * <li><b>entity</b>: instance of the entity class itself.</li>
 * <li><b>handler</b>: instance of the <tt>ModifierHandler</tt>, which is used
 * to call helper methods like {@link ModifierHandler#offset}</li>
 * </ul>
 * The <tt>InputStream</tt> object is not available in the script as complex
 * logics or codes that has side effects deserve a real <tt>ModifierHandler</tt>
 * but not a simple script.
 * 
 * @author dzh
 */
@Retention(RUNTIME)
public @interface Script {

    /**
     * Default(absent) value for {@link #deserialize() deserialize} as empty string
     * serves as marker for throwing exception.
     */
    public static final String DEFAULT = "\0";

    /**
     * Script text for both serialization and deserialization process.
     * 
     * @return Script text for both serialization and deserialization process.
     */
    String value();

    /**
     * Script text for deserialization. Can be used to override deserialization part
     * of {@link #value() value}.
     * 
     * @return Script text for deserialization.
     */
    String deserialize() default DEFAULT;
}
