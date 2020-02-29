package io.github.zhtmf.script;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base class that represents an identifier and provides associated operations
 * 
 * @author dzh
 */
abstract class Identifier {
    
    /**
     * The string representation mainly for debug purposes. Which can be same or
     * different from <tt>name</tt> property.
     * 
     * @return the string representation.
     */
    abstract String getName();

    /**
     * Retrieves the associated property value of this identifier in the specified
     * root object. Typically the instance field value of this object or entry value
     * in this map.
     * 
     * @param root the object to refer to.
     * @return the associated property value.
     */
    abstract Object dereference(Object root);

    /**
     * Set the value of associated property in the specified root object. Typically
     * set the instance field value in an object or entry value in a map.
     * 
     * @param root the object to refer to.
     * @param value the value to set.
     */
    abstract void set(Object root, Object value);

    /**
     * Concatenates this identifier and <tt>next</tt> to form a new identifier which
     * represents a property path in an object.
     * <p>
     * This method returns a sub class of {@link Identifier}. It is defined here for
     * providing a unified interface.
     * 
     * @param next the next identifier in the property path, which is concatenated
     *        to this one.
     * @return a concatenated identifier.
     */
    abstract Identifier add(Identifier next);
    
    /**
     * The id of this identifier which distinguishes an identifier from another one
     * in the same {@link Context}.
     * <p>
     * Because of the stack-based evaluation of this script, two identifier must be
     * treated as different ones even they have the same name or prefix/suffix
     * increment/decrement operators will produces different results with Java
     * compilers. So every identifier must have a unique id.
     * <p>
     * It is used by {@link Context#push(String, Object)} for an internal "cache".
     * 
     * @return the unique id, always allocated during construction.
     */
    abstract int getId();
    
    /**
     * Call the method probably represented by this identifier.
     * <p>
     * This method contains a different code path which dereferences an identifier
     * so it does not interfere with existing property looking up logic.
     * <p>
     * Obviously this feature only supports calling method with parameters of a set
     * of limited java types with corresponding types in this script.However
     * <tt>null</tt> literal can be used to call any methods with parameters with
     * non-primitive types.
     * <p>
     * When calling method on a variable, only instance methods are checked for
     * validity. In contrast, when calling method on the full qualified name of a
     * class, only static methods are checked.
     * <p>
     * As for calling overloaded methods, similar to what JSR states, an algorithm
     * is applied in looking up a single candidate method:
     * <ol>
     * <li>Lookup candidate methods from public/protected methods in this class and
     * inherited ones.</li>
     * <li>If no candidate methods are found, an exception is thrown to indicate the
     * failure.</li>
     * <li>Within the candidates, remove those with a different number of parameters
     * than the method call expression.</li>
     * <li>Then for every remaining methods, try to find the <tt>most specific</tt>
     * method. Method A is more specific than method B if for formal parameters
     * S1,S2,S3...Sn from method A and formal parameters T1,T2,T3...Tn from method
     * b, there is Si > Ti for 1<= i <= n.
     * <p>
     * A type S is more specific than type T regarding method calls if:
     * <ul>
     * <li>The actual argument in this script is of type {@link TokenType#NUM} and S
     * comes earlier than T in the sequence
     * <tt>BigDecimal, double, Double, float, Float, 
        BigInteger, long, Long, int, Integer, short, Short, byte, Byte</tt> or S
     * matches any of these types but T does not.</li>
     * <li>The actual argument in this script is of type {@link TokenType#BOOL} and
     * S comes earlier than T in the sequence <tt>boolean, Boolean</tt> or S matches
     * any of these types but T does not.
     * <li>The actual argument in this script is of type {@link TokenType#STR} and S
     * is of type java.lang.String but T is not.</li>
     * <li>The actual argument in this script is of type {@link TokenType#NULL} and
     * S is of a non-primitive type but T is primitive.</li>
     * </ul>
     * </li>
     * <li>If in the previous step all candidate methods fail to pass the test, the
     * call fails. If multiple candidates remains, the call is ambiguous. In both
     * cases, exception is thrown to indicate the failure.</li>
     * <li>Otherwise, the lookup result is remembered and cached using the key
     * formed by class of the object, method name and all actual arguments types in
     * the script.</li>
     * </ol>
     * 
     * @param root the object which possesses the method.
     * @param parameters formal parameters from the script.
     * @param parameterTypes types of this script for those formal parameters
     * @return result of the method call if success.
     */
    abstract Object call(Object root, Object[] parameters, TokenType[] parameterTypes);

