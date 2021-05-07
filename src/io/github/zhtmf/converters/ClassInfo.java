package io.github.zhtmf.converters;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import io.github.zhtmf.ConversionException;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Injectable;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.ListEndsWith;
import io.github.zhtmf.annotations.modifiers.ListLength;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.types.CHAR;
import io.github.zhtmf.annotations.types.RAW;
import io.github.zhtmf.annotations.types.UserDefined;
import io.github.zhtmf.converters.auxiliary.DataType;
import io.github.zhtmf.converters.auxiliary.ModifierHandler;
import io.github.zhtmf.converters.auxiliary.ModifierHandler.OffsetAccess;

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
     * Annotations that are present on the class
     */
    private Map<Class<? extends Annotation>, Annotation> globalAnnotations = new HashMap<>();
    
    private Map<Field, String> injectableFields = Collections.emptyMap();
    
    private Map<Method, String> injectableMethods = Collections.emptyMap();
    
    /**
     * {@link FieldInfo} objects in the order specified by {@link Order} annotation.
     */
    List<FieldInfo> fieldInfoList = new ArrayList<>();
    List<FieldInfo> fieldInfoListForLength = new ArrayList<FieldInfo>();
    
    /**
     * Fields that are not annotated with @Order and referenced by @Injectable of other classes
     * Put here simply for convenience and to avoid a global cache shared by all classes.
     */
    private Map<String, Field> referencedFields = new ConcurrentHashMap<String, Field>();
    
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
        while(tmp != DataPacket.class){
        	
            List<Field> tmpList = new ArrayList<>(Arrays.asList(tmp.getDeclaredFields()));
            for(int i=0;i<tmpList.size();++i) {
                Field f = tmpList.get(i);
                /*
                 * ignore any fields that are not annotated by Order annotation
                 * and any fields that are static or final
                 */
                int mod = f.getModifiers();
                if((mod & Modifier.STATIC)!=0 || (mod & Modifier.FINAL)!=0) {
                	tmpList.remove(i);
                    --i;
                    continue;
                }
                
                Order order = f.getAnnotation(Order.class);
                Injectable injectable = f.getAnnotation(Injectable.class);
                if(order != null && injectable != null)
                	throw FieldInfo.forContext(cls, f.getName(), "Field already marked with @Order cannot be marked with @Injectable")
            			.withSiteAndOrdinal(ClassInfo.class, 15);
                
                f.setAccessible(true);
                
                if(order == null) {
                    tmpList.remove(i);
                    --i;
                }
                
                if(injectable != null) {
            		String value = injectable.value();
            		if(injectableFields.isEmpty())
            			injectableFields = new HashMap<>();
            		injectableFields.put(f, value.isEmpty() ? f.getName() : value);
            	}
            }
            
            Method[] methods = tmp.getDeclaredMethods();
            for(int i = 0;i<methods.length;++i) {
            	Method method = methods[i];
            	if((method.getModifiers() & Modifier.STATIC) != 0)
            		continue;
            	Injectable injectable = method.getAnnotation(Injectable.class);
            	if(injectable != null) {
            		if(method.getParameterCount() > 1) {
            			throw FieldInfo.forContext(
            					cls, method.getName(), "@Injectable methods should have only one parameter")
                		.withSiteAndOrdinal(ClassInfo.class, 18);
            		}
            		String value = injectable.value();
            		if(value.isEmpty()) {
            			String name = method.getName();
            			//convert setter methods' names to their simple form
            			//otherwise keep the original name
            			if(name.length() > 3 && name.startsWith("set") && Character.isUpperCase(name.charAt(3))) {
            				name = Character.toLowerCase(name.charAt(3)) + name.substring(4);
            			}
            			value = name;
            		}
            		method.setAccessible(true);
            		if(injectableMethods.isEmpty())
            			injectableMethods = new HashMap<>();
            		injectableMethods.put(method, value);
            	}
            }
            
            Collections.sort(tmpList, reverseFieldComparator);
            fieldList.addAll(tmpList);
            tmp = tmp.getSuperclass();
        }
        
        injectableFields = Collections.unmodifiableMap(injectableFields);
        injectableMethods = Collections.unmodifiableMap(injectableMethods);
        
        Collections.reverse(fieldList);
        
        List<FieldInfo> fieldInfoList = this.fieldInfoList;
        
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
            
            FieldInfo fieldInfo;
            if(f.getType().isEnum()
               && f.getAnnotation(UserDefined.class)==null) {
                fieldInfo = new FieldInfo.EnumFieldInfo(f, type, this);
            }else {
                fieldInfo = new FieldInfo(f,type,this);
            }
            
            if(fieldInfo.listComponentClass!=null) {
                if(fieldInfo.localAnnotation(Length.class)==null
                        && fieldInfo.localAnnotation(ListLength.class) == null
                        && fieldInfo.localAnnotation(ListEndsWith.class) == null) {
                    throw FieldInfo.forContext(cls, name, "There is no Length, ListLength or EndsWith presents on this field")
                        .withSiteAndOrdinal(ClassInfo.class, 4);
                }
                if((fieldInfo.localAnnotation(Length.class) != null || fieldInfo.localAnnotation(ListLength.class) != null)
                        && fieldInfo.localAnnotation(ListEndsWith.class) != null) {
                    throw FieldInfo.forContext(cls, name, "Only one of Length/ListLength/EndsWith should be used to specify length of this field.")
                        .withSiteAndOrdinal(ClassInfo.class, 14);
                }
                    
                if(((fieldInfo.dataType == DataType.RAW && fieldInfo.localAnnotation(RAW.class).value()<0)
                || (fieldInfo.dataType == DataType.CHAR && fieldInfo.localAnnotation(CHAR.class).value()<0)
                || (fieldInfo.dataType == DataType.USER_DEFINED && fieldInfo.localAnnotation(UserDefined.class).length()<0)
                || (fieldInfo.dataType == DataType.BIT /* default value of bit is 1 */))
                        && fieldInfo.localAnnotation(Length.class) != null 
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
                                                + " and neither Length nor EndsWith is not present on it")
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
        
        //check whether BIT fields are grouped together
        //make another list solely for length calculation
        List<FieldInfo> fieldInfoListForLength = new ArrayList<FieldInfo>();
        int bitCount = 0;
        FieldInfo lastBitFieldInfo = null;
        for(int i=0;i<fieldInfoList.size();++i) {
            FieldInfo fieldInfo = fieldInfoList.get(i);
            if(fieldInfo.dataType == DataType.BIT) {
                if(lastBitFieldInfo==null) {
                    lastBitFieldInfo = fieldInfo;
                    fieldInfoListForLength.add(lastBitFieldInfo);
                }
                if(fieldInfo.listComponentClass != null) {
                    bitCount += fieldInfo.bitCount * fieldInfo.listLength;
                }else {
                    bitCount += fieldInfo.bitCount;
                }
                if(bitCount>8) {
                    throw FieldInfo.forContext(cls, "", 
                            "BIT type fields should appear consecutively"
                          + " within the same class and form"
                          + " groups of 8 bits.")
                    .withSiteAndOrdinal(ClassInfo.class, 12);
                }
                
                if(bitCount==8) {
                    lastBitFieldInfo = null;
                    bitCount = 0;
                }
            }else {
                fieldInfoListForLength.add(fieldInfo);
            }
        }
        
        if(lastBitFieldInfo!=null) {
            throw FieldInfo.forContext(cls, "", 
                    "BIT type fields should appear consecutively"
                  + " within the same class and form"
                  + " groups of 8 bits.")
            .withSiteAndOrdinal(ClassInfo.class, 13);
        }
        
        this.fieldInfoList = Collections.unmodifiableList(fieldInfoList);
        this.fieldInfoListForLength = Collections.unmodifiableList(fieldInfoListForLength);
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
    
    void injectAdditionalFields(Object parent, Object dataPacket) {
    	Map<Field, String> injectableFields = this.injectableFields;
    	if(!injectableFields.isEmpty())
    		for(Entry<Field, String> entry : injectableFields.entrySet()) {
    			Field field = entry.getKey();
    			String alias = entry.getValue();
    			Field parentField = ClassInfo.getClassInfo(parent).getCachedField(alias);
    			try {
    				field.set(dataPacket, parentField.get(parent));
    			} catch (IllegalArgumentException | IllegalAccessException e) {
    				throw FieldInfo.forContext(dataPacket.getClass(), field.getName(), 
    						"Error in injecting field " + field.getName() +", maybe it is of incompatible type with "
    								+ "injection source field in the parent object", e)
    				.withSiteAndOrdinal(ClassInfo.class, 17);
    			}
    		}
    	Map<Method, String> injectableMethods = this.injectableMethods;
    	if(!injectableMethods.isEmpty())
    		for(Entry<Method, String> entry : injectableMethods.entrySet()) {
    			Method method = entry.getKey();
    			String alias = entry.getValue();
    			Field parentField = ClassInfo.getClassInfo(parent).getCachedField(alias);
    			try {
    				method.invoke(dataPacket, parentField.get(parent));
    			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
    				throw FieldInfo.forContext(dataPacket.getClass(), method.getName(), 
    						"Error in injecting value by invoking method " + method, e)
    				.withSiteAndOrdinal(ClassInfo.class, 19);
    			}
    		}
    }
    
    private Field getCachedField(String name) {
    	Field field = referencedFields.get(name);
    	if(field == null) {
    		Class<?> entityClass  = this.entityClass;
			while(entityClass != DataPacket.class) {
    			try {
					field = entityClass.getDeclaredField(name);
					if((field.getModifiers() & Modifier.STATIC) != 0) {
						field = null;
						entityClass = entityClass.getSuperclass();
						continue;
					}
					break;
				} catch (NoSuchFieldException | SecurityException e) {
					entityClass = entityClass.getSuperclass();
				}
    		}
			if(field == null) {
				throw FieldInfo.forContext(this.entityClass, name, 
						"Injection source field " + name +" does not"
						+ " exist in class " + this.entityClass 
						+ " and its super classes or cannot be made accessible")
                			.withSiteAndOrdinal(ClassInfo.class, 16);
			}
			field.setAccessible(true);
			referencedFields.put(name, field);
    	}
    	return field;
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
    static ClassInfo getClassInfo(Object entity) {
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
    
    public static void serialize(Object self,OutputStream _dest)
            throws ConversionException, IllegalArgumentException{
        if(_dest==null) {
            throw new NullPointerException();
        }
        
        OutputStream dest = new BitOutputStream(_dest);
        
        //lazy initialization
        ClassInfo ci = getClassInfo(self);
        
        List<FieldInfo> list = ci.fieldInfoList;
        for(int i = 0, l = list.size();i < l; ++i) {
            FieldInfo ctx = list.get(i);
            
            // it is necessary to do it here instead of in ConditionalConverter 
            // to prevent null check for fields that are
            // deemed as unnecessary for serialization and set null
            // throwing an exception will be rather confusing in such a circumstance
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
                //drop unnecessary stack frames
                throw e;
            } catch (UnsatisfiedConstraintException e) {
                ConversionException ex = new ExtendedConversionException(ci.entityClass, ctx.name, e.getMessage())
                        .withSiteAndOrdinal(e.getSite(), e.getOrdinal());
                ex.initCause(e);
                throw ex;
            } catch (Exception e) {
                throw new ExtendedConversionException(self.getClass(),ctx.name,e)
                .withSiteAndOrdinal(DataPacket.class, 4);
            }
        }
    }
    
    public static void deserialize(Object self,InputStream in) throws ConversionException, IllegalArgumentException {
        if(in==null) {
            throw new NullPointerException();
        }
        InputStream _src = MarkableInputStream.wrap(in);
        ClassInfo ci = getClassInfo(self);
        
        List<FieldInfo> list = ci.fieldInfoList;
        for(int i = 0, l = list.size();i < l; ++i) {
            FieldInfo ctx = list.get(i);
            
            Object value = null;
            @SuppressWarnings("unchecked")
            Converter<Object> cv = (Converter<Object>)ctx.converter;
            try {
                value = cv.deserialize(_src, ctx, self);
            } catch(ConversionException | UnsatisfiedConstraintException e) {
                //drop unnecessary stack frames
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
        List<FieldInfo> list = ci.fieldInfoListForLength;
        for(int k=0, l = list.size();k<l;++k) {
            ret += list.get(k).fieldLength(self);
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
        ModifierHandler.setAccess(new OffsetAccess() {
            
            @Override
            public int offset() {
                return DelegateModifierHandler.offset.get();
            }

			@Override
			public Object context() {
				return DelegateModifierHandler.context.get();
			}
        });
    }
}