package org.dzh.bytesutil.annotations.enums;

import org.dzh.bytesutil.annotations.modifiers.Length;

/**
 * Marker interface used to make an enum-typed field eligible to be mapped to
 * string values (byte sequences interpreted as human-readable text).
 * <p>
 * The enum class implementing this interface should return non-null and
 * distinct string value for each enum member. Length of strings returned are
 * also restricted by annotations like {@link Length}.
 * <p>
 * If for some reason the enum class cannot implement this interface, it should
 * return numbers as string values in <code>toString</code> methods for each enum
 * member.
 * 
 * @author dzh
 */
public interface StringEnum {
    /**
     * Return the string value which logically an enum member is mapped to.
     * 
     * @return return the numeric value
     */
    String getValue();
}
