package test.general;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.dzh.bytesutil.annotations.modifiers.Order;

public class Utils {
	public static boolean equalsOrderFields(Object o1, Object o2) {
		if(o1==o2) {
			return true;
		}
		if(o1==null && o2!=null
		||(o1!=null && o2==null)) {
			return false;
		}
		Class<?> c1 = o1.getClass();
		Class<?> c2 = o2.getClass();
		if(Collection.class.isAssignableFrom(c1)
		&& Collection.class.isAssignableFrom(c2)) {
			Collection<?> col1 = (Collection<?>)o1;
			Collection<?> col2 = (Collection<?>)o2;
			if(col1.size()!=col2.size()) {
				return false;
			}
			Iterator<?> iter = ((Collection<?>)o1).iterator();
			Iterator<?> iter2 = ((Collection<?>)o2).iterator();
			while(iter.hasNext()) {
				Object elem1 = iter.next();
				Object elem2 = iter2.next();
				if( ! equalsOrderFields(elem1,elem2)) {
					return false;
				}
			}
			return true;
		}
		if(Map.class.isAssignableFrom(c1)
		&& Map.class.isAssignableFrom(c2)) {
			Map<?,?> map1 = (Map<?,?>)o1;
			Map<?,?> map2 = (Map<?,?>)o2;
			if(map1.size()!=map2.size()) {
				return false;
			}
			Set<?> keySet = map1.keySet();
			for(Object obj:keySet) {
				Object elem1 = map1.get(obj);
				Object elem2 = map2.get(obj);
				if( ! equalsOrderFields(elem1,elem2)) {
					return false;
				}
			}
			return true;
		}
		if(c1!=c2) {
			return false;
		}
		if(Number.class.isAssignableFrom(c1)) {
			return ((Number)o1).longValue() == ((Number)o2).longValue() 
					&& ((Number)o1).doubleValue() == ((Number)o2).doubleValue();
		}
		if(c1==String.class) {
			return ((String)o1).equals((String)o2);
		}
		if(c1==Character.class) {
			return ((Character)o1).equals((Character)o2);
		}
		if(c1==Boolean.class) {
			return ((Boolean)o1).booleanValue() == ((Boolean)o2).booleanValue();
		}
		if(java.util.Date.class.isAssignableFrom(c1)) {
			return ((java.util.Date)o1).getTime() == ((java.util.Date)o2).getTime();
		}
		if(c1.isArray()) {
			int len1 = Array.getLength(o1);
			int len2 = Array.getLength(o2);
			if(len1!=len2) {
				return false;
			}
			for(int i=0;i<len1;++i) {
				Object elem1 = Array.get(o1,i);
				Object elem2 = Array.get(o2, i);
				if( ! equalsOrderFields(elem1,elem2)) {
					return false;
				}
			}
			return true;
		}
		while(c1!=Object.class) {
			Field[] fields = c1.getDeclaredFields();
			for(Field f:fields) {
				if((f.getModifiers() & Modifier.STATIC)!=0) {
					continue;
				}
				if(f.getAnnotation(Order.class)==null) {
					continue;
				}
				f.setAccessible(true);
				try {
					Object val1 = f.get(o1);
					Object val2 = f.get(o2);
					if( ! equalsOrderFields(val1,val2)) {
						return false;
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new Error(f.getName());
				}
			}
			c1 = c1.getSuperclass();
		}
		return true;
	}
}