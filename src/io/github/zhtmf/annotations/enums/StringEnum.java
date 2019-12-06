package io.github.zhtmf.annotations.enums;

import io.github.zhtmf.annotations.modifiers.Length;

/**
 * Interface used to make an enum-typed field eligible to be mapped to string
 * values (byte sequences interpreted as human-readable text).
 * <p>
 * The enum class implementing this interface should return non-null and
 * distinct string value for each enum member. Length of strings returned are
 * also restricted by annotations like {@link Length}.
 * <p>
 * If for some reason the enum class cannot implement this interface, its
 * members should return the string value from its <code>toString</code> method.
 * 
 * @author dzh
 */
public interface StringEnum {
    /**
     * Return the string which logically this enum member is mapped to.
     * 
     * @return the string value
     */
    String getValue();
}
