package io.github.zhtmf.converters;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.github.zhtmf.ConversionException;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.ListLength;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.types.BCD;
import io.github.zhtmf.annotations.types.CHAR;
import io.github.zhtmf.annotations.types.RAW;
import io.github.zhtmf.annotations.types.UserDefined;
import io.github.zhtmf.converters.auxiliary.DataType;
import io.github.zhtmf.converters.auxiliary.exceptions.ExtendedConversionException;
import io.github.zhtmf.converters.auxiliary.exceptions.UnsatisfiedConstraintException;

/**
 * Internal class that records info about annotations, fields of a specific
 * class for quick access, should not be used in client code.
 * <p>
 * This class itself is thread-safe after construction
 * @author dzh
 */
class ClassInfo {
    
    final Class<?> entityClass;
    
    /**
     * Annotations that are present on the class (on the dataType)
     */
    private Map<Class<? extends Annotation>, Annotation> globalAnnotations = new HashMap<>();
    /**
     * {@link FieldInfo} objects in the order specified by {@link Order} annotation.
     */
    List<FieldInfo> fieldInfoList = new ArrayList<>();
    
    public ClassInfo(Class<?> cls) {
        
        if(cls==null || ! DataPacket.class.isAssignableFrom(cls)) {
            throw FieldInfo.forContext(cls, null, "should be a class which inherits DataPacket")
                .withSiteAndOrdinal(ClassInfo.class, 0);
        }
        
        this.entityClass = cls;
        
        for(Annotation an:cls.getAnnotations()) {
            globalAnnotations.put(an.annotationType(), an);
        }
        
        List<Field> fieldList = new ArrayList<>();
        /*
         * recursively finds all fields of this class and its super classes. make sure
         * fields in subclasses always appears later in the list than those from super
         * class.
         */
        Class<?> tmp = cls;
        while(true){
            List<Field> tmpList = new ArrayList<>(Arrays.asList(tmp.getDeclaredFields()));
            for(int i=0;i<tmpList.size();++i) {
                Field f = tmpList.get(i);
                /*
                 * ignore any fields that are not annotated by Order annotation
                 * and any fields that are static or final
                 */
                int mod = f.getModifiers();
                if(f.getAnnotation(Order.class)==null
                || (mod & Modifier.STATIC)!=0
                || (mod & Modifier.FINAL)!=0) {
                    tmpList.remove(i);
                    --i;
                }
                f.setAccessible(true);
            }
            
            Collections.sort(tmpList, reverseFieldComparator);
            fieldList.addAll(tmpList);
            tmp = tmp.getSuperclass();
            if(tmp == DataPacket.class) {
                break;
            }
        }
        
        Collections.reverse(fieldList);
        
        for(int i=0;i<fieldList.size();++i) {
            
            Field f = fieldList.get(i);
            
            String name = f.getName();
            
            /*
             * check whether a DataType is defined on this field, as is required by
             * serialization. If none, check whether this field itself is a
             * Data(entity), or is a List of Data. If all of these checks
             * fail, throw an exception
             */
            DataType type = null;
            for(DataType dataType:DataType.values()) {
                DataTypeOperations tp = DataTypeOperations.of(dataType);
                if(f.getAnnotation(tp.annotationClassOfThisType())!=null) {
                    if(type!=null) {
                        throw FieldInfo.forContext(cls, name, "multiple data DataType declaration on same field is not allowed")
                            .withSiteAndOrdinal(ClassInfo.class, 1);
                    }
                    type = dataType;
                }
            }
            if(type==null){
                Class<?> componentClass;
                if( ! DataPacket.class.isAssignableFrom(f.getType())
                && ((componentClass = firstTypeParameterClass(f))==null
                || ! DataPacket.class.isAssignableFrom(componentClass))) {
                    throw FieldInfo.forContext(cls, name, "field not marked with a DataType")
                        .withSiteAndOrdinal(ClassInfo.class, 2);
                }
            }
            
            if(type == DataType.BCD) {
                BCD anno = f.getAnnotation(BCD.class);
                if(anno.value()<0) {
                    throw FieldInfo.forContext(cls, name, "BCD length should not be negative")
                        .withSiteAndOrdinal(ClassInfo.class, 3);
                }
            }
            
            FieldInfo fieldInfo = f.getType().isEnum()
                    && f.getAnnotation(UserDefined.class)==null
                    ? new EnumFieldInfo(f, type, this)
                    : new FieldInfo(f,type,this);
            
            if(fieldInfo.listComponentClass!=null) {
                if(fieldInfo.localAnnotation(Length.class)==null
                        && fieldInfo.localAnnotation(ListLength.class)==null) {
                    throw FieldInfo.forContext(cls, name, "neither Length nor ListLength annotation are present")
                        .withSiteAndOrdinal(ClassInfo.class, 4);
                }
                if(((fieldInfo.dataType == DataType.RAW && fieldInfo.localAnnotation(RAW.class).value()<0)
                || (fieldInfo.dataType == DataType.CHAR && fieldInfo.localAnnotation(CHAR.class).value()<0)
                || (fieldInfo.dataType == DataType.USER_DEFINED && fieldInfo.localAnnotation(UserDefined.class).length()<0))
                        && fieldInfo.localAnnotation(ListLength.class)==null) {
                    throw FieldInfo.forContext(cls, name, "this field is a list of type that utilizes @Length, "
                            + "to avoid ambiguity, use @ListLength but not @Length to specify the list length")
                        .withSiteAndOrdinal(ClassInfo.class, 5);
                }
            }
            
            //either specify a positive value property or use a Length annotation to 
            //declare the length
            {
                CHAR ch = fieldInfo.localAnnotation(CHAR.class);
                if(ch!=null) {
                    if(ch.value()<0 &&  ! fieldInfo.lengthDefined && fieldInfo.endsWith==null) {
                        throw FieldInfo.forContext(cls, name, "this field is defined as CHAR, but its value property is negative"
                                + " and a Length annotation is not present on it")
                        .withSiteAndOrdinal(ClassInfo.class, 6);
                    }
                    if(fieldInfo.endsWith!=null) {
                        if(ch.value()>0 || fieldInfo.lengthDefined) {
                            throw FieldInfo.forContext(cls, name, "length of CHAR fields can only be specified by"
                                    + "one of value property, Length annotation or EndsWith")
                            .withSiteAndOrdinal(ClassInfo.class, 10);
                        }
                    }
                    if(fieldInfo.listComponentClass==null
                   && (ch.value()>=0 && fieldInfo.customLengthDefined)) {
                        throw FieldInfo.forContext(cls, name, "length of CHAR fields can only be specified by"
                                    + " one of value property, Length annotation or EndsWith")
                        .withSiteAndOrdinal(ClassInfo.class, 11);
                    }
                }
            }
            
            RAW raw = fieldInfo.localAnnotation(RAW.class);
            if(raw!=null && raw.value()<0) {
                if( ! fieldInfo.lengthDefined)
                    throw FieldInfo.forContext(cls, name, "this field is defined as RAW, but its value property is negative"
                            + " and a Length annotation is not present on it")
                        .withSiteAndOrdinal(ClassInfo.class, 7);
            }
            
            UserDefined ud = fieldInfo.localAnnotation(UserDefined.class);
            if(ud!=null && ud.length()<0) {
                if( ! fieldInfo.lengthDefined)
                    throw FieldInfo.forContext(cls, name, "this field is defined as UserDefined"
                            + ", but its value property is negative"
                            + " and a Length annotation is not present on it")
                        .withSiteAndOrdinal(ClassInfo.class, 9);
            }
            
            fieldInfoList.add(fieldInfo);
        }
        
        fieldInfoList = Collections.unmodifiableList(fieldInfoList);
    }
    
