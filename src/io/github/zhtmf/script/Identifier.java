package io.github.zhtmf.script;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

abstract class Identifier {
    
    abstract Object dereference(Object root);
    abstract void set(Object root, Object value);
    abstract Identifier add(Identifier next);
    abstract int getId();
    abstract String getName();
    
    @Override
    public String toString() {
        return "ID["+getName()+"]";
    }
    
    static Identifier of(String name) {
        return new SingleIdentifier(name);
    }
    
    @FunctionalInterface
    private interface Getter{
        Object get(Object obj, SingleIdentifier propertyName) throws Exception;
    }
    
    @FunctionalInterface
    private interface Setter{
        void set(Object obj, SingleIdentifier propertyName, Object value) throws Exception;
    }
    
    private static final ConcurrentHashMap<String, Getter> GETTERS = new ConcurrentHashMap<String, Getter>();
    private static final Getter DUMMY = (obj,p)->null;
    private static final ConcurrentHashMap<String, Setter> SETTERS = new ConcurrentHashMap<String, Setter>();
    private static final Setter DUMMY2 = (o,p,v)->{};
    private static final ConcurrentHashMap<String, Class<?>> classCache = new ConcurrentHashMap<String, Class<?>>();
    private static final ThreadLocal<Integer> NEXT_HASH_CODE = new ThreadLocal<Integer>() {
        protected Integer initialValue() {return 0;};
    };
    private static int nextHashCode() {
        Integer current = NEXT_HASH_CODE.get();
        int ret = current + 1;
        NEXT_HASH_CODE.set(ret);
        return ret;
    }
    
    private static Getter getGetter(Object root, SingleIdentifier property) {
        Class<?> clazz = getClassOf(root);
        String key = createKey(root, clazz, property);
        Getter getter = GETTERS.get(key);
        if(getter == DUMMY)
            return null;
        if(getter != null)
            return getter;
        getter = getGetter0(root, clazz, property);
        GETTERS.put(key, getter == null ? DUMMY : getter);
        return getter;
    }
    
    private static Setter getSetter(Object root, SingleIdentifier property) {
        Class<?> clazz = getClassOf(root);
        String key = createKey(root, clazz, property);
        Setter setter = SETTERS.get(key);
        if(setter == DUMMY2)
            return null;
        if(setter != null)
            return setter;
        setter = getSetter0(root, clazz, property);
        SETTERS.put(key, setter == null ? DUMMY2 : setter);
        return setter;
    }
    
