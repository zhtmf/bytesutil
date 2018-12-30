package org.dzh.bytesutil.annotations.enums;

/**
 * "Marker" interface used to indicates an enum-typed field is intended to be
 * mapped with string values (byte sequence that are interpreted as
 * human-readable text).
 * <p>
 * The implementing class should return non-null and distinct string value for
 * each enum member. Length of strings returned are also restricted normally by
 * annotations like {@link Length}.
 * <p>
 * If for some reason the enum class cannot implement a {@link #getValue()}
 * method, it should return that string value in {@link #toString()} methods of
 * each enum member.
 */
public interface StringEnum {
	/**
	 * Return the string value which logically an enum member is mapped to.
	 * 
	 * @return return the numeric value
	 */
	String getValue();
}
