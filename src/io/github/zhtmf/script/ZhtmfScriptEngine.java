package io.github.zhtmf.script;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

class ZhtmfScriptEngine extends AbstractScriptEngine implements Compilable{
    
    private ConcurrentHashMap<String, CompiledScript> compiledScripts = 
            new ConcurrentHashMap<String, CompiledScript>();
    
    private final ScriptEngineFactory factory;
    
    public ZhtmfScriptEngine(ScriptEngineFactory factory) {
        this.factory = factory;
    }

    @Override
    public Object eval(String script, ScriptContext context) throws ScriptException {
        return compile(script).eval(context);
    }

    @Override
    public Object eval(Reader reader, ScriptContext context) throws ScriptException {
        return eval(readertoString(reader), context);
    }
    
    @Override
    public Object eval(String script, Bindings bindings) throws ScriptException {
        return compile(script).eval(bindings);
    }
    
    @Override
    public Object eval(Reader reader, Bindings bindings) throws ScriptException {
        return eval(readertoString(reader), bindings);
    }

    @Override
    public Bindings createBindings() {
        return new SimpleBindings();
    }

    @Override
    public CompiledScript compile(String script) throws ScriptException {
        Map<String, CompiledScript> compiledScripts = this.compiledScripts;
        CompiledScript scriptObject = compiledScripts.get(script);
        
        if(scriptObject != null) {
            return scriptObject;
        }
        
        try {
            scriptObject = new ZhtmfCompiledScript(this, new Script(script).compile());
        } catch (ParsingException e) {
            throw new ScriptException(e);
        }
        compiledScripts.putIfAbsent(script, scriptObject);
        return scriptObject;
    }

    @Override
    public CompiledScript compile(Reader script) throws ScriptException {
        return compile(readertoString(script));
    }
    
    @Override
    public ScriptEngineFactory getFactory() {
        return factory;
    }
    
    private String readertoString(Reader reader) throws ScriptException {
        StringBuilder string = new StringBuilder();
        char[] buff = new char[32];
        int read = 0;
        try {
            while((read = reader.read(buff))!=-1) {
                string.append(buff, 0, read);
            }
        } catch (IOException e) {
            throw new ScriptException(e);
        }
        return string.toString();
    }
}