    @SuppressWarnings("unchecked")
    <T extends Annotation> T globalAnnotation(Class<T> annoCls) {
        return (T) globalAnnotations.get(annoCls);
    }
    
    //return null for non-generic field definitions
    static Class<?> firstTypeParameterClass(Field field){
        Type type = field.getGenericType();
        if( ! (type instanceof ParameterizedType)) {
            return null;
        }
        Type[] types = ((ParameterizedType)type).getActualTypeArguments();
        return (Class<?>)types[0];
    }
    
    private static final Comparator<Field> reverseFieldComparator = new Comparator<Field>() {
        @Override
        public int compare(Field o1, Field o2) {
            int val1 = o1.getAnnotation(Order.class).value();
            int val2 = o2.getAnnotation(Order.class).value();
            if(val1==val2) {
                throw FieldInfo.forContext(null, o1.getName()+"/"+o2.getName(), "two fields with same order value")
                    .withSiteAndOrdinal(ClassInfo.class, 8);
            }
            /*
             * sort them reversely there to prevent inserting into the beginning
             */
            return -(val1 - val2);
        }
        
    };
    
    //----------methods and fields originally defined in DataPacket-----------
    
    //thread-safe map of class info objects
    private static final ConcurrentHashMap<Class<?>,ClassInfo> 
    classInfoMap = new ConcurrentHashMap<>();
    //lazy initialization
    private static ClassInfo getClassInfo(Object entity) {
        Class<?> self = entity.getClass();
        ClassInfo ci = classInfoMap.get(self);
        if(ci==null) {
            //may suffer from duplicated creating
            //but the penalty is trivial 
            ci = new ClassInfo(self);
            classInfoMap.put(self, ci);
        }
        return ci;
    }
    