    @Override
    public String toString() {
        return "ID["+getName()+"]";
    }

    /**
     * Static factory method for hiding concrete implementation class.
     * 
     * @param name the property name.
     * @return an implementation.
     */
    static Identifier of(String name) {
        return new SingleIdentifier(name);
    }
    
    /**
     * Make string literal "callable" without removing length/size pseudo
     * property reference logic
     * 
     * @param str   the string literal.
     * @return  an implementation.
     */
    static Identifier ofLiteral(String str) {
        return new StringLiteralIdentifier(str);
    }

    /**
     * Common interface for retrieving value of a property, by utilizing
     * {@link Field} or {@link Method} or by any other means.
     */
    @FunctionalInterface
    private interface Getter{
        /*
         * The second parameter is useless in most cases as it is implied in a Field or
         * Method. But when indexing a list or an array, the actual index may be
         * different for each call so we cannot simply use the one captured in the
         * closure.
         */
        Object get(Object obj, SingleIdentifier propertyName) throws Exception;
    }
    
    /**
     * Common interface for setting value of a property, by utilizing
     * {@link Field} or {@link Method} or by any other means.
     */
    @FunctionalInterface
    private interface Setter{
        void set(Object obj, SingleIdentifier propertyName, Object value) throws Exception;
    }
    
    private static final class MethodObject{
        private Method method;
        final String name;
        final Class<?>[] parameterTypes;
        public MethodObject(Method method) {
            this.method = method;
            this.name = method.getName();
            this.parameterTypes = method.getParameterTypes();
        }
        public Object invoke(Object obj, Object... args) {
            try {
                return method.invoke(obj, args);
            } catch (Exception e) {
                throw new ParsingException("exception when calling method " + name + "from script")
                    .withSiteAndOrdinal(Identifier.class, 10);
            }
        }
    }
    
