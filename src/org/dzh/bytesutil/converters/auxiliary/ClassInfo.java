package org.dzh.bytesutil.converters.auxiliary;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.Length;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Variant;

/**
 * Internal class that records info about annotations, fields of a specific
 * class for quick access, should not be used in client code.
 * <p>
 * This class itself is thread-safe after construction
 * @author dzh
 */
public class ClassInfo {
	/**
	 * Annotations that are present on the class (on the type)
	 */
	private Map<Class<? extends Annotation>, Annotation> globalAnnotations = new HashMap<>();
	/**
	 * {@link Context} objects that will be passed to client code
	 */
	private Map<String,Context> contextsByField = new HashMap<>();
	/**
	 * {@link FieldInfo} objects that are used internally, in the order specified by
	 * {@link Order} annotation.
	 */
	private Map<String,FieldInfo> fieldInfoByField = new LinkedHashMap<>();
	
	Class<?> entityClass;
	/**
	 * Internal class that stores compile-time information of a {@link Field}
	 * 
	 * @author dzh
	 */
	public static final class FieldInfo{
		private Field field;
		public final String name;
		public final Class<?> fieldClass;
		public final DataType type;
		public final ModifierHandler<DataPacket> variantEntityHandler;
		public final boolean isEntity;
		public final Class<?> listComponentClass;
		public final boolean isEntityList;
		public final Map<Class<? extends Annotation>,Annotation> annotations;
		FieldInfo(Field field, String name, Class<?> fieldClass, DataType type) {
			this.field = field;
			this.name = name;
			this.fieldClass = fieldClass;
			this.type = type;
			this.isEntity = DataPacket.class.isAssignableFrom(fieldClass);
			if(List.class.isAssignableFrom(fieldClass)) {
				Class<?> componentClass = firstTypeParameterClass(field);
				if(componentClass==null) {
					throw new RuntimeException(
						String.format("field [%s] should declare type parameter if it is a List", name));
				}
				this.listComponentClass = componentClass;
				this.isEntityList = DataPacket.class.isAssignableFrom(listComponentClass);
			}else {
				this.listComponentClass = null;
				this.isEntityList = false;
			}
			
			Map<Class<? extends Annotation>,Annotation> _annotations = new HashMap<>();
			for(Annotation an:field.getAnnotations()) {
				_annotations.put(an.annotationType(), an);
			}
			this.annotations = Collections.unmodifiableMap(_annotations);
			
			Variant cond = getAnnotation(Variant.class);
			try {
				this.variantEntityHandler = cond != null ? cond.value().newInstance() : null;
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(
						String.format("VariantEntityHandler class [%s] cannot be initialized by no-arg contructor"
								, cond.value()));
			}
		}
		/**
		 * Wrapper of {@link Field#get(Object)}
		 * @param self this object
		 * @return
		 */
		public Object get(Object self) {
			try {
				return field.get(self);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(
						String.format("cannot obtain value of field [%s] by reflection"
								,field.getName()),e);
			}
		}
		/**
		 * Wrapper of {@link Field#set(Object, Object)}
		 * @param self
		 * @param val
		 */
		public void set(Object self, Object val) {
			try {
				field.set(self, val);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(
						String.format("cannot set value of field [%s] by reflection"
								,field.getName()),e);
			}
		}
		@SuppressWarnings("unchecked")
		public <T extends Annotation> T getAnnotation(Class<T> annoCls) {
			return (T) annotations.get(annoCls);
		}
	}
	
	public ClassInfo(Class<?> cls) {
		
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
		while(cls!=null){
			List<Field> tmpList = new ArrayList<>(Arrays.asList(cls.getDeclaredFields()));
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
				try {
					f.setAccessible(true);
				} catch (SecurityException e) {
					throw new RuntimeException(
							String.format("field %s is marked for serialization, but cannot be made accessible."
									,f.getName()),e);
				}
			}
			
			Collections.sort(tmpList, reverseFieldComparator);
			fieldList.addAll(tmpList);
			cls = cls.getSuperclass();
			//theoretically Object.class cannot be reached
			if(cls == DataPacket.class || cls==Object.class) {
				break;
			}
		}
		
		Collections.reverse(fieldList);
		
		for(Field f:fieldList) {
			
			String name = f.getName();
			
			/*
			 * check whether a DataType is defined on this field, as is required by
			 * serialization. If none, check whether this field itself is a
			 * Data(entity), or is a List of Data. If all of these checks
			 * fail, throw an exception
			 */
			DataType type = null;
			for(DataType tp:DataType.values()) {
				if(f.getAnnotation(tp.annotationClassOfThisType())!=null) {
					if(type!=null) {
						throw new IllegalArgumentException(
								String.format("multiple data type declaration on field [%s] is not allowed", name));
					}
					type = tp;
				}
			}
			if(type==null){
				Class<?> componentClass = null;
				if( ! DataPacket.class.isAssignableFrom(f.getType())
				&& ((componentClass = firstTypeParameterClass(f))==null
				|| ! DataPacket.class.isAssignableFrom(componentClass))) {
					throw new IllegalArgumentException(String.format("field [%s] is not marked with a DataType", name));
				}
			}
			FieldInfo fi = new FieldInfo(f, name, f.getType(), type);
			fieldInfoByField.put(name, fi);
			
			if(fi.listComponentClass!=null
			&& annotationOfField(name, Length.class)==null) {
				throw new IllegalArgumentException(String.format(
						"field [%s] is a list but a Length annotation is not present on it", name));
			}
			
			contextsByField.put(name, new Context(this,name));
		}
	}
	
	/**
	 * Get a <b>copy</b> of FieldInfo list
	 * @return
	 */
	public List<FieldInfo> fieldInfoList() {
		return new ArrayList<>(fieldInfoByField.values());
	}
	
	public Context contextOfField(String name) {
		return contextsByField.get(name);
	}
	
	@SuppressWarnings("unchecked")
	<T extends Annotation> T globalAnnotation(Class<T> annoCls) {
		return (T) globalAnnotations.get(annoCls);
	}
	<T extends Annotation> T annotationOfField(String name,Class<T> annoCls) {
		return (T) fieldInfoByField.get(name).getAnnotation(annoCls);
	}
	
	//return null for non-generic field definitions
	private static Class<?> firstTypeParameterClass(Field field){
		Type type = field.getGenericType();
		if( ! (type instanceof ParameterizedType)) {
			return null;
		}
		Type[] types = ((ParameterizedType)type).getActualTypeArguments();
		if(types.length==0) {
			return null;
		}
		return (Class<?>)types[0];
	}
	
	private static final Comparator<Field> reverseFieldComparator = new Comparator<Field>() {
		@Override
		public int compare(Field o1, Field o2) {
			int val1 = o1.getAnnotation(Order.class).value();
			int val2 = o2.getAnnotation(Order.class).value();
			if(val1==val2) {
				throw new RuntimeException(String.format("field %s and field %s have same order value",
						o1.getName(),o2.getName()));
			}
			/*
			 * sort them reversely there to prevent always insert into the beginning of
			 * result list.
			 */
			return -(val1 - val2);
		}
		
	};
}
