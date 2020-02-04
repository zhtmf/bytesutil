package io.github.zhtmf.converters;

import java.nio.charset.Charset;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import io.github.zhtmf.converters.auxiliary.ModifierHandler;

public class TestMain {
    
    static private class Entity{
        @SuppressWarnings("unused")
        private int a = 3;
    }
    
    public static void main(String[] args) throws Exception {
        for(;;){
            {
                Entity entity = new Entity();
                ModifierHandler<Charset> mod = new ScriptModifierHandler<Charset>("entity.a>0 ? 'UTF-8' : 'GBK'","entity.a>0 ? 'UTF-8' : 'GBK'",Charset.class) {
                };
                long st = System.currentTimeMillis();
                for(int i=0;i<100000;++i) {
                    mod.handleSerialize0("a", entity);
                }
                System.out.println(System.currentTimeMillis() - st);
            }
            {
                Entity entity = new Entity();
                ScriptEngine nashorn = new ScriptEngineManager().getEngineByName("nashorn");
                CompiledScript cs = ((Compilable)nashorn).compile("entity.a>0 ? 'UTF-8' : 'GBK'");
                ScriptContext sc = nashorn.getContext();
                long st = System.currentTimeMillis();
                for(int i=0;i<100000;++i) {
                    sc.setAttribute("fieldName", "a", ScriptContext.ENGINE_SCOPE);
                    sc.setAttribute("entity", entity, ScriptContext.ENGINE_SCOPE);
                    Charset.forName((String)cs.eval(sc));
                }
                System.out.println(System.currentTimeMillis() - st);
            }
        }
    }
}
