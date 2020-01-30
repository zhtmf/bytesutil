package io.github.zhtmf.script;

import java.util.LinkedList;
import java.util.List;

class Statement {
    public final List<Object> tokenList;
    /**
     * Whether this statement is ordered and can be excluded by reorder phase.
     */
    public boolean ordered = false;
    /**
     * Whether this statement is associated with conditional processing and should
     * not be unwrapped.
     */
    public boolean deferred = false;
    public Statement() {
        this.tokenList = new LinkedList<Object>();
    }
    
    public void evaluate(Context ctx) {
        for(Object token : this.tokenList){
            if(token instanceof Operator) {
                ((Operator)token).eval(ctx);
            }else if(token instanceof Statement){
                Statement stmt = (Statement)token;
                if(stmt.deferred) {
                    ctx.push(stmt);
                }else {
                    /*
                     * no block scope as we lacks 
                     * variable declaration syntax
                     */
                    ((Statement)token).evaluate(ctx);
                }
            }else {
                if(token instanceof Identifier) {
                    ctx.cacheValue((Identifier) token);
                }
                ctx.push(token);
            }
        }
        ctx.push(Operator.convertValue(ctx, ctx.pop()));
    }
    
    @Override
    public String toString() {
        return toString(0);
    }
    protected String getType() {
        return "STMT";
    }
    private String toString(int indent) {
        StringBuilder ret = new StringBuilder();
        String indentStr = getIndent(indent);
        String nextIndent = getIndent(indent + 4);
        ret.setLength(0);
        ret.append(indentStr+getType()+"\n");
        for(Object obj:tokenList) {
            if(obj instanceof Statement) {
                ret.append(((Statement)obj).toString(indent+4));
            }else {
                ret.append(nextIndent+obj.toString());
                ret.append("\n");
            }
        }
        return ret.toString();
    }
    
    private static String getIndent(int indent) {
        StringBuilder ret = new StringBuilder();
        for(int i=0;i<indent;++i) {
            ret.append(' ');
        }
        return ret.toString();
    }
}

class BlockStatement extends Statement{
    @Override
    protected String getType() {
        return "BLOCK";
    }
}

/**
 * Placeholder for empty statements. Most of them are removed during flattening
 * phase but still useful and retained if associated with an if.
 * 
 * @author dzh
 */
class EmptyStatement extends Statement{
    public EmptyStatement() {
    }
    @Override
    protected String getType() {
        return "EMPTY";
    }
    @Override
    public void evaluate(Context ctx) {
    }
}