package io.github.zhtmf.script;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

public class ZhtmfScriptEngineFactory implements ScriptEngineFactory{
    
    private static final String VERSION = "1.0";
    private static final String NAME = "__zhtmf-script";

    @Override
    public String getEngineName() {
        return "zhtmf private script engine";
    }

    @Override
    public String getEngineVersion() {
        return VERSION;
    }

    @Override
    public List<String> getExtensions() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getMimeTypes() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getNames() {
        return Arrays.asList(NAME);
    }

    @Override
    public String getLanguageName() {
        return NAME;
    }

    @Override
    public String getLanguageVersion() {
        return VERSION;
    }

    @Override
    public Object getParameter(String key) {
        switch (key) {
        case ScriptEngine.ENGINE:return getEngineName();
        case ScriptEngine.ENGINE_VERSION:return getEngineVersion();
        case ScriptEngine.LANGUAGE:return getLanguageName();
        case ScriptEngine.LANGUAGE_VERSION:return getLanguageVersion();
        case ScriptEngine.NAME:return getNames();
        case "THREADING": return "STATELESS";
        }
        return null;
    }

    @Override
    public String getMethodCallSyntax(String obj, String m, String... args) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getOutputStatement(String toDisplay) {
        throw new UnsupportedOperationException();

    }

    @Override
    public String getProgram(String... statements) {
        StringBuilder ret = new StringBuilder();
        for(String str:statements) {
            ret.append(str).append(';');
        }
        return ret.toString();
    }

    @Override
    public ScriptEngine getScriptEngine() {
        return new ZhtmfScriptEngine(this);
    }

    public ScriptEngine getProtectedScriptEngine() {
        return new ZhtmfScriptEngine(this, true);
    }
}
