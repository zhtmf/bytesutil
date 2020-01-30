package io.github.zhtmf.script;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Map-like container holding global values which provides a context for 
 * script evaluation.
 * <p>
 * Property names are case sensitive.
 * 
 * @author dzh
 */
class Context extends AbstractMap<String, Object> {
    
    /**
     * At runtime, operands (identifiers, strings etc.) are pushed onto this stack
     * while operators pop them out, do calculation and push result back to it, if
     * any.
     */
    private final LinkedList<Object> operandStack = new LinkedList<>();
    
    
    /**
     * Actual map holding mappings of values
     */
    private Map<String,Object> values = new HashMap<String, Object>();
    
    /**
     * package names derived from classes in values which are used in implicit class
     * member access. This one is lazily initialized.
     */
    private boolean implicitPakageNamesFilled = false;
    private List<String> implicitPackageNames;
    
    private Map<Integer,Object> cachedIdentifierValues = new HashMap<Integer, Object>();
    
    /**
     * Initialize this context with values from a map
     * 
     * @param initialMap initial mappings in this context.
     */
    public Context(Map<String,Object> initialMap) {
        this.values.putAll(initialMap);
    }
    
    /**
     * Cache its value on first encounter to deal with operators which modifies
     * value of identifiers in place.
     * <p>
     * Without calling this method, b + b++ will be 11 not 10 and likely b + b++ + b
     * + b++ + b++ will be 24 instead of 23. Although 11 and 24 are exactly what
     * returns by C compiler we must be compatible with Java compilers.
     * 
     * @param ctx context object
     */
    void cacheValue(Identifier id) {
        /*
         * do not distinguish between absence of value and null,
         * null value from dereference may not be final result,
         * as the identifier itself may represent a non-exist value in the context 
         * but it can be used to dereference another value (for example a string)
         */
        if(!cachedIdentifierValues.containsKey(id.hashCode())) {
            cachedIdentifierValues.put(id.hashCode(), id.dereference(this));
        }
    }
    
    Object getCachedValue(Identifier id) {
        return cachedIdentifierValues.get(id.hashCode());
    }
    
    /**
     * Peek the first operand.
     * 
     * @return the first operand or null if there isn't any.
     */
    Object peek() {
        return operandStack.peek();
    }
    
    /**
     * Pop and return the first operand.
     * 
     * @return the first operand or null if there isn't any.
     */
    Object pop() {
        return operandStack.poll();
    }
    
    /**
     * Push an operand onto the operand stack.
     * @param operand
     */
    void push(Object operand) {
        operandStack.push(operand);
    }

    /**
     * Recursively get an property in this context all one of its parents.
     */
    public Object get(Object name) {
        return values.get(name);
    }
    
    public Object put(String name,Object value) {
        values.put(name, value);
        return null;
    }
    
    @Override
    public Set<Entry<String, Object>> entrySet() {
        return values.entrySet();
    }
    
    /**
     * Returns implicit package names derived from current set of values. Initialize
     * the list if necessary.
     * <p>
     * Objects in current {@link #values} are iterated and checked for their
     * packages, if a package name does not start with "java" then it is valid.
     * <p>
     * As this script does not permit object creation and implicit objects created
     * by this script are all under java package, which is considered invalid, so it
     * is OK to lazily initialize the list using "current" set of values.
     * 
     * @return <tt>immutable</tt> list of implicit package names.
     */
    List<String> getImplicitPackageNames() {
        if(!implicitPakageNamesFilled) {
            fillImplicitPackageNames(this.values);
        }
        return implicitPackageNames;
    }
    
    private void fillImplicitPackageNames(Map<String, Object> objects) {
        List<String> implicitPackageNames = new ArrayList<String>();
        for(Object obj:objects.values()) {
            if(obj == null)
                continue;
            Package package1 = obj.getClass().getPackage();
            if(package1 == null)
                continue;
            String name = package1.getName();
            if(!name.startsWith("java")) {
                implicitPackageNames.add(name);
            }
        }
        implicitPakageNamesFilled = true;
        this.implicitPackageNames = Collections.unmodifiableList(implicitPackageNames);
    }
}
