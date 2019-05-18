package org.dzh.bytesutil.converters.auxiliary;

import static org.dzh.bytesutil.converters.auxiliary.Utils.forContext;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.TypeConverter;
import org.dzh.bytesutil.annotations.modifiers.BigEndian;
import org.dzh.bytesutil.annotations.modifiers.CHARSET;
import org.dzh.bytesutil.annotations.modifiers.DatePattern;
import org.dzh.bytesutil.annotations.modifiers.EndsWith;
import org.dzh.bytesutil.annotations.modifiers.Length;
import org.dzh.bytesutil.annotations.modifiers.ListLength;
import org.dzh.bytesutil.annotations.modifiers.LittleEndian;
import org.dzh.bytesutil.annotations.modifiers.Signed;
import org.dzh.bytesutil.annotations.modifiers.Unsigned;
import org.dzh.bytesutil.annotations.modifiers.Variant;
import org.dzh.bytesutil.annotations.types.UserDefined;
import org.dzh.bytesutil.converters.Converter;
import org.dzh.bytesutil.converters.Converters;

/**
 * Internal class that stores compile-time information of a {@link Field}
 * 
 * @author dzh
 */
public class FieldInfo{
    private final Field field;
    protected final ClassInfo base;
    private final Map<Class<? extends Annotation>,Annotation> annotations;
    private final Class<?> fieldClass;
    
    public final Converter<?> converter;
    public final Converter<?> innerConverter;
    
    public final String name;
    public final DataType dataType;
    public final boolean isEntity;
    public final boolean isEntityList;
    public final Class<?> listComponentClass;
    
    public final EntityHandler entityCreator;
    
    public final byte[] endsWith;
    //auxiliary array for KMP searching
    public final int[] endingArrayAux;
    /**
     * Entity class that declares this field
     */
    public final Class<?> enclosingEntityClass;
    /**
     * whether this field is defined as little-endian
     */
    public final boolean littleEndian;
    /**
     * whether this field is defined as big-endian
     */
    public final boolean bigEndian;
    /**
     * whether this field is defined as signed
     */
    public final boolean signed;
    /**
     * whether this field is defined as unsigned
     */
    public final boolean unsigned;
    /**
     * Value of {@link Length} annotation.
     * <p>
     * If the annotation is absent, or it declares a dynamic length, value of this
     * field will be -1.
     */
    public final int length;
    public final int listLength;
    /**
     * Type of the {@link Length} value in the stream. For example some protocol
     * defines length as unsigned byte, while others defines it as unsigned short
     * etc.
     */
    private final DataType lengthType;
    private final DataType listLengthType;
    /**
     * {@link ModifierHandler} object which is used to obtain the length value
     * dynamically for this field, null if it is not defined in the {@link Length}
     * annotation.
     */
    public final ModifierHandler<Integer> lengthHandler;
    public final ModifierHandler<Integer> listLengthHandler;
    @SuppressWarnings("rawtypes")
    public final TypeConverter userDefinedConverter;
    /**
     * Charset of this field, null if not defined
     */
    public final Charset charset;
    /**
     * {@link ModifierHandler} object which is used to obtain Charset object dynamically 
     * for this field, null if it is not defined in the {@link CHARSET}
     * annotation.
     */
    public final ModifierHandler<Charset> charsetHandler;
    /**
     * Pattern string defined in {@link DatePattern} annotation, null if not present.
     */
    public final String datePattern;
    /**
     * Whether Length/ListLength annotation is present, used only by ClassInfo
     */
    final boolean lengthDefined;
    /**
     * Whether Length/ListLength annotation is present and used to define 
     * a concrete length (not dynamic length)
     */
    final boolean customLengthDefined;
    
