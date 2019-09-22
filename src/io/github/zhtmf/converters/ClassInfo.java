package io.github.zhtmf.converters;

import java.io.InputStream;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.ListLength;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.types.BCD;
import io.github.zhtmf.annotations.types.CHAR;
import io.github.zhtmf.annotations.types.RAW;
import io.github.zhtmf.annotations.types.UserDefined;
import io.github.zhtmf.converters.auxiliary.DataType;
import io.github.zhtmf.converters.auxiliary.exceptions.UnsatisfiedConstraintException;

/**
 * Internal class that records info about annotations, fields of a specific
 * class for quick access, should not be used in client code.
 * <p>
 * This class itself is thread-safe after construction
 * @author dzh
 */
public class ClassInfo {
    
    final Class<?> entityClass;
    
    /**
     * Annotations that are present on the class (on the dataType)
     */
    private Map<Class<? extends Annotation>, Annotation> globalAnnotations = new HashMap<>();
    /**
     * {@link FieldInfo} objects that are used internally, in the order specified by
     * {@link Order} annotation.
     */
    private Map<String,FieldInfo> fieldInfoByField = new LinkedHashMap<>();
    
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
            
            FieldInfo fi = f.getType().isEnum()
                    && f.getAnnotation(UserDefined.class)==null
                    ? new EnumFieldInfo(f, type, this)
                    : new FieldInfo(f,type,this);
            
            if(fi.listComponentClass!=null) {
                if(fi.localAnnotation(Length.class)==null
                        && fi.localAnnotation(ListLength.class)==null) {
                    throw FieldInfo.forContext(cls, name, "neither Length nor ListLength annotation are present")
                        .withSiteAndOrdinal(ClassInfo.class, 4);
                }
                if(((fi.dataType == DataType.RAW && fi.localAnnotation(RAW.class).value()<0)
                || (fi.dataType == DataType.CHAR && fi.localAnnotation(CHAR.class).value()<0)
                || (fi.dataType == DataType.USER_DEFINED && fi.localAnnotation(UserDefined.class).length()<0))
                        && fi.localAnnotation(ListLength.class)==null) {
                    throw FieldInfo.forContext(cls, name, "this field is a list of type that utilizes @Length, "
                            + "to avoid ambiguity, use @ListLength but not @Length to specify the list length")
                        .withSiteAndOrdinal(ClassInfo.class, 5);
                }
            }
            
            //either specify a positive value property or use a Length annotation to 
            //declare the length
            {
                CHAR ch = fi.localAnnotation(CHAR.class);
                if(ch!=null) {
                    if(ch.value()<0 &&  ! fi.lengthDefined && fi.endsWith==null) {
                        throw FieldInfo.forContext(cls, name, "this field is defined as CHAR, but its value property is negative"
                                + " and a Length annotation is not present on it")
                        .withSiteAndOrdinal(ClassInfo.class, 6);
                    }
                    if(fi.endsWith!=null) {
                        if(ch.value()>0 || fi.lengthDefined) {
                            throw FieldInfo.forContext(cls, name, "length of CHAR fields can only be specified by"
                                    + "one of value property, Length annotation or EndsWith")
                            .withSiteAndOrdinal(ClassInfo.class, 10);
                        }
                    }
                    if(fi.listComponentClass==null
                   && (ch.value()>=0 && fi.customLengthDefined)) {
                        throw FieldInfo.forContext(cls, name, "length of CHAR fields can only be specified by"
                                    + " one of value property, Length annotation or EndsWith")
                        .withSiteAndOrdinal(ClassInfo.class, 11);
                    }
                }
            }
            
            RAW raw = fi.localAnnotation(RAW.class);
            if(raw!=null && raw.value()<0) {
                if( ! fi.lengthDefined)
                    throw FieldInfo.forContext(cls, name, "this field is defined as RAW, but its value property is negative"
                            + " and a Length annotation is not present on it")
                        .withSiteAndOrdinal(ClassInfo.class, 7);
            }
            
            UserDefined ud = fi.localAnnotation(UserDefined.class);
            if(ud!=null && ud.length()<0) {
                if( ! fi.lengthDefined)
                    throw FieldInfo.forContext(cls, name, "this field is defined as UserDefined"
                            + ", but its value property is negative"
                            + " and a Length annotation is not present on it")
                        .withSiteAndOrdinal(ClassInfo.class, 9);
            }
            
            fieldInfoByField.put(name, fi);
        }
    }
    
    /**
     * Get a <b>copy</b> of FieldInfo list
     * @return  a <b>copy</b> of FieldInfo list
     */
    public List<FieldInfo> fieldInfoList() {
        return new ArrayList<>(fieldInfoByField.values());
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
    
    static {
        DataPacket.setAuxiliaryAccess(new DataPacket.AuxiliaryAccess() {
            
            @Override
            public InputStream wrap(InputStream in) {
                return MarkableInputStream.wrap(in);
            }
            
            @Override
            public int calculateFieldLength(FieldInfo fi,Object self) {
                if(fi.shouldSkipFieldForSerializing(self)) {
                    return 0;
                }
                Object value = fi.get(self);
                if(value==null) {
                    throw new UnsatisfiedConstraintException(
                            fi.name + " is intended to be processed but its value is null")
                            .withSiteAndOrdinal(DataPacket.class, 20);
                            
                }
                if(fi.isEntity) {
                    DataPacket dp = (DataPacket)value;
                    return dp.length();
                }
                int ret = 0;
                int length = 0;
                if(fi.listComponentClass!=null) {
                    length = fi.lengthForList(self);
                    @SuppressWarnings("rawtypes")
                    List lst = (List)value;
                    if(length<0) {
                        //write ahead
                        //size of the write-ahead length should be considered
                        //even the list itself is null or empty
                        ret += DataTypeOperations.of(fi.lengthType()).size();
                        //use the defined length rather than the actual list size
                        length = lst.size();
                    }
                    if(fi.isEntityList) {
                        for(int i=0;i<length;++i) {
                            ret += ((DataPacket)lst.get(i)).length();
                        }
                        return ret;
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
                    ret += DataTypeOperations.of(type).size() * length;
                    break;
                case CHAR:{
                    int size = fi.lengthForSerializingCHAR(self);
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
                            size += DataTypeOperations.of(fi.annotation(Length.class).type()).size();
                            size += FieldInfo.getThreadLocalDateFormatter(fi.datePattern).format((Date)value).length();
                        }else {
                            Charset cs = fi.charsetForSerializingCHAR(self);
                            int fixedOverHead;
                            if(fi.endsWith!=null) {
                                fixedOverHead = fi.endsWith.length;
                            }else {
                                fixedOverHead = DataTypeOperations.of(fi.annotation(Length.class).type()).size();
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
                    int size = fi.lengthForSerializingRAW(self);
                    if(size>=0) {
                        ret += size * length;
                    }else {
                        size = 0;
                        DataTypeOperations lengthType = DataTypeOperations.of(fi.annotation(Length.class).type());
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
                    int size = fi.lengthForSerializingUserDefinedType(self);
                    ret += size * length;
                    break;
                }
                return ret;
            }

            @Override
            public boolean shouldSkipFieldForSerializing(FieldInfo fi, Object self) {
                return fi.shouldSkipFieldForSerializing(self);
            }
        });
    }
}