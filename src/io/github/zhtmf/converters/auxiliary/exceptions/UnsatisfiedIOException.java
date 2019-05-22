package io.github.zhtmf.converters.auxiliary.exceptions;

import java.io.IOException;

/**
 * A simple subclass of {@link IOException} that enables
 * tracking of exact exceptions thrown for code coverage purposes.
 * 
 * @author dzh
 */
public class UnsatisfiedIOException extends IOException implements ExactException{
    private static final long serialVersionUID = 1L;
    private Class<?> site;
    private int ordinal;
    public UnsatisfiedIOException(String s) {
        this(s,null);
    }
    public UnsatisfiedIOException(String message, Throwable cause) {
        super(message, cause);
    }
    public Class<?> getSite() {
        return site;
    }
    public int getOrdinal() {
        return ordinal;
    }
    public IOException withSiteAndOrdinal(Class<?> site, int ordinal) {
        this.site = site;
        this.ordinal = ordinal;
        return this;
    }
}
