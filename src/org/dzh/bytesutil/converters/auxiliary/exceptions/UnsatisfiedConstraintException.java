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
	public UnsatisfiedConstraintException(String s, Class<?> site, int ordinal) {
		this(s,null,site,ordinal);
	}
	public UnsatisfiedConstraintException(Throwable cause, Class<?> site, int ordinal) {
		this(null,cause,site,ordinal);
	}
	public UnsatisfiedConstraintException(String message, Throwable cause, Class<?> site, int ordinal) {
		super(message, cause);
		this.site = site;
		this.ordinal = ordinal;
	}
	public Class<?> getSite() {
		return site;
	}
	public int getOrdinal() {
		return ordinal;
	}
}
