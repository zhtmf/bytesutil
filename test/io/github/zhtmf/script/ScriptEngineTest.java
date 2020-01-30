package io.github.zhtmf.script;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.junit.Test;

public class ScriptEngineTest {
    
    private static final ScriptEngineManager MANAGER = new ScriptEngineManager();
    private static final ScriptEngine engine = MANAGER.getEngineByName("__zhtmf-script");
    
    public static void main(String[] args) throws ScriptException {
        ScriptEngine engine = MANAGER.getEngineByName("nashorn");
        ScriptContext sc = engine.getContext();
        engine.eval("a=3;a*3;");
        for(int b:sc.getScopes()) {
            System.out.println(b);
            System.out.println(sc.getBindings(b).entrySet());
        }
        System.out.println(engine.eval("a*3"));
    }
    
    @Test
    public void testEngineFactory() throws Exception{
        ZhtmfScriptEngineFactory factory = new ZhtmfScriptEngineFactory();
        assertTrue(factory.getNames().contains(factory.getLanguageName()));
        assertTrue(factory.getMimeTypes().isEmpty());
        assertTrue(factory.getExtensions().isEmpty());
        assertEquals(factory.getEngineVersion(), factory.getLanguageVersion());
        assertNotNull(factory.getLanguageVersion());
        assertNotNull(factory.getEngineVersion());
        assertNotNull(factory.getEngineName());
        
        assertEquals(factory.getParameter(ScriptEngine.NAME), factory.getNames());
        assertEquals(factory.getParameter(ScriptEngine.LANGUAGE_VERSION), factory.getLanguageVersion());
        assertEquals(factory.getParameter(ScriptEngine.LANGUAGE), factory.getLanguageName());
        assertEquals(factory.getParameter(ScriptEngine.ENGINE_VERSION), factory.getEngineVersion());
        assertEquals(factory.getParameter(ScriptEngine.ENGINE), factory.getEngineName());
        assertEquals(factory.getParameter("THREADING"), "STATELESS");
        
        assertNull(factory.getParameter("abc"));
        
        try {
            factory.getMethodCallSyntax(null, null);
            fail();
        } catch (UnsupportedOperationException e) {
        }
        try {
            factory.getOutputStatement(null);
            fail();
        } catch (UnsupportedOperationException e) {
        }
        assertEquals(factory.getProgram("a","b"), "a;b;");
    }
    
    @Test
    public void testEngine() throws Exception{
        SimpleScriptContext ssc = new SimpleScriptContext();
        ssc.setBindings(engine.createBindings(), ScriptContext.ENGINE_SCOPE);
        ssc.setAttribute("a",3,ScriptContext.ENGINE_SCOPE);
        assertEquals(engine.eval(new StringReader("a+a*3/10^5"), ssc), engine.eval("a+a*3/10^5", ssc));
        assertEquals(((Compilable)engine).compile(new StringReader("a+b+c"))
                    ,((Compilable)engine).compile("a+b+c"));
        
        CompiledScript cs = ((Compilable)engine).compile(new StringReader("a+b+c"));
        assertEquals(cs.getEngine(), engine);
        
        List<ScriptEngineFactory> factories = MANAGER.getEngineFactories();
        for(ScriptEngineFactory factory :factories) {
            if(factory instanceof ZhtmfScriptEngineFactory) {
                assertEquals(factory, engine.getFactory());
                break;
            }
        }
        
        try {
            StringReader reader = new StringReader("a+b");
            reader.close();
            engine.eval(reader);
        } catch (ScriptException e) {
            assertTrue(e.getCause() instanceof IOException);
        }
    }
}
