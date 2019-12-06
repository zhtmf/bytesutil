package io.github.zhtmf.annotations.enums;

/**
 * Interface used to make an enum-typed field eligible to be mapped to numeric
 * values.
 * <p>
 * The enum class implementing this interface should return distinct value for
 * each enum member from {@link #getValue()} and those values should be within
 * the defined range of numeric data type annotations (<code>BYTE</code>,
 * <code>SHORT</code>, etc.).
 * <p>
 * If for some reason the enum class cannot implement this interface, its
 * members will by default be mapped by their ordinal numbers, unless <b>all</b>
 * of them provide with reasonable <code>toString</code> method which returns a
 * numeric string for its associated number.
 * 
 * @author dzh
 */
public interface NumericEnum {
    /**
     * Return the number which logically this enum member is mapped to.
     * 
     * @return the numeric value
     */
    long getValue();
}