    FieldInfo(Field field, DataType type, ClassInfo base) {
        this.base = base;
        this.field = field;
        this.name = field.getName();
        final Class<?> fieldClass = field.getType();
        this.fieldClass = fieldClass;
        this.dataType = type;
        this.enclosingEntityClass = field.getDeclaringClass();
        
        this.isEntity = DataPacket.class.isAssignableFrom(fieldClass);
        
        if(List.class.isAssignableFrom(fieldClass)) {
            Class<?> componentClass = ClassInfo.firstTypeParameterClass(field);
            if(componentClass==null) {
                throw forContext(base.entityClass, name, "should declare dataType parameter if it is a List")
                    .withSiteAndOrdinal(FieldInfo.class, -1);
            }
            this.listComponentClass = componentClass;
            this.isEntityList = DataPacket.class.isAssignableFrom(listComponentClass);
            
            if(! isEntityList && ! type.supports(componentClass)) {
                throw forContext(base.entityClass, field.getName(), "conversion from "+componentClass+" to "+type+" is not supported")
                    .withSiteAndOrdinal(FieldInfo.class, 0);
            }
        }else {
            this.listComponentClass = null;
            this.isEntityList = false;
            
            if(! isEntity && ! type.supports(fieldClass)) {
                throw forContext(base.entityClass, field.getName(), "conversion from "+fieldClass+" to "+type+" is not supported")
                    .withSiteAndOrdinal(FieldInfo.class, 1);
            }
        }
        
        Map<Class<? extends Annotation>,Annotation> _annotations = new HashMap<>();
        for(Annotation an:field.getAnnotations()) {
            _annotations.put(an.annotationType(), an);
        }
        this.annotations = Collections.unmodifiableMap(_annotations);
        
        Variant cond = localAnnotation(Variant.class);
        if(cond==null) {
            this.entityCreator = new PlainReflectionEntityHandler(isEntityList ? listComponentClass : fieldClass);
        }else {
            try {
                this.entityCreator = cond.value().newInstance();
            } catch (Exception e) {
                throw forContext(base.entityClass, name, "VariantEntityHandler cannot be initialized by no-arg contructor")
                    .withSiteAndOrdinal(FieldInfo.class, 5);
            }
        }
        
        {
            Annotation ret = mutualExclusive(BigEndian.class,LittleEndian.class);
            this.littleEndian = ret!=null && ret instanceof LittleEndian;
            this.bigEndian = !littleEndian;
        }
        {
            Annotation ret = mutualExclusive(Unsigned.class,Signed.class);
            this.signed = ret!=null && ret instanceof Signed;
            this.unsigned = !signed;
        }
        {
            CHARSET cs = annotation(CHARSET.class);
            if(cs==null) {
                charset = CHARSET.DEFAULT_CHARSET;
                charsetHandler = null;
            }else if( ! PlaceHolderHandler.class.isAssignableFrom(cs.handler())) {
                charset = null;
                try {
                    charsetHandler = cs.handler().newInstance();
                } catch (Exception e) {
                    throw forContext(base.entityClass, name, "Charset ModifierHandler cannot be initialized by no-arg contructor")
                        .withSiteAndOrdinal(FieldInfo.class, 6);
                }
            }else {
                try {
                    charset = Charset.forName(cs.value());
                } catch (IllegalCharsetNameException | UnsupportedCharsetException e) {
                    throw forContext(base.entityClass, name, "Illegal charset name: "+cs.value())
                        .withSiteAndOrdinal(FieldInfo.class, 6);
                }
                charsetHandler = null;
            }
        }
        {
            Length len = localAnnotation(Length.class);
            if(len==null) {
                length = -1;
                lengthHandler = null;
                lengthType = null;
                lengthDefined = false;
                customLengthDefined = false;
            }else {
                this.length = len.value();
                if( ! PlaceHolderHandler.class.isAssignableFrom(len.handler())) {
                    try {
                        this.lengthHandler = len.handler().newInstance();
                        this.lengthHandler.checkLength = true;
                    } catch (Exception e) {
                        throw forContext(base.entityClass, name, "Length ModifierHandler cannot be initialized by no-arg contructor")
                            .withSiteAndOrdinal(FieldInfo.class, 9);
                    }
                }else {
                    this.lengthHandler = null;
                }
                lengthType = len.type();
                switch(lengthType) {
                case BYTE:
                case SHORT:
                case INT:
                    break;
                default:
                    throw forContext(base.entityClass, name, "data dataType "+lengthType+" should not be specified as length dataType")
                            .withSiteAndOrdinal(FieldInfo.class, 10);
                }
                
                lengthDefined = true;
                customLengthDefined = length>=0 || lengthHandler!=null;
            }
        }
        {
            EndsWith ew = localAnnotation(EndsWith.class);
            if(ew==null) {
                endsWith = null;
                endingArrayAux = null;
            }else {
                endsWith = ew.value();
                if(endsWith.length==0) {
                    throw forContext(base.entityClass, name, "EndsWith array should be non-empty")
                        .withSiteAndOrdinal(FieldInfo.class, 11);
                }
                endingArrayAux = createAuxArray(endsWith);
            }
        }
        {
            ListLength len = localAnnotation(ListLength.class);
            if(len==null) {
                listLength = -1;
                listLengthHandler = null;
                listLengthType = null;
            }else {
                this.listLength = len.value();
                if( ! PlaceHolderHandler.class.isAssignableFrom(len.handler())) {
                    try {
                        this.listLengthHandler = len.handler().newInstance();
                        this.listLengthHandler.checkLength = true;
                    } catch (Exception e) {
                        throw forContext(base.entityClass, name, "ListLength ModifierHandler cannot be initialized by no-arg contructor")
                            .withSiteAndOrdinal(FieldInfo.class, 11);
                    }
                }else {
                    this.listLengthHandler = null;
                }
                listLengthType = len.type();
            }
        }
        {
            DatePattern df = localAnnotation(DatePattern.class);
            if(df==null) {
                if(fieldClass == java.util.Date.class && type!=DataType.INT && type!=DataType.LONG) {
                    throw forContext(base.entityClass, name, "define a date pattern")
                        .withSiteAndOrdinal(FieldInfo.class, 2);
                }
                this.datePattern = null;
            }else {
                this.datePattern = df.value();
            }
        }
        
        TypeConverter<?> typeConverter = null;
        UserDefined userDefined = annotation(UserDefined.class);
        if(userDefined!=null) {
            if(lengthDefined && length<0 && lengthHandler==null) {
                throw forContext(base.entityClass, name, "user defined type does not support write-ahead length")
                    .withSiteAndOrdinal(FieldInfo.class, 23);
            }
            try {
                typeConverter = annotation(UserDefined.class).value().newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw forContext(base.entityClass, name, "user defined converter type cannot be instantiated")
                    .withSiteAndOrdinal(FieldInfo.class, 24);
            }
        }else {
            typeConverter = null;
        }
        this.userDefinedConverter = typeConverter;
        
        if(isEntityList) {
            //class of list elements is another DataPacket
            this.converter = Converters.listConverter;
            this.innerConverter = Converters.dataPacketConverter;
        }else if(listComponentClass!=null) {
            this.converter = Converters.listConverter;
            //component class is a pre-defined data dataType or a user-defined type
            this.innerConverter = typeConverter!=null ? Converters.userDefinedTypeConverter : Converters.converters.get(listComponentClass);
        }else if(isEntity) {
            //class of field is a DataPacket
            this.converter = Converters.dataPacketConverter;
            this.innerConverter = null;
        }else {
            //a plain field
            this.converter = typeConverter!=null ? Converters.userDefinedTypeConverter : Converters.converters.get(getFieldType());
            this.innerConverter = null;
        }
    }
    /**
     * Wrapper of {@link Field#get(Object)}
     * @param self this object
     * @return  field value
     */
    public Object get(Object self) {
        try {
            return field.get(self);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            //for backward-compatibility, do not throw checked exception here 
            throw new Error(
                    String.format("cannot obtain value of field [%s] by reflection"
                            ,field.getName()),e);
        }
    }
    /**
     * Wrapper of {@link Field#set(Object, Object)}
     * @param self  this object
     * @param val   value to set
     */
    public void set(Object self, Object val) {
        try {
            field.set(self, val);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            //for backward-compatibility, do not throw checked exception here 
            throw new Error(
                    String.format("cannot set value of field [%s] by reflection"
                            ,field.getName()),e);
        }
    }
    
