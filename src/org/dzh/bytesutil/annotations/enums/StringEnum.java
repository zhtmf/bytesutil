package org.dzh.bytesutil.annotations.enums;

/**
 * "Marker" interface used to enable an enum-typed field be mapped to string
 * values (byte sequences interpreted as human-readable text).
 * <p>
 * The enum class implementing this interface should return non-null and
 * distinct string value for each enum member. Length of strings returned are
 * also restricted by annotations like {@link Length}.
 * <p>
 * If for some reason the enum class cannot implement this interface, it should
 * return string value in {@link #toString()} methods for each enum member.
 */
public interface StringEnum {
	/**
	 * Return the string value which logically an enum member is mapped to.
	 * 
	 * @return return the numeric value
	 */
	String getValue();
}
