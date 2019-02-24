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
import org.dzh.bytesutil.annotations.modifiers.ListLength;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.types.BCD;
import org.dzh.bytesutil.annotations.types.CHAR;
import org.dzh.bytesutil.annotations.types.RAW;

import static org.dzh.bytesutil.converters.auxiliary.Utils.forContext;

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
			throw forContext(cls, null, "should be a class which inherits DataPacket")
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
			for(DataType tp:DataType.values()) {
				if(f.getAnnotation(tp.annotationClassOfThisType())!=null) {
					if(type!=null) {
						throw forContext(cls, name, "multiple data dataType declaration on same field is not allowed")
							.withSiteAndOrdinal(ClassInfo.class, 1);
					}
					type = tp;
				}
			}
			if(type==null){
				Class<?> componentClass;
				if( ! DataPacket.class.isAssignableFrom(f.getType())
				&& ((componentClass = firstTypeParameterClass(f))==null
				|| ! DataPacket.class.isAssignableFrom(componentClass))) {
					throw forContext(cls, name, "field not marked with a DataType")
						.withSiteAndOrdinal(ClassInfo.class, 2);
				}
			}
			
			if(type == DataType.BCD) {
				BCD anno = f.getAnnotation(BCD.class);
				if(anno.value()<0) {
					throw forContext(cls, name, "BCD length should not be negative")
						.withSiteAndOrdinal(ClassInfo.class, 3);
				}
			}
			
			FieldInfo fi = f.getType().isEnum() ? new EnumFieldInfo(f, type, this) : new FieldInfo(f,type,this);
			
			if(fi.listComponentClass!=null) {
				if(fi.localAnnotation(Length.class)==null
						&& fi.localAnnotation(ListLength.class)==null) {
					throw forContext(cls, name, "neither Length nor ListLength annotation are present")
						.withSiteAndOrdinal(ClassInfo.class, 4);
				}
				if(((fi.dataType == DataType.RAW && fi.localAnnotation(RAW.class).value()<0)
						|| (fi.dataType == DataType.CHAR && fi.localAnnotation(CHAR.class).value()<0))
						&& fi.localAnnotation(ListLength.class)==null) {
					throw forContext(cls, name, "this field is a list of Data Type that supports dynamic length, "
							+ "to avoid ambiguity, use ListLength but not Length to specify the list length")
						.withSiteAndOrdinal(ClassInfo.class, 5);
				}
			}
			
			//either specify a positive value property or use a Length annotation to 
			//declare the length
			CHAR ch = fi.localAnnotation(CHAR.class);
			if(ch!=null && ch.value()<0) {
				if( ! fi.lengthDefined) {
					throw forContext(cls, name, "this field is defined as CHAR, but its value property is negative"
							+ " and a Length annotation is not present on it")
						.withSiteAndOrdinal(ClassInfo.class, 6);
				}
			}
			
			RAW raw = fi.localAnnotation(RAW.class);
			if(raw!=null && raw.value()<0) {
				if( ! fi.lengthDefined)
					throw forContext(cls, name, "this field is defined as RAW, but its value property is negative"
							+ " and a Length annotation is not present on it")
						.withSiteAndOrdinal(ClassInfo.class, 7);
			}
			
			fieldInfoByField.put(name, fi);
		}
	}
	
	/**
	 * Get a <b>copy</b> of FieldInfo list
	 * @return
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
				throw forContext(null, o1.getName()+"/"+o2.getName(), "two fields have same order value")
					.withSiteAndOrdinal(ClassInfo.class, 8);
			}
			/*
			 * sort them reversely there to prevent inserting into the beginning
			 */
			return -(val1 - val2);
		}
		
	};
}