    /**
     * Returns lengthType or listLengthType
     * @return  lengthType or listLengthType
     */
    public DataType lengthType() {
        return lengthType!=null ? lengthType : listLengthType;
    }
    
    /**
     * Get class of this field,
     * intended to be overridden by sub-classes
     * @return  class of this field
     */
    public Class<?> getFieldType(){
        return fieldClass;
    }
    
    public Class<?> getEntityType(){
        return base.entityClass;
    }
    
    /**
     * Annotation of specific class for this field, if the same annotation is
     * present on both field and class, the one on the field always take precedence.
     * 
     * @param annoCls   class of annotation
     * @param <T>   class of annotation
     * @return null if it is not present both on the field or on the class
     */
    public <T extends Annotation> T annotation(Class<T> annoCls) {
        T anno = localAnnotation(annoCls);
        if(anno!=null) {
            return anno;
        }
        return globalAnnotation(annoCls);
    }
    
    /**
     * Annotation of specific class present on the field.
     * 
     * @param annoCls   class of annotation
     * @param <T>   class of annotation
     * @return returns null if it is not present on the field, even it is present on
     *         the class
     */
    @SuppressWarnings("unchecked")
    public <T extends Annotation> T localAnnotation(Class<T> annoCls) {
        return (T) annotations.get(annoCls);
    }

