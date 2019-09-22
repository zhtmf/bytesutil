package io.github.zhtmf;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentHashMap;

import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.converters.ClassInfo;
import io.github.zhtmf.converters.Converter;
import io.github.zhtmf.converters.FieldInfo;
import io.github.zhtmf.converters.auxiliary.exceptions.ExtendedConversionException;

/**
 * <p>
 * Main class of this library. Typical steps of using this library are:
 * <ol>
 * <li>Make a custom Java class inherit this class (which is referred to as
 * "entity class" in terms of this library).</li>
 * <li>Use annotations under {@code annotations} package to define the binary
 * protocol (scheme) that the entity class intends to implement.</li>
 * <li>Use the three methods inherited ({@link #serialize(OutputStream)
 * serialize}, {@link #deserialize(InputStream) deserialize} and
 * {@link #length() length}) to convert instances of the entity class to/from
 * byte streams.</li>
 * </ol>
 * <p>
 * Classes resides in the same package as this class, as well as
 * annotations/classes in the {@code annotations} package are part of public
 * interface. Any other classes/interfaces are considered private and should not
 * be used by client codes.
 * <p>
 * The three methods in this class and this class itself is thread-safe. It is
 * declared as abstract only to remind users that it should be subclassed but
 * not used alone.
 * 
 * @author dzh
 *
 */
public abstract class DataPacket {
    
    //thread-safe map of class info objects
    private static final ConcurrentHashMap<Class<?>,ClassInfo> 
        classInfoMap = new ConcurrentHashMap<>();
    
    /**
     * <p>
     * Serialize entity class into the specified output stream.
     * <p>
     * Non-final, non-static fields annotated with {@link Order} are processed while
     * other fields are effectively ignored. Fields are processed according to their
     * ascending order as specified by {@link Order#value()}.
     * 
     * @param dest
     *            destination stream of serialization
     * @throws ConversionException
     *             If invalid input encountered during runtime.
     * @throws IllegalArgumentException
     *             If initial parsing of annotations on entity class or fields
     *             failed, this exception should be eliminated during compile time
     *             but not caught and handled during runtime.
     * @throws NullPointerException
     *             if <tt>dest</tt> is null.
     */
    public void serialize(OutputStream dest) throws ConversionException, IllegalArgumentException{
        if(dest==null) {
            throw new NullPointerException();
        }
        
        //lazy initialization
        ClassInfo ci = getClassInfo();
        
        for(FieldInfo ctx:ci.fieldInfoList()) {
            
            if(auxiliaryAccess.shouldSkipFieldForSerializing(ctx, this))
                continue;
            
            Object value = ctx.get(this);
            if(value==null) {
                /*
                 * null values shall not be permitted as it may be impossible 
                 * to deserialize the byte sequence generated
                 * Note: this modification causes incompatibility with former releases
                 */
                throw new ExtendedConversionException(this.getClass(),ctx.name,
                        "this field is intended to be processed but its value is null")
                        .withSiteAndOrdinal(DataPacket.class, 0);
            }
            
            try {
                @SuppressWarnings("unchecked")
                Converter<Object> cv = (Converter<Object>)ctx.converter;
                cv.serialize(value, dest, ctx, this);
            } catch(ConversionException e) {
                throw e;
            } catch (Exception e) {
                throw new ExtendedConversionException(this.getClass(),ctx.name,e)
                .withSiteAndOrdinal(DataPacket.class, 4);
            }
        }
    }
    
    /**
     * <p>
     * Deserialize entity class from an input stream.
     * <p>
     * Non-final, non-static fields annotated with {@link Order} are processed while
     * other fields are effectively ignored. Fields are processed according to their
     * ascending order as specified by {@link Order#value()}.
     * <p>
     * Fields declared as subclass of {@link DataPacket} should declare a no-arg
     * constructor and that dataType should be accessible (not a non-static inner
     * class or a private inner class).
     * <p>
     * Such fields will always be assigned with newly created objects after a
     * successful deserialization. Any value associated with them before will be
     * overwritten.
     * 
     * @param src
     *            the input stream
     * @throws ConversionException
     *             If invalid input encountered during runtime.
     * @throws IllegalArgumentException
     *             If initial parsing of annotations on entity class or fields
     *             failed, this exception should be eliminated during compile time
     *             but not caught and handled during runtime.
     * @throws NullPointerException
     *             if <tt>src</tt> is null.
     */
    public void deserialize(InputStream src) throws ConversionException, IllegalArgumentException {
        if(src==null) {
            throw new NullPointerException();
        }
        deserialize0(auxiliaryAccess.wrap(src));
    }
    
    private void deserialize0(InputStream _src) throws ConversionException {
        ClassInfo ci = getClassInfo();
        for(FieldInfo ctx:ci.fieldInfoList()) {
            Object value = null;
            @SuppressWarnings("unchecked")
            Converter<Object> cv = (Converter<Object>)ctx.converter;
            try {
                value = cv.deserialize(_src, ctx, this);
            } catch(ConversionException e) {
                throw e;
            } catch (Exception e) {
                throw new ExtendedConversionException(this.getClass(),ctx.name,e)
                        .withSiteAndOrdinal(DataPacket.class, 14);
            }
            ctx.set(this, value);
        }
    }

    /**
     * Calculate the length in bytes of this object as if it was serialized to an
     * output stream.
     * <p>
     * This is <b>NOT</b> a constant time operation as the actual length should and
     * can only be calculated at runtime.
     * 
     * @throws IllegalArgumentException
     *             If initial parsing failed or other preliminaries not satisfied.
     *             Some errors may be better expressed by a
     *             {@link ConversionException} but we use an
     *             {@link IllegalArgumentException} to keep compatible with old
     *             versions;
     * 
     * @return length in bytes
     */
    public int length() throws IllegalArgumentException{
        ClassInfo ci = getClassInfo();
        int ret = 0;
        for(FieldInfo ctx:ci.fieldInfoList()) {
            ret += auxiliaryAccess.calculateFieldLength(ctx, this);
        }
        return ret;
    }
    
    public static interface AuxiliaryAccess{
        InputStream wrap(InputStream in);
        int calculateFieldLength(FieldInfo ctx,Object self);
        boolean shouldSkipFieldForSerializing(FieldInfo ctx, Object self);
    }
    private static AuxiliaryAccess auxiliaryAccess;
    public static final void setAuxiliaryAccess(AuxiliaryAccess auxiliaryAccess) {
        DataPacket.auxiliaryAccess = auxiliaryAccess;
    }
    
    static {
        try {
            Class.forName("io.github.zhtmf.converters.ClassInfo");
        } catch (ClassNotFoundException e) {
            throw new Error(e);
        }
    }
    
    //lazy initialization
    private ClassInfo getClassInfo() {
        Class<?> self = this.getClass();
        ClassInfo ci = classInfoMap.get(self);
        if(ci==null) {
            //may suffer from duplicated creating
            //but the penalty is trivial 
            ci = new ClassInfo(self);
            classInfoMap.put(self, ci);
        }
        return ci;
    }
}
