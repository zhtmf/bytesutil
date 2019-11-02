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
 * If for some reason the enum class cannot implement this interface, it should
 * return those numeric values as a string from <code>toString</code> methods of
 * each enum member.
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
