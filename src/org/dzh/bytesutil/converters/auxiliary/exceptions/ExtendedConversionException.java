package org.dzh.bytesutil.converters.auxiliary.exceptions;

import org.dzh.bytesutil.ConversionException;
import org.dzh.bytesutil.converters.auxiliary.FieldInfo;

/**
 * Sub class for code coverage purposes.
 * @author dzh
 */
public class ExtendedConversionException extends ConversionException implements ExactException{
    private static final long serialVersionUID = 1L;
    private Class<?> site;
    private int ordinal;
    public ExtendedConversionException(Class<?> enclosingEntityClass, String fieldName, String msg, Throwable cause) {
        super(enclosingEntityClass, fieldName, msg, cause);
    }
    public ExtendedConversionException(Class<?> enclosingEntityClass, String fieldName, String msg) {
        super(enclosingEntityClass, fieldName, msg);
    }
    public ExtendedConversionException(Class<?> enclosingEntityClass, String fieldName, Throwable cause) {
        super(enclosingEntityClass, fieldName, cause);
    }
    public ExtendedConversionException(FieldInfo ctx, String msg, Throwable cause) {
        super(ctx, msg, cause);
    }
    public ExtendedConversionException(FieldInfo ctx, String msg) {
        super(ctx, msg);
    }
    public ConversionException withSiteAndOrdinal(Class<?> site, int ordinal) {
        this.site = site;
        this.ordinal = ordinal;
        return this;
    }
    @Override
    public Class<?> getSite() {
        return site;
    }
    @Override
    public int getOrdinal() {
        return ordinal;
    }
}
