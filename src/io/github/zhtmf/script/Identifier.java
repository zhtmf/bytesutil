package io.github.zhtmf.script;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Reference to an object or a property of an object in the context, as well as
 * codes for getting/setting its value by reflection.
 * 
 * @author dzh
 */
class Identifier {
    
    private static final Field DUMMY_FIELD;
    private static final Method DUMMY_METHOD;
    private static final ConcurrentHashMap<String, Field> fields = new ConcurrentHashMap<String, Field>();
    private static final ConcurrentHashMap<String, Method> setters = new ConcurrentHashMap<String, Method>();
    private static final ConcurrentHashMap<String, Method> getters = new ConcurrentHashMap<String, Method>();
    private static final ConcurrentHashMap<String, Class<?>> classCache = new ConcurrentHashMap<String, Class<?>>();
    private static final ThreadLocal<Integer> NEXT_HASH_CODE = new ThreadLocal<Integer>() {
        protected Integer initialValue() {return 0;};
    };
    static {
        try {
            DUMMY_FIELD = Identifier.class.getDeclaredField("unused");
            DUMMY_METHOD = Identifier.class.getDeclaredMethod("unused");
        } catch (Exception e) {
            throw new InternalError(e);
        }
    }
    private static int nextHashCode() {
        Integer current = NEXT_HASH_CODE.get();
        int ret = current + 1;
        NEXT_HASH_CODE.set(ret);
        return ret;
    }
    
    
    @SuppressWarnings("unused")
    private String unused;
    @SuppressWarnings("unused")
    private void unused() {throw new UnsupportedOperationException();};
    
    final List<String> list;
    /**
     * A string constructed by joining all but the last identifier string with dot
     * for checking whether it is a class through <tt>Class.forName</tt>
     */
    private String fastName;
    
    /**
     * the only string in this identifier parsed as a numeric index, used in
     * {@link #set(Object, Object) set}.<br/>
     * -1 if it is not a numeric string.
     */
    private int fastIndex;
    
    private final int hashCode = nextHashCode();
    
    private Identifier() {
        this.list = new ArrayList<String>();
    }
    
    public Identifier(String name) {
        this.list = Collections.singletonList(name);
        init();
    }
    
    public Identifier concat(String next) {
        Identifier ret = new Identifier();
        List<String> list = ret.list;
        list.addAll(this.list);
        list.add(next);
        ret.init();
        return ret;
    }
    
    public Identifier concat(Identifier next) {
        Identifier ret = new Identifier();
        List<String> list = ret.list;
        list.addAll(this.list);
        list.addAll(next.list);
        ret.init();
        return ret;
    }
    
    private void init() {
        List<String> list = this.list;
        this.fastName = getFastName(list);
        this.fastIndex = tryParseIndex(list.get(list.size()-1));
    }
    
    public Object dereference(Object root) {
        if(root instanceof Context) {
            Object cachedValue = ((Context) root).getCachedValue(this);
            if(cachedValue !=null ) 
                return cachedValue;
        }
        Object result = dereference0(root, list);
        if(result != null)
            return result;
        String fastName = this.fastName;
        List<String> list = this.list;
        if( ! fastName.isEmpty()) {
            Class<?> found = classCache.get(fastName);
            if(found == null) {
                try {
                    found = Class.forName(fastName);
                } catch (ClassNotFoundException | LinkageError e) {
                    if(root instanceof Context) {
                        List<String> names = ((Context) root).getImplicitPackageNames();
                        for (int i = 0, len = names.size(); i < len; ++i) {
                            String packageName = names.get(i);
                            try {
                                found = Class.forName(packageName + "." + fastName);
                                break;
                            } catch (ClassNotFoundException | LinkageError e1) {
                            }
                        }
                    }
                }
            }
            // $ is occupied by this feature for inner class reference
            if(found != null && found != Identifier.class) {
                classCache.put(fastName, found);
                root = found;
                list = Collections.singletonList(list.get(list.size() - 1));
                return dereference0(root, list);
            }else {
                classCache.put(fastName, Identifier.class);
            }
        }
        return null;
    }
    
