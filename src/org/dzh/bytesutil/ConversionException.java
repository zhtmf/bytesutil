package org.dzh.bytesutil;

import org.dzh.bytesutil.converters.auxiliary.FieldInfo;

/**
 * Generic Exception class for <tt>runtime</tt> error produced by this library.<br/>
 * This class contains information about the entity class and the field that failed the parsing.
 */
public class ConversionException extends Exception {
	private static final long serialVersionUID = 1L;
	private String entityClassName;
	private String fieldName;
	
	public ConversionException(FieldInfo ctx, String msg) {
		this(ctx.enclosingEntityClass, ctx.name, msg);
	}
	
	public ConversionException(FieldInfo ctx, Throwable cause) {
		this(ctx.enclosingEntityClass, ctx.name, cause);
	}
	
	public ConversionException(FieldInfo ctx, String msg, Throwable cause) {
		this(ctx.enclosingEntityClass, ctx.name, msg, cause);
	}
	
	public ConversionException(Class<?> enclosingEntityClass,String fieldName, String msg) {
		super(String.format("Entity Class[%s], field [%s], error:[%s]", enclosingEntityClass, fieldName,msg));
		this.entityClassName = enclosingEntityClass.getName();
		this.fieldName = fieldName;
	}
	
	public ConversionException(Class<?> enclosingEntityClass ,String fieldName, Throwable cause) {
		super(String.format("Entity Class[%s], field [%s], error:[%s]", enclosingEntityClass, fieldName,cause),cause);
		this.entityClassName = enclosingEntityClass.getName();
		this.fieldName = fieldName;
	}
	
	public ConversionException(Class<?> enclosingEntityClass, String fieldName, String msg, Throwable cause) {
		super(String.format("Entity Class[%s], field [%s], error:[%s]", enclosingEntityClass, fieldName,msg),cause);
		this.entityClassName = enclosingEntityClass.getName();
		this.fieldName = fieldName;
	}
	
	/**
	 * Returns name of the entity class which failed parsing and produces this error.
	 * 
	 * @return
	 */
	public String getEntityClassName() {
		return entityClassName;
	}
	/**
	 * Returns name of the field which failed parsing and produces this error.
	 * @return
	 */
	public String getFieldName() {
		return fieldName;
	}
	
	@Override
	public String toString() {
		return this.getMessage();
	}
}
