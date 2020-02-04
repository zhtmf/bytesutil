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
import io.github.zhtmf.script.ZhtmfScriptEngineFactory;

abstract class ScriptModifierHandler<E> extends ModifierHandler<E>{
    
    private final CompiledScript scriptSerialize;
    private final CompiledScript scriptDeserialize;
    private final Class<?> handlerClass;
    private static final ScriptEngine engine = new ZhtmfScriptEngineFactory().getScriptEngine();
    private static final ThreadLocal<Bindings> THREADLOCALBINDINGS = new ThreadLocal<Bindings>() {
        protected Bindings initialValue() {
            return engine.createBindings();
        };
    };
    
    public ScriptModifierHandler(String scriptSerialize, String scriptDeserialize, Class<E> handlerClass) throws ScriptException {
        Compilable compilable = (Compilable)engine;
        this.scriptSerialize = compilable.compile(scriptSerialize);
        this.scriptDeserialize = compilable.compile(scriptDeserialize);
        this.handlerClass = handlerClass;
    }

    @SuppressWarnings("unchecked")
    @Override
    public E handleDeserialize0(String fieldName, Object entity, InputStream in) throws IOException {
        Bindings bindings = THREADLOCALBINDINGS.get();
        bindings.put("fieldName", fieldName);
        bindings.put("entity", entity);
        try {
            return (E) convertIfNeeded(scriptSerialize.eval(bindings));
        } catch (ScriptException e) {
            throw new UnsatisfiedConstraintException("", e)
                .withSiteAndOrdinal(ScriptModifierHandler.class, 0);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public E handleSerialize0(String fieldName, Object entity) {
        Bindings bindings = THREADLOCALBINDINGS.get();
        bindings.put("fieldName", fieldName);
        bindings.put("entity", entity);
        try {
            return (E) convertIfNeeded(scriptDeserialize.eval(bindings));
        } catch (ScriptException e) {
            throw new UnsatisfiedConstraintException("", e)
                .withSiteAndOrdinal(ScriptModifierHandler.class, 1);
        }
    }
    
    private Object convertIfNeeded(Object obj) {
        if(obj == null)
            return obj;
        Class<?> resultClass = obj.getClass();
        Class<?> handlerClass = this.handlerClass;
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
