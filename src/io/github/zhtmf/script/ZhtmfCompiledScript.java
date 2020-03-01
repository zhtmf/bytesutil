package io.github.zhtmf.script;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

class ZhtmfCompiledScript extends CompiledScript{
    
    private final ScriptEngine engine;
    private final Script compiledScript;
    
    public ZhtmfCompiledScript(ScriptEngine engine, Script compiledScript) {
        this.engine = engine;
        this.compiledScript = compiledScript;
    }

    @Override
    public Object eval(ScriptContext context) throws ScriptException {
        List<Integer> scopes = context.getScopes();
        Map<String, Object> global = new HashMap<String, Object>();
        for(int i=0, len=scopes.size(); i<len; ++i) {
            Bindings bindings = context.getBindings(scopes.get(i));
            if(bindings != null) {
                global.putAll(bindings);
            }
        }
        return compiledScript.evaluate(global);
    }
    
    @Override
    public Object eval(Bindings bindings) throws ScriptException {
        return compiledScript.evaluate(bindings);
    }
    
    @Override
    public ScriptEngine getEngine() {
        return engine;
    }

    @Override
    public String toString() {
        return compiledScript.toString();
    }
}
