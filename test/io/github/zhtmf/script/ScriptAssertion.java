package io.github.zhtmf.script;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Map;

import org.junit.Assert;

/**
 * Stream api for traversing and testing an AST
 * @author dzh
 */
final class ScriptAssertion{
    private Script script;
    public ScriptAssertion(String program) {
        script = new Script(program);
    }
    public ScriptAssertion compile() {
        this.script
            .compile();
        return this;
    }
    public Object evaluate(Map<String, Object> global) {
        return this.script.evaluate(global);
    }
    public ScriptAssertion tokenize() {
        this.script.tokenize();
        return this;
    }
    public ScriptAssertion reorder() {
        this.script.reorder();
        return this;
    }
    public ScriptAssertion flatten() {
        this.script.flatten();
        return this;
    }
    public ScriptAssertion hasChild(int num) {
        Assert.assertEquals(print(),script.statements.size(), num);
        return this;
    }
    public ScriptAssertion.StatementAssertion child(int num) {
       return new StatementAssertion(
                   script.statements.get(num)
                   ,null
                   ,this);
    }
    public String print() {
        StringWriter sw = new StringWriter();
        for(Statement s:script.statements) {
            sw.write(s.toString());
            sw.write("\r\n");
        }
        System.out.println(sw.toString());
        return sw.toString();
    }
    public static final class StatementAssertion{
        private Statement statement;
        private ScriptAssertion.StatementAssertion parent;
        private ScriptAssertion script;
        private int tokenCounter;
        public StatementAssertion(
                Statement statement
               ,ScriptAssertion.StatementAssertion parent
               ,ScriptAssertion script) {
            this.statement = statement;
            this.parent = parent;
            this.script = script;
        }
        public ScriptAssertion.StatementAssertion isEmpty() {
            Assert.assertTrue(script.print(),statement instanceof EmptyStatement);
            return this;
        }
        public ScriptAssertion.StatementAssertion isBlock() {
            Assert.assertTrue(script.print(),statement instanceof BlockStatement);
            return this;
        }
        public ScriptAssertion.StatementAssertion hasNoChild() {
            Assert.assertEquals(script.print(),statement.tokenList.size(), 0);
            return this;
        }
        public ScriptAssertion.StatementAssertion hasChild(int num) {
            Assert.assertEquals(script.print(),statement.tokenList.size(), num);
            return this;
        }
        public ScriptAssertion.StatementAssertion child(int num) {
            tokenCounter = num + 1;
            return new StatementAssertion(
                    (Statement) statement.tokenList.get(num)
                    ,this
                    ,this.script);
        }
        public ScriptAssertion.StatementAssertion child() {
            int num = tokenCounter;
            tokenCounter++;
            return new StatementAssertion(
                    (Statement) statement.tokenList.get(num)
                    ,this
                    ,this.script);
        }
        public ScriptAssertion.StatementAssertion token(int num, TokenType type, String name) {
            token(num,type);
            token(num,name);
            tokenCounter = num+1;
            return this;
        }
        public ScriptAssertion.StatementAssertion token(TokenType type, String name) {
            int num = tokenCounter;
            token(num,type);
            token(num,name);
            tokenCounter = num+1;
            return this;
        }
        public ScriptAssertion.StatementAssertion resetTokenCounter() {
            tokenCounter = 0;
            return this;
        }
        public ScriptAssertion.StatementAssertion token(int num, TokenType type) {
            tokenCounter = num+1;
            Assert.assertTrue(statement.tokenList.get(num)+"", type.is(statement.tokenList.get(num)));
            return this;
        }
        public ScriptAssertion.StatementAssertion token(int num, String name) {
            tokenCounter = num+1;
            Object obj = statement.tokenList.get(num);
            String objName = null;
            TokenType type = typeof(obj);
            switch(type) {
            case STR:
                objName = obj.toString();
                break;
            case BOOL:
                objName = obj.toString();
                break;
            case ID:
                objName = ((Identifier)obj).getName();
                break;
            case NULL:
                objName = "null";
                break;
            case NUM:
                objName = ((BigDecimal)obj).toPlainString();
                break;
            case OP:
                objName = ((Operator)obj).op;
                break;
            case STMT:
                objName = "<STMT>";
                break;
            default:
                break;
            }
            Assert.assertEquals(name, objName);
            return this;
        }
        public ScriptAssertion and() {
            return this.script;
        }
        public ScriptAssertion.StatementAssertion parent() {
            if(this.parent==null)
                throw new IllegalStateException();
            return this.parent;
        }
        
        static TokenType typeof(Object obj) {
            if(obj==null)
                return null;
            for(TokenType type:TokenType.values()) {
                if(type.is(obj)) {
                    return type;
                }
            }
            return null;
        }
    }
}