    private static String createKey(Object root, Class<?> clazz, SingleIdentifier property) {
        if(root instanceof List && property.index >= 0)
            return "java.util.List.Index";
        if(root instanceof Map && !property.lengthOrSize)
            return "java.util.Map";
        return clazz.getName().concat(" ").concat(property.name);
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Getter getGetter0(Object root, Class<?> clazz, SingleIdentifier property) {
        int index = property.index;
        if(index>=0) {
            if(clazz.isArray()) {
                return (obj,s)->Array.get(obj, s.index);
            }else if(!(root instanceof List)){
                throw new ParsingException("indexing "+index+" on non-list object "+root)
                    .withSiteAndOrdinal(Identifier.class, 1);
            }else {
                return (obj,s)->((List<Map<String,Object>>)obj).get(s.index);
            }
        }else if(root instanceof Map){
            if(property.lengthOrSize)
                return (obj,s)->new BigDecimal(((Map)obj).size());
            else
                return (obj,s)->((Map<String,Object>)obj).get(s.name);
        }else {
            //special treatment for length and size
            if(property.lengthOrSize)
                if(root instanceof String)
                    return (obj,s)->new BigDecimal(((String)obj).length());
                else if(root instanceof Collection) 
                    return (obj,s)->new BigDecimal(((Collection)obj).size());
                else if(getClassOf(root).isArray()) 
                    return (obj,s)->new BigDecimal(Array.getLength(obj));
                    
            Method getter = getGetterMethod(clazz, property);
            if(getter != null) {
                return (obj,s)->getter.invoke(obj);
            }
            
            Field field = null;
            try {
                field = clazz.getDeclaredField(property.name);
                field.setAccessible(true);
                final Field tmp = field;
                return (obj,s)->tmp.get(obj);
            } catch (Exception e) {
                return null;
            }
        }
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Setter getSetter0(Object root, Class<?> clazz, SingleIdentifier property) {
        int index = property.index;
        if(index>=0) {
            if(clazz.isArray()) {
                Class<?> componentType = clazz.getComponentType();
                return (obj,p,value)->Array.set(obj, p.index, convertValueIfNeeded(componentType, value));
            }else if(root instanceof List){
                return (obj,p,value)->((List)obj).set(p.index, value);
            }
        }
        
        if(root instanceof Map){
            return (obj,p,value)->((Map<String,Object>)obj).put(p.name, value);
        }
        
        if(index >=0 ) {
            throw new ParsingException("indexing"+index+" on object "+root)
                .withSiteAndOrdinal(Identifier.class, 4);
        }
        Method setter = getSetterMethod(clazz, property);
        if(setter != null) {
            Class<?> type = setter.getParameterTypes()[0];
            return (obj,p,value)->setter.invoke(obj, convertValueIfNeeded(type, value));
        }
        
        Field field = null;
        try {
            field = clazz.getDeclaredField(property.name);
            field.setAccessible(true);
            final Field tmp = field;
            final Class<?> type = tmp.getType();
            return (obj,p,value)->tmp.set(obj, convertValueIfNeeded(type, value));
        } catch (Exception e) {
            return null;
        }
    }
    
    private static Method getGetterMethod(Class<?> cls, SingleIdentifier property) {
        try {
            return cls.getMethod(property.getterName);
        } catch (Exception e) {
            try {
                return cls.getMethod(property.isName);
            } catch (Exception e1) {
                //try to find method with exactly that name 
                //and have no parameters
                try {
                    return cls.getMethod(property.name);
                } catch (Exception e2) {
                }
            }
        }
        return null;
    }
    
    private static Method getSetterMethod(Class<?> cls, SingleIdentifier property) {
        String methodName = property.setterName;
        for(Method method:cls.getMethods()) {
            if(method.getName().matches(methodName)
            && method.getParameterCount() == 1 
            && method.getReturnType() == void.class) {
                return method;
            }
        }
        return null;
    }
    
    private static Class<?> getClassOf(Object root){
        return root instanceof Class ? (Class<?>) root : root.getClass();
    }
    
    private static Object convertValueIfNeeded(Class<?> fieldClass, Object scriptValue) {
        if(!(scriptValue instanceof BigDecimal)) {
            return scriptValue;
        }
        if(fieldClass == BigDecimal.class) {
            return scriptValue;
        }
        BigDecimal numValue = (BigDecimal) scriptValue;
        if(fieldClass.isPrimitive() || Number.class.isAssignableFrom(fieldClass)) {
            if(fieldClass == byte.class || fieldClass == Byte.class) {
                return numValue.byteValue();
            }else if(fieldClass == short.class || fieldClass == Short.class) {
                return numValue.shortValue();
            }else if(fieldClass == int.class || fieldClass == Integer.class) {
                return numValue.intValue();
            }else if(fieldClass == long.class || fieldClass == Long.class) {
                return numValue.longValue();
            }else if(fieldClass == float.class || fieldClass == Float.class) {
                return numValue.floatValue();
            }else if(fieldClass == double.class || fieldClass == Double.class) {
                return numValue.doubleValue();
            }else if(fieldClass == BigInteger.class) {
                return numValue.toBigInteger();
            }
        }
        return scriptValue;
    }
    
    private static final class SingleIdentifier extends Identifier{

        private final String name;
        private final String getterName;
        private final String isName;
        private final String setterName;
        private final boolean lengthOrSize;
        private final int index;
        private final int id;
        public SingleIdentifier(String name) {
            this.name = name;
            this.lengthOrSize = "length".equals(name) || "size".equals(name);
            this.index = tryParseIndex(name);
            this.id = nextHashCode();
            String initialUppercased = Character.toUpperCase(name.charAt(0))+name.substring(1);
            this.getterName = "get" + initialUppercased;
            this.setterName = "set" + initialUppercased;
            this.isName = "is" + initialUppercased;
        }

        @Override
        Object dereference(Object root) {
            if(root instanceof Context) {
                Object cachedValue = ((Context) root).getCachedValue(this);
                if(cachedValue !=null ) 
                    return cachedValue;
            }
            Getter accessor = getGetter(root, this);
            try {
                return accessor == null ? null : accessor.get(root, this);
            } catch (Exception e) {
                throw new ParsingException("exception in retrieving value of property " + name + " of object " + root, e)
                    .withSiteAndOrdinal(Identifier.class, 3);
            }
        }

        @Override
        void set(Object root, Object value) {
            Setter accessor = getSetter(root, this);
            if(accessor != null) {
                try {
                    accessor.set(root, this, value);
                    return;
                } catch (Exception e) {
                    throw new ParsingException("exception in setting value of property " + name + " on object " + root, e)
                    .withSiteAndOrdinal(Identifier.class, 5);
                }
            }
            
            throw new ParsingException("no property named " + name + " found on object "+root)
                .withSiteAndOrdinal(Identifier.class, 6);
        }

        @Override
        Identifier add(Identifier next) {
            return new IdentifierList(this).add(next);
        }

        @Override
        int getId() {
            return id;
        }
        
        private static int tryParseIndex(String name) {
            for(int i=0,len=name.length();i<len;++i) {
                char ch = name.charAt(i);
                if(!(ch>='0' && ch<='9'))
                    return -1;
            }
            try {
                return Integer.parseInt(name);
            } catch (NumberFormatException e) {
                return -1;
            }
        }

        @Override
        String getName() {
            return name;
        }
    }

    private static final class IdentifierList extends Identifier{
        
        private String name;
        private final List<Identifier> list = new ArrayList<Identifier>();
        private final int id;
        
        IdentifierList(Identifier initial){
            /*
             * initial cannot be another IdentifierList
             * as the only syntax which will result in such concatenation is (a.b).(c.d)
             * and its illegal in any sense
             */
            this.list.add(initial);
            this.id = nextHashCode();
            this.name = combineName();
        }
        IdentifierList(IdentifierList copy){
            this.list.addAll(copy.list);
            this.id = nextHashCode();
            this.name = copy.name;
        }

        @Override
        Object dereference(Object root) {
            
            if(root instanceof Context) {
                Object cachedValue = ((Context) root).getCachedValue(this);
                if(cachedValue !=null ) 
                    return cachedValue;
            }
            
            Object result = root;
            List<Identifier> list = this.list;
            final int len = list.size();
            for(int k=0;k<len;++k) {
                Identifier id = list.get(k);
                result = id.dereference(result);
                if(result == null)
                    break;
            }
            
            if(result == null) {
                //class member reference
                String fastName = this.name;
                if( ! fastName.isEmpty()) {
                    Class<?> found = classCache.get(fastName);
                    if(found == null) {
                        try {
                            found = Class.forName(fastName);
                        } catch (ClassNotFoundException e) {
                            if(root instanceof Context) {
                                List<String> names = ((Context) root).getImplicitPackageNames();
                                for (int i = 0, len2 = names.size(); i < len2; ++i) {
                                    String packageName = names.get(i);
                                    try {
                                        found = Class.forName(packageName.concat(".").concat(fastName));
                                        break;
                                    } catch (ClassNotFoundException e1) {
                                    }
                                }
                            }
                        }
                    }
                    // $ is occupied by this feature for inner class reference
                    if(found != null && found != Identifier.class) {
                        classCache.put(fastName, found);
                        result = list.get(len-1).dereference(found);
                    }else {
                        classCache.put(fastName, Identifier.class);
                    }
                }
            }
            
            return result;
        }

        @Override
        void set(Object root, Object value) {
            
            int k = 0;
            List<Identifier> list = this.list;
            for(int len = list.size() - 1;k<len;++k) {
                Identifier id = list.get(k);
                root = id.dereference(root);
                if(root == null)
                    break;
            }
            if(root == null)
                throw new ParsingException("setting property on null object")
                    .withSiteAndOrdinal(Identifier.class, 8);
            list.get(k).set(root, value);
        }

        @Override
        Identifier add(Identifier next) {
            IdentifierList ret = new IdentifierList(this);
            ret.list.add(next);
            ret.name = ret.combineName();
            return ret;
        }

        @Override
        int getId() {
            return id;
        }
        
        @Override
        String getName() {
            return name + "." + list.get(list.size() - 1).getName();
        }
        
        private String combineName() {
            StringBuilder name1 = new StringBuilder();
            List<Identifier> list = this.list;
            for(int i=0; i<list.size()-1;++i) {
                name1.append('.').append(list.get(i).getName());
            }
            if(name1.length() == 0)
                return "";
            return name1.substring(1);
        }
    }
}