    private static final ConcurrentHashMap<String, MethodObject> METHOD_CALLS = new ConcurrentHashMap<String, MethodObject>();
    private static final ConcurrentHashMap<String, Getter> GETTERS = new ConcurrentHashMap<String, Getter>();
    private static final Getter DUMMY = (obj,p)->null;
    private static final ConcurrentHashMap<String, Setter> SETTERS = new ConcurrentHashMap<String, Setter>();
    private static final Setter DUMMY2 = (o,p,v)->{};
    private static final ConcurrentHashMap<String, Class<?>> classCache = new ConcurrentHashMap<String, Class<?>>();
    private static final ThreadLocal<Integer> NEXT_ID = new ThreadLocal<Integer>() {
        protected Integer initialValue() {return 0;};
    };
    private static int nextId() {
        Integer current = NEXT_ID.get();
        int ret = current + 1;
        NEXT_ID.set(ret);
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
            return "java.util.List.indexing";
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
            //dedicated code path for length and size
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
                //and no parameters
                try {
                    return cls.getMethod(property.name);
                } catch (Exception e2) {
                    //try to find inherited, non-private methods
                    Class<?> current = cls.getSuperclass();
                    while(current!= null && current != Object.class) {
                        try {
                            Method method = current.getDeclaredMethod(property.name);
                            int mod = method.getModifiers();
                            if(Modifier.isProtected(mod) || Modifier.isPublic(mod)) {
                                method.setAccessible(true);
                                return method;
                            }
                        } catch (NoSuchMethodException | SecurityException e3) {
                        }
                        current = current.getSuperclass();
                    }
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
    
    private static MethodObject getMostSpecificMethod(
            Object[] parameters, TokenType[] parameterTypes, Object root, String name) {
        Class<?> clazz = getClassOf(root);
        StringBuilder key = new StringBuilder();
        key.append(clazz.getName()).append(' ').append(name).append(' ');
        for(int k = 0, l = parameterTypes.length; k < l; ++k) {
            key.append(parameterTypes[k].name());
        }
        String keyStr = key.toString();
        MethodObject method = METHOD_CALLS.get(keyStr);
        if(method != null)
            return method;
        //cannot return null
        method = new MethodObject(
                getMostSpecificMethod0(
                        parameters, parameterTypes, clazz, name, root instanceof Class));
        METHOD_CALLS.put(keyStr, method);
        return method;
    }
    
    private static Method getMostSpecificMethod0(Object[] parameters, TokenType[] scriptTypes, Class<?> clazz, String name, boolean staticOnly) {
        List<Method> candidates = new ArrayList<Method>();
        List<String> scores = new ArrayList<String>();
        StringBuilder score = new StringBuilder();
        int mask = staticOnly ? Modifier.STATIC : 0;
        while(clazz != Object.class) {
            Method[] methods = clazz.getDeclaredMethods();
            for(Method method:methods) {
                if(!method.getName().equals(name))
                    continue;
                int mod = method.getModifiers();
                if((mod & Modifier.PUBLIC) == 0 && (mod & Modifier.PROTECTED) == 0)
                    continue;
                if((mod & Modifier.STATIC) != mask)
                    continue;
                Class<?>[] types = method.getParameterTypes();
                if(types.length == parameters.length) {
                    //candidate
                    //calculate scores
                    if(types.length == 0) {
                        score.append('A');
                    }else {
                        for(int p = 0, l = types.length; p<l; ++p) {
                            char result = isConvertible(types[p], scriptTypes[p]);
                            if(result == 0) {
                                score.setLength(0);
                                break;
                            }
                            score.append(result);
                        }
                    }
                    if(score.length() > 0) {
                        method.setAccessible(true);
                        candidates.add(method);
                        scores.add(score.toString());
                        score.setLength(0);
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
        if(scores.size() > 0) {
            /*
             * Similar to what JavaSE specification states about choosing the most specific
             * method. But we does not take generic parameters or return type into
             * consideration.
             * 
             * Method A is more specific than method B if for all its formal arguments the
             * type is of higher precedence than method B's counterpart, namely the
             * character in the method A's score string comes earlier than corresponding
             * character of method B's in alphabetical order.
             * 
             * If no single maximally specific method exists, follow Nashorn's suit and throw an exception.
             */
            for(int p = 0;p<scores.size(); ++p) {
                String max = scores.get(p);
                boolean result = true;
                for(int m = 0;m<scores.size();++m) {
                    if(p != m) {
                        if(!strictlyGreaterThan(max, scores.get(m))) {
                            result = false;
                            break;
                        }
                    }
                }
                if(result)
                    return candidates.get(p);
            }
            //throw exception
            throw new ParsingException("cannot call method "+name
                    +" by unambiguously select between multiple valid signatures "+candidateParameterErrorMessage(candidates)
                    +" for argument types "+Arrays.toString(scriptTypes))
            .withSiteAndOrdinal(Identifier.class, 11);
        }
        throw new ParsingException("cannot call method "+name
                +" by select valid signature from "+candidateParameterErrorMessage(candidates)
                +" for argument types "+Arrays.toString(scriptTypes))
        .withSiteAndOrdinal(Identifier.class, 12);
    }
    
    private static String candidateParameterErrorMessage(List<Method> candidates) {
        StringBuilder message = new StringBuilder();
        for(Method candidate : candidates) {
            message.append(Arrays.toString(candidate.getParameterTypes())).append(",");
        }
        return message.toString();
    }
    
    private static boolean strictlyGreaterThan(String score1, String score2) {
        boolean result = true;
        for(int p = 0; p < score1.length(); ++p) {
            char c1 = score1.charAt(p);
            char c2 = score2.charAt(p);
            if(c1 > c2) {
                result = false;
                break;
            }
        }
        return result;
    }
    
    private static Class<?> getClassOf(Object root){
        //supports static fields
        return root instanceof Class ? (Class<?>) root : root.getClass();
    }
    
    private static Class<?>[] NUM_CONVERTIBLE_TYPES = new Class<?>[] { 
        BigDecimal.class,
        double.class, Double.class, float.class,Float.class, 
        BigInteger.class, long.class, Long.class, int.class, Integer.class, 
        short.class, Short.class, byte.class,Byte.class, };
        
    static char isConvertible(Class<?> paramType, TokenType scriptType) {
        switch (scriptType) {
        case STR:
            return paramType == String.class ? 'A' : 0;
        case BOOL:
            return paramType == boolean.class ? 'A' : paramType == Boolean.class ? 'B' : 0;
        case NULL:
            return !paramType.isPrimitive() ? 'A' : 0;
        case NUM:
            for(int p = NUM_CONVERTIBLE_TYPES.length-1; p>=0;--p) {
                if(paramType == NUM_CONVERTIBLE_TYPES[p])
                    return (char) (p + 'A');
            }
            return 0;
        default:
            return 0;
        }
    }
    
    /**
     * Try to convert a value to specified type.
     * <p>
     * This is for making internal representation compatible with more frequently
     * used types from user codes. Currently this method only converts BigDecimals
     * to other numeric types.
     * 
     * @param fieldClass the type to convert <tt>scriptValue</tt> to.
     * @param scriptValue the object generated from this script engine.
     * @return successfully converted object or the original one on failure.
     */
    static Object convertValueIfNeeded(Class<?> fieldClass, Object scriptValue) {
        if(!(scriptValue instanceof BigDecimal)) {
            return scriptValue;
        }
        if(fieldClass == BigDecimal.class) {
            return scriptValue;
        }
        BigDecimal numValue = (BigDecimal) scriptValue;
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
        return scriptValue;
    }
    
    private static final class StringLiteralIdentifier extends Identifier{
        
        private final String str;
        private final int id;
        public StringLiteralIdentifier(String str) {
            this.str = str;
            this.id = -str.hashCode();
        }

        @Override
        String getName() {
            return str;
        }

        @Override
        Object dereference(Object root) {
            return str;
        }

        @Override
        void set(Object root, Object value) {
            throw new ParsingException("setting properties on a string literal")
                .withSiteAndOrdinal(StringLiteralIdentifier.class, 0);
        }

        @Override
        Identifier add(Identifier next) {
            return new IdentifierList(this).add(next);
        }

        @Override
        int getId() {
            return id;
        }

        @Override
        Object call(Object root, Object[] parameters, TokenType[] parameterTypes) {
            throw new UnsupportedOperationException();
        }
        
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
            this.id = nextId();
            String initialUppercased = name.isEmpty() ? "" : Character.toUpperCase(name.charAt(0))+name.substring(1);
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

        @Override
        Object call(Object root, Object[] parameters, TokenType[] parameterTypes) {
            throw new ParsingException("method "+this.name+" not found on global object")
                .withSiteAndOrdinal(Identifier.class, 7);
        }
    }
    
    //TODO: support setting static fields
    //TODO: revamp similar logic in three main methods

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
            this.id = nextId();
            this.name = combineName();
        }
        IdentifierList(IdentifierList copy){
            this.list.addAll(copy.list);
            this.id = nextId();
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
                Object found = findClassByFQN(root);
                if(found != null && found != Identifier.class) {
                    result = list.get(len-1).dereference(found);
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
            //concatenate all names but the last
            StringBuilder name1 = new StringBuilder();
            List<Identifier> list = this.list;
            for(int i=0; i<list.size()-1;++i) {
                name1.append('.').append(list.get(i).getName());
            }
            if(name1.length() == 0)
                return "";
            return name1.substring(1);
        }
        @Override
        Object call(Object root, Object[] parameters, TokenType[] parameterTypes) {
            List<Identifier> list = this.list;
            for(int k = 0, len = list.size() - 1;k<len;++k) {
                Identifier id = list.get(k);
                root = id.dereference(root);
                if(root == null)
                    break;
            }
            if(root == null)
                root = findClassByFQN(root);
            if(root == null)
                throw new ParsingException("calling method on null object")
                    .withSiteAndOrdinal(Identifier.class, 9);
            //k is not necessarily list.size()-1 at this point
            String name = list.get(list.size()-1).getName();
            MethodObject method = getMostSpecificMethod(parameters, parameterTypes, root, name);
            Class<?>[] types = method.parameterTypes;
            for(int n = 0, l = parameters.length;n<l;++n) {
                parameters[n] = convertValueIfNeeded(types[n], parameters[n]);
            }
            return method.invoke(root, parameters);
        }
        
        //support class member reference
        private Object findClassByFQN(Object root) {
            String fastName = this.name;
            Class<?> found = classCache.get(fastName);
            if(found == Identifier.class)
                return null;
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
            if(found != null) {
                classCache.put(fastName, found);
            }else {
                classCache.put(fastName, Identifier.class);
            }
            
            return found;
        }
    }
}
