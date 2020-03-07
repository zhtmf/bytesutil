package io.github.zhtmf.converters;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import io.github.zhtmf.converters.auxiliary.ModifierHandler;

/**
 * Implementation of script-based {@link ModifierHandler}s.
 * 
 * @author dzh
 */
class ScriptModifierHandler<E> extends ModifierHandler<E>{
    
    private final CompiledScript scriptSerialize;
    private final CompiledScript scriptDeserialize;
    private final Class<?> handlerClass;
    private static final ScriptEngine engine = new ScriptEngineManager().getEngineByName("__zhtmf-script");
    
    public ScriptModifierHandler(String scriptSerialize, String scriptDeserialize, Class<E> handlerClass) throws ScriptException {
        Compilable compilable = (Compilable)engine;
        this.scriptSerialize = scriptSerialize.isEmpty() ? null : compilable.compile(scriptSerialize);
        this.scriptDeserialize = scriptDeserialize.isEmpty() ? null : compilable.compile(scriptDeserialize);
        this.handlerClass = handlerClass;
    }

    @SuppressWarnings("unchecked")
    @Override
    public E handleDeserialize0(String fieldName, Object entity, InputStream in) throws IOException {
        if(scriptDeserialize == null)
            throw new UnsatisfiedIOException(
                    "script is absent, which may means this script should not be called during deserialization")
                        .withSiteAndOrdinal(ScriptModifierHandler.class, 3);
        Bindings bindings = engine.createBindings();
        bindings.put("fieldName", fieldName);
        bindings.put("entity", entity);
        bindings.put("handler", this);
        try {
            return (E) convertIfNeeded(scriptDeserialize.eval(bindings), this.handlerClass);
        } catch (ScriptException e) {
            throw new UnsatisfiedConstraintException("", e)
                .withSiteAndOrdinal(ScriptModifierHandler.class, 0);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public E handleSerialize0(String fieldName, Object entity) {
        if(scriptSerialize == null)
            throw new UnsatisfiedConstraintException(
                    "script is absent, which may means this script should not be called during serialization")
                        .withSiteAndOrdinal(ScriptModifierHandler.class, 4);
        Bindings bindings = engine.createBindings();
        bindings.put("fieldName", fieldName);
        bindings.put("entity", entity);
        bindings.put("handler", this);
        try {
            return (E) convertIfNeeded(scriptSerialize.eval(bindings), this.handlerClass);
        } catch (ScriptException e) {
            throw new UnsatisfiedConstraintException("", e)
                .withSiteAndOrdinal(ScriptModifierHandler.class, 1);
        }
    }
    
    private Object convertIfNeeded(Object obj, Class<?> handlerClass) {
        if(obj == null)
            return obj;
        Class<?> resultClass = obj.getClass();
        if(resultClass == handlerClass)
            return obj;
        if(handlerClass == Integer.class
          && (Number.class.isAssignableFrom(resultClass)))
            return ((Number)obj).intValue();
        if(handlerClass == Charset.class
          && resultClass == String.class)
            return Charset.forName(obj.toString());
        throw new UnsatisfiedConstraintException("script should return a result of type "+handlerClass)
        .withSiteAndOrdinal(ScriptModifierHandler.class, 2);
    }
}
