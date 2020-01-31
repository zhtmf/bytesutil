package io.github.zhtmf.script;

enum TokenType {
    //enclosed by single quotes
    STR(java.lang.String.class),
    //true/false
    BOOL(java.lang.Boolean.class),
    //all numbers are treated as 64-bit float
    NUM(java.math.BigDecimal.class),
    //any legal text
    ID(Identifier.class),
    STMT(Statement.class),
    OP(Operator.class),
    IF(Temporary.If.class),
    Else(Temporary.Else.class),
    //not directly represented as null values in java 
    //as this compiler is also written in java
    NULL(Temporary.Null.class),
    LBracket(Temporary.LBracket.class),
    RBracket(Temporary.RBracket.class),
    LParentheses(Temporary.LParentheses.class),
    RParentheses(Temporary.RParentheses.class),
    LBraces(Temporary.LBraces.class),
    RBraces(Temporary.RBraces.class),
    Semicolon(Temporary.Semicolon.class),
    ;
    private Class<?> actualType;
    
    TokenType(Class<?> actualType){
        this.actualType = actualType;
    }
    
    boolean is(Object obj) {
        if(obj == null)
            return false;
        Class<?> clazz = obj.getClass();
        return this.actualType == clazz || this.actualType.isAssignableFrom(clazz);
    }
    
    static boolean isTypes(Object token,TokenType[] operandTypes) {
        for(TokenType type:operandTypes) {
            if(type.is(token)) {
                return true;
            }
        }
        return false;
    }
    
    static interface Temporary{
        static class Null{
            private Null() {}
            public static final Null instance = new Null();
            @Override
            public String toString() {
                return "<null>";
            }
        }
        static class IfBool {
            public static final IfBool TRUE = new IfBool(true);
            public static final IfBool FALSE = new IfBool(false);
            public final boolean value;
            public static IfBool of(boolean bool) {
                return bool ? TRUE : FALSE;
            }
            private IfBool(boolean bool) {
                this.value = bool;
            }
        }
        //Else
        static class Else{
            private Else() {}
            public static final Else instance = new Else();
            @Override
            public String toString() {return "else";}
        }
        //If
        static class If{
            private If() {}
            public static final If instance = new If();
            public String toString() {return "if";}
        }
        //[]
        static class LBracket{
            private LBracket() {}
            public static final LBracket instance = new LBracket();
            public String toString() {return "[";}
        }
        static class RBracket{
            private RBracket() {}
            public static final RBracket instance = new RBracket();
            public String toString() {return "]";}
        }
        //()
        static class LParentheses{
            private LParentheses() {}
            public static final LParentheses instance = new LParentheses();
            public String toString() {return "(";}
        }
        static class RParentheses{
            private RParentheses() {}
            public static final RParentheses instance = new RParentheses();
            public String toString() {return ")";}
        }
        //{}
        static class LBraces{
            private LBraces() {}
            public static final LBraces instance = new LBraces();
            public String toString() {return "{";}
        }
        static class RBraces{
            private RBraces() {}
            public static final RBraces instance = new RBraces();
            public String toString() {return "}";}
        }
        //semicolon ;
        static class Semicolon{
            private Semicolon() {}
            public static final Semicolon instance = new Semicolon();
            public String toString() {return ";";}
        }
    }
}