    /**
     * More generic method for getting a value from context. Mainly used by []
     * operator
     * 
     * @param root  context object
     * @param list  identifier string list
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Object dereference0(Object root, List<String> list) {
        for(int i = 0, len = list.size(); i < len; ++i) {
            String name = list.get(i);
            if(root == null) {
                return null;
            }
            int index = tryParseIndex(name);
            if(index>=0) {
                if(getClassOf(root).isArray()) {
                    root = Array.get(root, index);
                }else if(!(root instanceof List)){
                    throw new ParsingException("indexing "+index+" on non-list object "+root)
                        .withSiteAndOrdinal(Identifier.class, 1);
                }else {
                    root = ((List<Map<String,Object>>)root).get(index);
                }
            }else if(root instanceof Map){
                if("length".equals(name) || "size".equals(name)) {
                    root = new BigDecimal(((Map)root).size());
                    continue;
                }
                root = ((Map<String,Object>)root).get(name);
            }else {
                //special treatment for length and size
                if("length".equals(name) || "size".equals(name)) { 
                    if(root instanceof String) {
                        root = new BigDecimal(((String)root).length());
                        continue;
                    }
                    else if(root instanceof Collection) {
                        root = new BigDecimal(((Collection)root).size());
                        continue;
                    }
                    else if(getClassOf(root).isArray()) {
                        root = new BigDecimal(Array.getLength(root));
                        continue;
                    }
                }
                
                Method getter = getGetter(root,name);
                if(getter != null) {
                    try {
                        root = getter.invoke(root);
                    } catch (Exception e) {
                        throw new ParsingException("exception in calling getter for property "+name+" on object "+root, e)
                            .withSiteAndOrdinal(Identifier.class, 2);
                    }
                }else {
                    Field field = getField(root, name);
                    if(field != null) {
                        try {
                            root = field.get(root);
                            continue;
                        } catch (IllegalArgumentException | IllegalAccessException e) {
                            throw new ParsingException("exception in accessing property "+name+" on object "+root, e)
                            .withSiteAndOrdinal(Identifier.class, 3);
                        }
                    }
                    
                    root = null;
                }
            }
        }
        return root;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void set(Object root, Object value) {
        List<String> list = this.list;
        String name = list.get(list.size()-1);
        Object parent = list.size() == 1 ? root : dereference0(root, list.subList(0, list.size()-1));
        if(parent == null)
            throw new ParsingException("setting property on null object")
                .withSiteAndOrdinal(Identifier.class, 8);
        int index = this.fastIndex;
        if(index>=0) {
            Class<?> parentClass = getClassOf(parent);
            if(parentClass.isArray()) {
                Array.set(parent, index, convertValueIfNeeded(parentClass.getComponentType(), value));
                return;
            } else if(parent instanceof List){
                ((List)parent).set(index, value);
                return;
            }
        }
        if(parent instanceof Map){
            ((Map<String,Object>)parent).put(name, value);
        }else {
            if(index >=0 ) {
                throw new ParsingException("indexing"+index+" on object "+root)
                    .withSiteAndOrdinal(Identifier.class, 4);
            }
            Method setter = getSetter(parent, name);
            if(setter != null) {
                try {
                    setter.invoke(parent, convertValueIfNeeded(setter.getParameterTypes()[0], value));
                    return;
                } catch (Exception e) {
                    throw new ParsingException("exception in calling setter for property "+name+" on object "+root, e)
                        .withSiteAndOrdinal(Identifier.class, 5);
                }
            }else {
                Field field = getField(parent, name);
                if(field != null) {
                    try {
                        field.set(parent, convertValueIfNeeded(field.getType(), value));
                        return;
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        throw new ParsingException("exception in accessing property "+name+" on object "+root, e)
                        .withSiteAndOrdinal(Identifier.class, 6);
                    }
                }
                
                throw new ParsingException("no property named "+name+" found on object "+root)
                .withSiteAndOrdinal(Identifier.class, 7);
            }
        }
    }
    
    @Override
    public String toString() {
        return "ID["+list+"]";
    }
    
    @Override
    public int hashCode() {
        return hashCode;
    }
    
    private Object convertValueIfNeeded(Class<?> fieldClass, Object scriptValue) {
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
    
    private static int tryParseIndex(String name) {
        try {
            return Integer.parseInt(name);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    private static Method getGetter(Object root, String property) {
        Class<?> cls = getClassOf(root);
        String key = cls.getSimpleName()+" "+property;
        Method getter = getters.get(key);
        if(getter == DUMMY_METHOD) {
            return null;
        }
        if(getter == null) {
            String initialUppercased = Character.toUpperCase(property.charAt(0))+property.substring(1);
            try {
                getter = cls.getMethod("get"+initialUppercased);
                getters.put(key, getter);
            } catch (Exception e) {
                try {
                    getter = cls.getMethod("is"+initialUppercased);
                    getters.put(key, getter);
                } catch (Exception e1) {
                    //try to find method with exactly that name 
                    //and have no parameters
                    try {
                        getter = cls.getMethod(property);
                        getters.put(key, getter);
                    } catch (Exception e2) {
                        getters.put(key, DUMMY_METHOD);
                    }
                }
            }
        }
        return getter;
    }
    
    private static Method getSetter(Object root, String property) {
        Class<?> cls = getClassOf(root);
        String key = cls.getSimpleName()+" "+property;
        Method setter = setters.get(key);
        if(setter == DUMMY_METHOD) {
            return null;
        }
        if(setter == null) {
            String methodName = "set"+Character.toUpperCase(property.charAt(0))+property.substring(1);
            for(Method method:cls.getMethods()) {
                if(method.getName().matches(methodName)
                && method.getParameterCount() == 1 
                && method.getReturnType() == void.class) {
                    setter = method;
                    break;
                }
            }
            if(setter == null) {
                setters.put(key, DUMMY_METHOD);
            }else {
                setters.put(key, setter);
            }
        }
        return setter;
    }
    
    private static Field getField(Object root, String property) {
        Class<?> cls = getClassOf(root);
        String key = cls.getSimpleName()+" "+property;
        Field field = fields.get(key);
        if(field == DUMMY_FIELD) {
            return null;
        }
        if(field == null) {
            try {
                field = cls.getDeclaredField(property);
                field.setAccessible(true);
                fields.put(key, field);
            } catch (Exception e) {
                fields.put(key, DUMMY_FIELD);
            }
        }
        return field;
    }
    
    private static Class<?> getClassOf(Object root){
        return root instanceof Class ? (Class<?>) root : root.getClass();
    }
    
    private static String getFastName(List<String> list) {
        StringBuilder name1 = new StringBuilder();
        for(int i=0; i<list.size()-1;++i) {
            name1.append('.').append(list.get(i));
        }
        if(name1.length() == 0)
            return "";
        return name1.substring(1);
    }
}