    public static void serialize(Object self,OutputStream dest)
            throws ConversionException, IllegalArgumentException{
        if(dest==null) {
            throw new NullPointerException();
        }
        
        //lazy initialization
        ClassInfo ci = getClassInfo(self);
        
        for(FieldInfo ctx:ci.fieldInfoList) {
            
            if(ctx.shouldSkipFieldForSerializing(self))
                continue;
            
            Object value = ctx.get(self);
            if(value==null) {
                /*
                 * null values shall not be permitted as it may be impossible 
                 * to deserialize the byte sequence generated
                 * Note: this modification causes incompatibility with former releases
                 */
                throw new ExtendedConversionException(self.getClass(),ctx.name,
                        "this field is intended to be processed but its value is null")
                        .withSiteAndOrdinal(DataPacket.class, 0);
            }
            
            try {
                @SuppressWarnings("unchecked")
                Converter<Object> cv = (Converter<Object>)ctx.converter;
                cv.serialize(value, dest, ctx, self);
            } catch(ConversionException e) {
                throw e;
            } catch (Exception e) {
                throw new ExtendedConversionException(self.getClass(),ctx.name,e)
                .withSiteAndOrdinal(DataPacket.class, 4);
            }
        }
    }
    
    public static void deserialize(Object self,InputStream src) throws ConversionException, IllegalArgumentException {
        if(src==null) {
            throw new NullPointerException();
        }
        InputStream _src = MarkableInputStream.wrap(src);
        ClassInfo ci = getClassInfo(self);
        for(FieldInfo ctx:ci.fieldInfoList) {
            Object value = null;
            @SuppressWarnings("unchecked")
            Converter<Object> cv = (Converter<Object>)ctx.converter;
            try {
                value = cv.deserialize(_src, ctx, self);
            } catch(ConversionException e) {
                throw e;
            } catch (Exception e) {
                throw new ExtendedConversionException(self.getClass(),ctx.name,e)
                        .withSiteAndOrdinal(DataPacket.class, 14);
            }
            ctx.set(self, value);
        }
    }
    
    public static int length(Object self) throws IllegalArgumentException{
        ClassInfo ci = getClassInfo(self);
        int ret = 0;
        for(FieldInfo ctx:ci.fieldInfoList) {
            ret += calculateFieldLength(ctx, self);
        }
        return ret;
    }
    
    private static int calculateFieldLength(FieldInfo ctx,Object self) {
        if(ctx.shouldSkipFieldForSerializing(self)) {
            return 0;
        }
        Object value = ctx.get(self);
        if(value==null) {
            throw new UnsatisfiedConstraintException(
                    ctx.name + " is intended to be processed but its value is null")
                    .withSiteAndOrdinal(DataPacket.class, 20);
                    
        }
        if(ctx.isEntity) {
            DataPacket dp = (DataPacket)value;
            return dp.length();
        }
        int ret = 0;
        int length = 0;
        if(ctx.listComponentClass!=null) {
            length = ctx.lengthForList(self);
            @SuppressWarnings("rawtypes")
            List lst = (List)value;
            if(length<0) {
                //write ahead
                //size of the write-ahead length should be considered
                //even the list itself is null or empty
                ret += DataTypeOperations.of(ctx.lengthType()).size();
                //use the defined length rather than the actual list size
                length = lst.size();
            }
            if(ctx.isEntityList) {
                for(int i=0;i<length;++i) {
                    ret += ((DataPacket)lst.get(i)).length();
                }
                return ret;
            }
        }else {
            length = 1;
        }
        DataType type = ctx.dataType;
        switch(type) {
        case BCD:
            ret += ((BCD)ctx.localAnnotation(BCD.class)).value() * length;
            break;
        case BYTE:
        case SHORT:
        case INT:
        case LONG:
        case INT3:
        case INT5:
        case INT6:
        case INT7:
            ret += DataTypeOperations.of(type).size() * length;
            break;
        case CHAR:{
            int size = ctx.lengthForSerializingCHAR(self);
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
                    size += DataTypeOperations.of(ctx.annotation(Length.class).type()).size();
                    size += FieldInfo.getThreadLocalDateFormatter(ctx.datePattern).format((Date)value).length();
                }else {
                    Charset cs = ctx.charsetForSerializingCHAR(self);
                    int fixedOverHead;
                    if(ctx.endsWith!=null) {
                        fixedOverHead = ctx.endsWith.length;
                    }else {
                        fixedOverHead = DataTypeOperations.of(ctx.annotation(Length.class).type()).size();
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
            int size = ctx.lengthForSerializingRAW(self);
            if(size>=0) {
                ret += size * length;
            }else {
                size = 0;
                DataTypeOperations lengthType = DataTypeOperations.of(ctx.annotation(Length.class).type());
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
            int size = ctx.lengthForSerializingUserDefinedType(self);
            ret += size * length;
            break;
        }
        return ret;
    }
    
    //---------lang access for this package------------
    
    static {
        
        DataPacket.setAuxiliaryAccess(new DataPacket.AuxiliaryAccess() {
            public void serialize(Object self,OutputStream dest)
                    throws ConversionException, IllegalArgumentException{
                ClassInfo.serialize(self, dest);
            }
            public void deserialize(Object self,InputStream src)
                    throws ConversionException, IllegalArgumentException{
                ClassInfo.deserialize(self, src);
            }
            
            public int length(Object self) throws IllegalArgumentException{
                return ClassInfo.length(self);
            }
        });
    }
}