    /**
     * Annotation of specific class present on the class definition of this field.
     * 
     * @param annoCls   class of annotation
     * @param <T>   class of annotation
     * @return    returns null if it is not present
     */
    public <T extends Annotation> T globalAnnotation(Class<T> annoCls) {
        return base.globalAnnotation(annoCls);
    }
    
    private Annotation mutualExclusive(Class<? extends Annotation> def, Class<? extends Annotation> another) {
        Annotation local1 = localAnnotation(def);
        Annotation local2 = localAnnotation(another);
        if(local1!=null && local2!=null) {
            throw forContext(base.entityClass, name, 
                    String.format("[%s] and [%s] should not be both present on the same field declaration",def,another))
                    .withSiteAndOrdinal(FieldInfo.class, 21);
        }else if(local1!=null && local2==null) {
            return local1;
        }else if(local1==null && local2!=null) {
            return local2;
        }
        Annotation global1 = globalAnnotation(def);
        Annotation global2 = globalAnnotation(another);
        if(global1!=null && global2!=null) {
            throw forContext(base.entityClass, name, 
                    String.format("[%s] and [%s] should not be both present on the same class declaration",def,another))
                    .withSiteAndOrdinal(FieldInfo.class, 22);
        }else if(global1!=null && global2==null) {
            return global1;
        }else if(global1==null && global2!=null) {
            return global2;
        }
        return null;
    }
    
    private static final class PlainReflectionEntityHandler extends EntityHandler{
        
        private Class<?> classToCreate;
        
        public PlainReflectionEntityHandler(Class<?> classToCreate) {
            this.classToCreate = classToCreate;
        }

        @Override
        public DataPacket handle0(String fieldName, Object entity, InputStream is) throws IOException {
            try {
                return (DataPacket) classToCreate.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    private static final int[] createAuxArray(byte[] src){ 
        int[] ret = new int[src.length];
        int len = 0; 
        int i = 1; 
        ret[0] = 0;
        while (i < src.length) { 
            if (src[i] == src[len]) { 
                len++; 
                ret[i] = len; 
                i++; 
            }else{ 
                if (len != 0) { 
                    len = ret[len - 1]; 
                }else{ 
                    ret[i] = len; 
                    i++; 
                } 
            } 
        }
        return ret;
    } 
    
    @Override
    public String toString() {
        return "FieldInfo:Entity["+enclosingEntityClass+"],Field:["+name+"]";
    }
}