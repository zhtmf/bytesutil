package io.github.zhtmf;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.types.BCD;
import io.github.zhtmf.converters.Converter;
import io.github.zhtmf.converters.auxiliary.ClassInfo;
import io.github.zhtmf.converters.auxiliary.DataType;
import io.github.zhtmf.converters.auxiliary.FieldInfo;
import io.github.zhtmf.converters.auxiliary.MarkableInputStream;
import io.github.zhtmf.converters.auxiliary.Utils;
import io.github.zhtmf.converters.auxiliary.exceptions.ExtendedConversionException;
import io.github.zhtmf.converters.auxiliary.exceptions.UnsatisfiedConstraintException;

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
        
        for(FieldInfo fi:ci.fieldInfoList()) {
            
            Object value = fi.get(this);
            if(value==null) {
                /*
                 * null values shall not be permitted as it may be impossible 
                 * to deserialize the byte sequence generated
                 * Note: this modification causes incompatibility with former releases
                 */
                throw new ExtendedConversionException(this.getClass(),fi.name,
                        "this field is intended to be processed but its value is null")
                        .withSiteAndOrdinal(DataPacket.class, 0);
            }
            
            try {
                @SuppressWarnings("unchecked")
                Converter<Object> cv = (Converter<Object>)fi.converter;
                cv.serialize(value, dest, fi, this);
            } catch(ConversionException e) {
                throw e;
            } catch (Exception e) {
                throw new ExtendedConversionException(this.getClass(),fi.name,e)
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
        deserialize0(new MarkableInputStream(src));
    }
    
    private void deserialize0(MarkableInputStream _src) throws ConversionException {
        ClassInfo ci = getClassInfo();
        for(FieldInfo fi:ci.fieldInfoList()) {
            Object value = null;
            @SuppressWarnings("unchecked")
            Converter<Object> cv = (Converter<Object>)fi.converter;
            try {
                value = cv.deserialize(_src, fi, this);
            } catch(ConversionException e) {
                throw e;
            } catch (Exception e) {
                throw new ExtendedConversionException(this.getClass(),fi.name,e)
                        .withSiteAndOrdinal(DataPacket.class, 14);
            }
            fi.set(this, value);
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
        for(FieldInfo fi:ci.fieldInfoList()) {
            if(fi.conditionalHandler!=null
            && fi.conditionalHandler.handleSerialize(fi.name, this).equals(Boolean.FALSE)) {
                continue;
            }
            Object value = fi.get(this);
            if(value==null) {
                throw new UnsatisfiedConstraintException(
                        fi.name + " is intended to be processed but its value is null")
                        .withSiteAndOrdinal(DataPacket.class, 20);
                        
            }
            if(fi.isEntity) {
                DataPacket dp = (DataPacket)value;
                ret += dp.length();
                continue;
                
            }
            int length = 0;
            if(fi.listComponentClass!=null) {
                length = Utils.lengthForList(fi, this);
                @SuppressWarnings("rawtypes")
                List lst = (List)value;
                if(length<0) {
                    //write ahead
                    //size of the write-ahead length should be considered
                    //even the list itself is null or empty
                    ret += fi.lengthType().size();
                    //use the defined length rather than the actual list size
                    length = lst.size();
                }
                if(fi.isEntityList) {
                    for(int i=0;i<length;++i) {
                        ret += ((DataPacket)lst.get(i)).length();
                    }
                    continue;
                }
            }else {
                length = 1;
            }
            DataType type = fi.dataType;
            switch(type) {
            case BCD:
                ret += ((BCD)fi.localAnnotation(BCD.class)).value() * length;
                break;
            case BYTE:
            case SHORT:
            case INT:
            case LONG:
            case INT3:
            case INT5:
            case INT6:
            case INT7:
                ret += type.size() * length;
                break;
            case CHAR:{
                int size = Utils.lengthForSerializingCHAR(fi, this);
                if(size>=0) {
                    //explicitly declared size
                    ret += size * length;
                }else {
                    //dynamic length written to stream prior to serializing
                    //or strings terminated by specific sequence of bytes
                    //size should be retrieved by inspecting the value itself
                    //or in case of a list, inspecting values for EACH element
                    size = 0;
                    if(value instanceof Date) {
                        size += fi.annotation(Length.class).type().size();
                        size += Utils.getThreadLocalDateFormatter(fi.datePattern).format((Date)value).length();
                    }else {
                        Charset cs = Utils.charsetForSerializingCHAR(fi, this);
                        int fixedOverHead;
                        if(fi.endsWith!=null) {
                            fixedOverHead = fi.endsWith.length;
                        }else {
                            fixedOverHead = fi.annotation(Length.class).type().size();
                        }
                        if(value instanceof List) {
                            @SuppressWarnings("rawtypes")
                            List lst = (List)value;
                            int lstSize = lst.size();
                            size += fixedOverHead*lstSize;
                            for(int i=0;i<lstSize;++i) {
                                size += lst.get(i).toString().getBytes(cs).length;
                            }
                        }else {
                            size += fixedOverHead;
                            size += value.toString().getBytes(cs).length;
                        }
                    }
                    ret += size;
                }
                break;
            }
            case RAW:{
                int size = Utils.lengthForSerializingRAW(fi, this);
                if(size>=0) {
                    ret += size * length;
                }else {
                    size = 0;
                    DataType lengthType = fi.annotation(Length.class).type();
                    if(value instanceof List) {
                        @SuppressWarnings("rawtypes")
                        List lst = (List)value;
                        for(int i=0;i<lst.size();++i) {
                            value = lst.get(i);
                            size += lengthType.size();
                            size += Array.getLength(value);
                        }
                    }else {
                        size += lengthType.size();
                        size += Array.getLength(value);
                    }
                    ret += size;
                }
                break;
            }
            case USER_DEFINED:
                int size = Utils.lengthForSerializingUserDefinedType(fi, this);
                ret += size * length;
                break;
            }
        }
        return ret;
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
