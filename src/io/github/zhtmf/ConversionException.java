package io.github.zhtmf;

/**
 * <p>
 * General exception class for <tt>runtime</tt> error produced by this library.
 * <p>
 * Users can retrieve name of the class and the field which failed the
 * processing through {@link #getEntityClassName() getEntityClassName} and
 * {@link #getFieldName() getFieldName} for better exception handling.
 */
public class ConversionException extends Exception {
    private static final long serialVersionUID = 1L;
    private String entityClassName;
    private String fieldName;
    
    public ConversionException(Class<?> enclosingEntityClass,String fieldName, String msg) {
        super(String.format("Entity Class[%s], field [%s], error:[%s]",enclosingEntityClass, fieldName,msg));
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
     * Returns name of the entity class which caused this
     * error.
     * 
     * @return FQN of the entity class
     */
    public String getEntityClassName() {
        return entityClassName;
    }
    /**
     * Returns name of the field which caused this error.
     * @return name of the field
     */
    public String getFieldName() {
        return fieldName;
    }
    
    @Override
    public String toString() {
        return this.getMessage();
    }
}
