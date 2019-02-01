package org.dzh.bytesutil.converters.auxiliary.exceptions;

/**
 * A simple subclass of {@link IllegalArgumentException} that enables
 * tracking of exact exceptions thrown for code coverage purposes.
 * 
 * @author dzh
 */
public class UnsatisfiedConstraintException extends IllegalArgumentException implements ExactException{
	private static final long serialVersionUID = 1L;
	private Class<?> site;
	private int ordinal;
	public UnsatisfiedConstraintException(String s) {
		this(s,null);
	}
	public UnsatisfiedConstraintException(Throwable cause) {
		this(null,cause);
	}
	public UnsatisfiedConstraintException(String message, Throwable cause) {
		super(message, cause);
	}
	public Class<?> getSite() {
		return site;
	}
	public int getOrdinal() {
		return ordinal;
	}
	public IllegalArgumentException withSiteAndOrdinal(Class<?> site, int ordinal) {
		this.site = site;
		this.ordinal = ordinal;
		return this;
	}
}
