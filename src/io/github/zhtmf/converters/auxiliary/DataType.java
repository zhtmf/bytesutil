package io.github.zhtmf.converters.auxiliary;

import io.github.zhtmf.annotations.modifiers.Length;

/**
 * Abstract data types that can be handled by this library.
 * <p>
 * This enum is used by other modifier annotations, like
 * {@link Length#type()}
 * <p>
 * For concrete definitions of these types please refer to corresponding
 * annotations under package <code>io.github.zhtmf.annotations.types</code>
 * 
 * @author dzh
 */
public enum DataType{
    BYTE
    ,SHORT
    ,INT
    ,INT3
    ,INT5
    ,INT6
    ,INT7
    ,BCD
    ,RAW
    ,CHAR
    ,LONG
    ,USER_DEFINED
    ,BIT
    ,FIXED
}