package org.dzh.bytesutil.annotations.enums;

/**
 * Marker interface used to make an enum-typed field eligible to mapped to
 * numeric values.
 * <p>
 * The enum class implementing this interface should return distinct value for
 * each enum member and those values should be within the defined range of
 * numeric dataType annotations ({@link BYTE}, {@link SHORT} etc.).
 * <p>
 * If for some reason the enum class cannot implement this interface, it should
 * return that numeric value as a string in {@link #toString()} methods for each
 * enum member.
 * 
 * @author dzh
 */
public interface NumericEnum {
	/**
	 * Return the numeric value which logically an enum member is mapped to.
	 * 
	 * @return return the numeric value
	 */
	long getValue();
}
