package org.dzh.bytesutil.annotations.enums;

/**
 * "Marker" interface used to indicates an enum-typed field is intended to be
 * mapped with numeric values.
 * <p>
 * The implementing enum class should return distinct value for each enum member and
 * those values should be within the defined range of numeric types annotations
 * ({@link BYTE}, {@link SHORT} etc.).
 * <p>
 * If for some reason the enum class cannot implement a {@link #getValue()}
 * method, it should return that numeric value as a string in
 * {@link #toString()} methods of each enum member.
 */
public interface NumericEnum {
	/**
	 * Return the numeric value which logically an enum member is mapped to.
	 * 
	 * @return return the numeric value
	 */
	long getValue();
}
