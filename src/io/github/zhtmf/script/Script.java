package io.github.zhtmf.script;

import static io.github.zhtmf.script.TokenType.BOOL;
import static io.github.zhtmf.script.TokenType.Else;
import static io.github.zhtmf.script.TokenType.ID;
import static io.github.zhtmf.script.TokenType.IF;
import static io.github.zhtmf.script.TokenType.LBraces;
import static io.github.zhtmf.script.TokenType.LBracket;
import static io.github.zhtmf.script.TokenType.LParentheses;
import static io.github.zhtmf.script.TokenType.NULL;
import static io.github.zhtmf.script.TokenType.NUM;
import static io.github.zhtmf.script.TokenType.OP;
import static io.github.zhtmf.script.TokenType.RBraces;
import static io.github.zhtmf.script.TokenType.RBracket;
import static io.github.zhtmf.script.TokenType.RParentheses;
import static io.github.zhtmf.script.TokenType.STMT;
import static io.github.zhtmf.script.TokenType.STR;
import static io.github.zhtmf.script.TokenType.Semicolon;
import static io.github.zhtmf.script.TokenType.isTypes;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.zhtmf.script.Operators.BracketOperator;
import io.github.zhtmf.script.Operators.OperatorIterator;
import io.github.zhtmf.script.ParsingException.ParsingTerminationException;
import io.github.zhtmf.script.TokenType.Temporary;
import io.github.zhtmf.script.TokenType.Temporary.If;
import io.github.zhtmf.script.TokenType.Temporary.Null;


/**
 * Main entry for this script engine.
 * <p>
 * Safe for concurrent evaluation after {@link #compile() compile}.
 * 
 * @author dzh
 */
public class Script {
    
    private static TokenType[] TOKENS = {ID, STMT, NUM, STR, BOOL, NULL, OP};
    private static final char[] DECIMAL_CHARS = "0123456789".toCharArray();
    private static final char[] HEX_CHARS = "0123456789ABCDEFabcdef".toCharArray();
    private static final char[] BINARY_CHARS = "01_".toCharArray();
    private static final Set<String> keywords = Collections
            .unmodifiableSet(new HashSet<>(Arrays.asList("false", "true", "null", "if", "else")));
    
    private static class State{
        String program;
        int index;
        public State(String program, int index) {
            this.program = program;
            this.index = index;
        }
        public boolean end() {
            return index >= program.length();
        }
    }
    
    private static class Index{
        public int index;
        public Index(int index) {
            this.index = index;
        }
    }
    
    /**
     * Interface for operations reading tokens
     * @author dzh
     */
    private interface ReadTokenOp {
        /**
         * Try reading a token of specific type.
         * 
         * @param program the raw program string, may be only a statement
         * @param index   the index from which the parsing starts. Set to one before the
         *                index of last character of this token -1 on success.
         * @return token, or <tt>null</tt> if no token of this type can be found.
         * @throws ParsingException if the program is syntactically invalid (such as
         *                          unclosed string literal).
         */
        Object run(String program, Index index) throws ParsingException;
    }
    
    /**
     * the raw program string
     */
    private final String program;
    
    private volatile boolean compiled;
    /**
     * Statements in this program
     */
    List<Statement> statements = Collections.emptyList();
    
    /**
     * Constructs a script instance but without instantiating it.
     * 
     * @param program
     */
    public Script(String program) {
        if(program == null) program = "";
        //eliminate unnecessary empty statement at the end
        //due to automatic appending of semicolon
        program = program.trim();
        this.program = program;
    }

    Script tokenize() {
        this.statements = parse(this.program);
        return this;
    }
    
    /**
     * Reorder token list in statements to make reverse-polish expressions.
     * 
     * @return  this object
     */
    Script reorder() {
        for(Statement obj:this.statements) {
            reorder(obj);
        }
        return this;
    }
    
    private void reorder(Statement statement) {
        List<Object> tokenList = statement.tokenList;
        //first reorder statements
        for(Object obj:tokenList) {
            if(obj instanceof Statement) {
                reorder((Statement)obj);
            }
        }
        if(statement.ordered) {
            return;
        }
        OperatorIterator iterator = new OperatorIterator();
        while(iterator.hasNext()) {
            List<Operator> operatorLevel = iterator.next();
            if(iterator.isPrefixOperators()) {
                //prefix operators, right to left associativity
                int start = -1;
                for(int index=0;index<tokenList.size();++index) {
                    Object token = tokenList.get(index);
                    if(token instanceof Operator) {
                        if(operatorLevel.contains(token)) {
                            if (start < 0) {
                                start = index;
                            }
                        }
                    }else if(start >= 0) {
                        int end = index-1;
                        while(end >= start) {
                            index = ((Operator) tokenList.get(end)).reorder(tokenList, end);
                            --end;
                        }
                        start = -1;
                    }
                }
                
                if(start >= 0) {
                    throw new ParsingException(
                            "invalid operator notation in "+statement)
                        .withSiteAndOrdinal(Script.class, 9);
                }
                
            }else for(int index=0;index<tokenList.size();++index) {
                Object token = tokenList.get(index);
                if(token instanceof Operator) {
                    Operator operator = (Operator)token;
                    if(operatorLevel.contains(operator)) {
                        index = operator.reorder(tokenList, index);
                    }
                }else if(token instanceof Statement) {
                    //operators may create new unordered statements, 
                    //currently only IF does this
                    reorder((Statement)token);
                }
            }
        }
        statement.ordered = true;
    }

    /**
     * Unwrap unnecessary nesting structures to eliminate statement type operands.
     * This can make the final AST have less levels and evaluation easier.
     * <p>
     * Conditional statements associated with logical operators / if or else /
     * ternary operators are not unwrapped.
     * <p>
     * Stack based checking for integrity of statements based on arity of operators
     * and how many result it returns is also done in this phase.
     * <p>
     * <ol>
     * <li>Empty or nested empty statements are deleted entirely, like
     * <tt>{{{{}}}{}}</tt> or the statement in the middle of <tt>a=b;;b=c</tt></li>
     * <li>Tokens in the single inline statement between a brace statement are
     * lifted to be direct children of it rather than forming another child
     * statement. like <tt>{a+b;}</tt> is parsed as
     * 
     * <pre>
     * BLOCK
     *    ID[a]
     *    ID[b]
     *    OP[+]
     * </pre>
     * 
     * rather than
     * 
     * <pre>
     * BLOCK
     *    STMT
     *      ID[a]
     *      ID[b]
     *      OP[+]
     * </pre>
     * 
     * </li>
     * <li>Parentheses statements are unwrapped except for those associated with
     * conditional processing (like the If-True branch of <tt>if</tt>).
     * Correct order of evaluation is guaranteed by correct order of tokens in the
     * final reverse-polish expression.</li>
     * </ol>
     * 
     * @return this object
     */
    Script flatten() {
        List<Statement> list = this.statements;
        for(int s = 0; s < list.size(); ++s) {
            Statement obj = list.get(s);
            if(isEmptyStatement(obj)) {
                list.remove(s);
                --s;
                continue;
            }
            flatten(obj);
            inspect(obj);
        }
        return this;
    }
    
    private void flatten(Statement statement) {
        List<Object> flattenedTokenList = new ArrayList<Object>();
        List<Object> tokenList = statement.tokenList;
        for(Object token:tokenList) {
            if(token instanceof Statement) {
                Statement sub = (Statement)token;
                if(sub instanceof EmptyStatement)
                    flattenedTokenList.add(sub);
                else if(isEmptyStatement(sub))
                    flattenedTokenList.add(new EmptyStatement());
                else {
                    flatten(sub);
                    if(!sub.deferred
                    && (!(statement instanceof BlockStatement) || tokenList.size() == 1)) {
                        flattenedTokenList.addAll(sub.tokenList);
                    }else {
                        flattenedTokenList.add(sub);
                    }
                }
            }else {
                flattenedTokenList.add(token);
            }
        }
        tokenList.clear();
        tokenList.addAll(flattenedTokenList);
        statement.tokens = statement.tokenList.toArray();
    }
    
    private void inspect(Statement statement) {
        if(statement instanceof EmptyStatement)
            return;
        List<Object> tokenList = statement.tokenList; 
        for(Object token:tokenList) {
            if(token instanceof Statement) {
                inspect((Statement)token);
            }
        }
        if(! (statement instanceof BlockStatement) && !(statement instanceof PListStatement)) {
            int counter = 0;
            for(int p = 0;p < tokenList.size(); ++p) {
                Object token = tokenList.get(p);
                if(token instanceof Operator) {
                    Operator operator = (Operator)token;
                    counter -= operator.arity();
                    counter += operator.returns();
                }else {
                    ++counter;
                }
            }
            
            if(counter != 1) {
                throw new ParsingException("malformed statement:"+statement)
                .withSiteAndOrdinal(Script.class, 6);
            }
        }
    }
    
    private static boolean isEmptyStatement(Statement stmt) {
        if(stmt instanceof EmptyStatement)
            return true;
        if(stmt instanceof PListStatement)
            return false;
        for(Object token:stmt.tokenList) {
            if(!(token instanceof Statement) 
            || !isEmptyStatement((Statement) token)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Compile the script and make this Script ready for repeated evaluation.
     * <p>
     * After this method is successfully called, calling this method again has no
     * effect.
     * 
     * @return this object.
     */
    public synchronized Script compile() {
        if(compiled) {
            return this;
        }
        this.tokenize()
            .reorder()
            .flatten();
        compiled = true;
        return this;
    }

    /**
     * Evaluate this script using specified initial global mappings and optional
     * protected mode.
     * <p>
     * This method can be called concurrently. As the <tt>global</tt> parameter is
     * copied internally it can be reused across different calls.
     * 
     * @param global initial global mappings
     * @throws IllegalStateException if this script has not been compiled in
     *         advance.
     * @throws ParsingException error thrown by script itself
     */
    public Object evaluate(Map<String, Object> global) throws IllegalStateException, ParsingException {
        if(!compiled) {
            throw new IllegalStateException("not compiled");
        }
        Context ctx = new Context(global);
        try {
            List<Statement> statements = this.statements;
            for(int i=0, len = statements.size(); i < len; ++i) {
                statements.get(i).evaluate(ctx);
            }
        } catch (ParsingTerminationException e) {
        }
        return ctx.peek();
    }
    
    @Override
    public String toString() {
        return "Script["+program+"]";
    }
    
    //------- state based parsing -------
    
    /**
     * Main parsing routine
     * @param program   script
     * @return  tokenized statements
     */
    private static List<Statement> parse(String program){
        State script = new State(program, 0);
        List<Statement> ret = new ArrayList<Statement>();
        while(!script.end())
            ret.addAll(initial(script));
        return ret;
    }
    
    private static List<Statement> initial(State program){
        return initial(null, program);
    }

    private static List<Statement> initial(Object leading, State program){
        Object token = leading == null ? readToken(program) : leading;
        
        if(Semicolon.is(token)) {
            return new ArrayList<Statement>(Collections.singletonList(normalStatement(token, program)));
        }else if(isTypes(token,TOKENS)) {
            return new ArrayList<Statement>(Collections.singletonList(normalStatement(token, program)));
        }else if(LParentheses.is(token)) {
            return new ArrayList<Statement>(Collections.singletonList(normalStatement(token, program)));
        }else if(LBraces.is(token)) {
            return braceStatement(token, program);
        } else if(IF.is(token)) {
            return ifCondition(token, program);
        }
        
        throw unexpectedTokenException(token, program).withSiteAndOrdinal(Script.class, 22);
    }
    
    private static Statement normalStatement(Object leading, State program){
        List<Object> tokenList = new ArrayList<Object>();
        
        for(;;) {
            Object token;
            if(leading != null) {
                token = leading;
                leading = null;
            }else {
                if(program.end()) {
                    break;
                }
                token = readToken(program);
            }
            if(Semicolon.is(token)) {
                break;
            } else if(isTypes(token,TOKENS)) {
                tokenList.add(token);
            } else if(LParentheses.is(token)) {
                boolean plist = precedesMethodCall(tokenList);
                tokenList.add(roundStatement(token, program ,plist));
                if(plist)
                    tokenList.add(Operators.getOperator("()"));
            } else if(LBracket.is(token)) {
                tokenList.addAll(bracketStatement(token, program));
            } else {
                throw unexpectedTokenException(token, program).withSiteAndOrdinal(Script.class, 23);
            }
        }
        if(tokenList.isEmpty()) {
            return new EmptyStatement();
        }
        
        Statement stmt = new Statement();
        stmt.tokenList.addAll(tokenList);
        return stmt;
    }
    
    private static Statement roundStatement(Object leading, State program, boolean plist){
        List<Object> tokenList = new ArrayList<Object>();
        for(;;) {
            Object token = readToken(program);
            if(RParentheses.is(token)) {
                break;
            } else if(isTypes(token,TOKENS)) {
                tokenList.add(token);
            } else if(LParentheses.is(token)) {
                boolean plist2 = precedesMethodCall(tokenList);
                tokenList.add(roundStatement(token, program ,plist2));
                if(plist2)
                    tokenList.add(Operators.getOperator("()"));
            } else if(LBracket.is(token)) {
                tokenList.addAll(bracketStatement(token, program));
            } else {
                throw unexpectedTokenException(token, program).withSiteAndOrdinal(Script.class, 24);
            }
        }
        if(!plist && tokenList.isEmpty())
            throw new ParsingException("expression expected after (")
                .withSiteAndOrdinal(Script.class, 28);
        if(plist && tokenList.size() > 1) {
            //the last comma operator before the ) is omitted in the syntax
            //but necessary for further processing
            tokenList.add(Operators.getOperator(","));
        }
        Statement stmt = plist ? new PListStatement() : new Statement();
        stmt.tokenList.addAll(tokenList);
        return stmt;
    }
    
    private static List<Statement> braceStatement(Object leading, State program){
        List<Object> tokenList = new ArrayList<Object>();
        for(;;) {
            Object token = readToken(program);
            if(RBraces.is(token)) {
                break;
            } else if(LBraces.is(token)){
                tokenList.addAll(braceStatement(token, program));
            } else {
                tokenList.addAll(initial(token, program));
            }
        }
        if(tokenList.isEmpty()) {
            return new ArrayList<Statement>(Collections.singletonList((Statement)new EmptyStatement()));
        }else {
            Statement stmt = new BlockStatement();
            stmt.tokenList.addAll(tokenList);
            return new ArrayList<Statement>(Collections.singletonList(stmt));
        }
    }
    
    private static List<Object> bracketStatement(Object leading, State program){
        List<Object> between = new ArrayList<Object>();
        for(;;) {
            Object token = readToken(program);
            if(RBracket.is(token)) {
                break;
            } else if(LBracket.is(token)){
                between.addAll(bracketStatement(token, program));
            } else if(LParentheses.is(token)){
                boolean plist = precedesMethodCall(between);
                between.add(roundStatement(token, program ,plist));
                if(plist)
                    between.add(Operators.getOperator("()"));
            } else if(isTypes(token,TOKENS)){
                between.add(token);
            } else {
                throw unexpectedTokenException(token, program).withSiteAndOrdinal(Script.class, 25);
            }
        }
        if(between.isEmpty()) {
            throw parsingException("empty property reference", program)
                .withSiteAndOrdinal(Script.class, 26);
        }else {
            List<Object> ret = new ArrayList<Object>();
            Statement property = new Statement();
            property.tokenList.addAll(between);
            ret.add(property);
            ret.add(Operators.getOperator("[]"));
            return ret;
        }
    }
    
    private static List<Statement> ifCondition(Object leading, State program){
        List<Object> tokenList = new ArrayList<Object>();
        tokenList.add(Operators.getOperator("if"));
        Object token = readToken(program);
        if(LParentheses.is(token)) {
            //a method call cannot succeeds an if
            tokenList.add(roundStatement(token, program, false));
        } else {
            throw unexpectedTokenException(token, program).withSiteAndOrdinal(Script.class, 27);
        }
        tokenList.add(ifTrue(null, program));
        Statement stmt = new Statement();
        stmt.tokenList.addAll(tokenList);
        token = peekToken(program);
        if(Else.is(token)) {
            stmt.tokenList.addAll(afterIf(readToken(program), program));
        }
        return Collections.singletonList(stmt);
    }
    
    private static Statement ifTrue(Object leading, State program) {
        List<Statement> ret = initial(program);
        return ret.get(0);
    }
    
    private static List<Object> afterIf(Object leading, State program) {
        List<Object> tokenList = new ArrayList<Object>();
        tokenList.add(Operators.getOperator("else"));
        tokenList.addAll(initial(program));
        return tokenList;
    }
    
    private static boolean precedesMethodCall(List<Object> tokenList) {
        Object token;
        return !tokenList.isEmpty() && 
                (ID.is((token = tokenList.get(tokenList.size()-1))) || (token instanceof BracketOperator));
    }
    
    private static Object peekToken(State program) {
        Index idx = new Index(program.index);
        while(!program.end() &&
            Character.isWhitespace(
                    program.program.charAt(idx.index)))
                ++idx.index;
        if(program.end())
            return null;
        for(ReadTokenOp op: READ_TOKEN_OPS) {
            Object token = op.run(program.program, idx);
            if(token == null)
                continue;
            return token;
        }
        return null;
    }
    
    private static Object readToken(State program) {
        Index idx = new Index(program.index);
        while(!program.end() &&
                Character.isWhitespace(program.program.charAt(idx.index)))
                    ++idx.index;
        if(program.end())
            throw new ParsingException("unexpected end of program")
            .withSiteAndOrdinal(Script.class, 20);
        for(ReadTokenOp op: READ_TOKEN_OPS) { 
            Object token = op.run(program.program, idx);
            if(token == null)
                continue;
            program.index = idx.index + 1;
            return token;
        }
        
        throw parsingException("unknown token at "+program.index, program).withSiteAndOrdinal(Script.class, 21);
    }
    
    private static ParsingException unexpectedTokenException(Object token, State program) {
        return parsingException("unexpected token"+token+" at "+program.index, program);
    }
    
    private static ParsingException parsingException(String message, State program) {
        StringBuilder visualized = new StringBuilder();
        visualized.append(message);
        visualized.append(System.lineSeparator());
        visualized.append(program.program);
        visualized.append(System.lineSeparator());
        for(int i=0;i<program.index - 1;++i) {
            visualized.append(' ');
        }
        visualized.append('^');
        visualized.append(System.lineSeparator());
        return new ParsingException(visualized.toString());
    }
    
    private static ReadTokenOp readString = new ReadTokenOp() {
        public Object run(String program, Index idx) throws ParsingException {
            int index = idx.index;
            if (program.charAt(index) != '\'')
                return null;
            boolean escape = false;
            StringBuilder ret = new StringBuilder();
            for (int i = index + 1, len = program.length(); i < len; ++i) {
                char ch = program.charAt(i);
                if (escape) {
                    // valid escapes are \b \t \n \f \r \" \' \\
                    switch (ch) {
                    case 'b':
                        ret.append('\b');
                        break;
                    case 't':
                        ret.append('\t');
                        break;
                    case '\\':
                        ret.append('\\');
                        break;
                    case '\'':
                        ret.append('\'');
                        break;
                    case 'r':
                        ret.append('\r');
                        break;
                    case 'f':
                        ret.append('\f');
                        break;
                    case 'n':
                        ret.append('\n');
                        break;
                    default:
                        throw new ParsingException("invalid escape sequence \\" + ch
                                + " valid ones are \\b  \\t  \\n  \\f  \\r  \\'  \\\\").withSiteAndOrdinal(Script.class,
                                        11);
                    }
                    escape = false;
                } else if (ch == '\\') {
                    escape = true;
                } else if (ch == '\'') {
                    idx.index = i;
                    return ret.toString();
                } else {
                    ret.append(ch);
                }
            }
            throw new ParsingException("unclosed string literal in statement /" + program + "/ at index " + index)
                .withSiteAndOrdinal(Script.class, 2);
        }
    };
    private static ReadTokenOp readIdentifier = new ReadTokenOp() {
        @Override
        public Object run(String program, Index idx) throws ParsingException {
            int index = idx.index;
            char ch = program.charAt(index);
            if (ch >= '0' && ch <= '9') {
                return null;
            }
            int i = index;
            for (; i < program.length(); ++i) {
                if (!Character.isJavaIdentifierPart(program.charAt(i))) {
                    break;
                }
            }
            if(i == index)
                return null;
            String identifier = program.substring(index, i);
            if (Operators.getOperator(identifier) != null || keywords.contains(identifier))
                return null;
            idx.index = i - 1;
            return Identifier.of(identifier);
        }
    };
    
    // boolean or null literal
    private static ReadTokenOp readBooleanNull = new ReadTokenOp() {
        @Override
        public Object run(String program, Index idx) throws ParsingException {
            int index = idx.index;
            if (program.startsWith("false", index)) {
                idx.index = index + "false".length() - 1;
                return Boolean.FALSE;
            } else if (program.startsWith("true", index)) {
                idx.index = index + "true".length() - 1;
                return Boolean.TRUE;
            } else if (program.startsWith("null", index)) {
                idx.index = index + "null".length() - 1;
                return Null.instance;
            } else if (program.startsWith("if", index)) {
                idx.index = index + "if".length() - 1;
                return If.instance;
            }else if (program.startsWith("else", index)) {
                idx.index = index + "else".length() - 1;
                return Temporary.Else.instance;
            }
            return null;
        }
    };

    // number literal, supports hex and binary literal syntax
    private static ReadTokenOp readNumLiteral = new ReadTokenOp() {
        @Override
        public Object run(String program, Index idx) throws ParsingException {
            int index = idx.index;
            char ch = program.charAt(index);
            if (!(ch >= '0' && ch <= '9')) {
                return null;
            }

            boolean permitsFraction = false;
            int start = index;
            char[] collection;

            char ch2 = index < program.length() - 1 ? program.charAt(index + 1) : 0;
            if (ch2 == 'x' || ch2 == 'X') {
                start = index + 2;
                collection = HEX_CHARS;
            } else if (ch2 == 'b' || ch2 == 'B') {
                start = index + 2;
                collection = BINARY_CHARS;
            } else {
                start = index;
                permitsFraction = true;
                collection = DECIMAL_CHARS;
            }

            boolean dotSeen = false;
            for (int i = start; i < program.length(); ++i) {
                
                ch = program.charAt(i);
                
                boolean found = false;
                for (char numChar : collection) {
                    if (numChar == ch) {
                        found = true;
                        break;
                    }
                }
                
                if (!found) {
                    if (ch == '.' && permitsFraction) {
                        if (!dotSeen) {
                            dotSeen = true;
                        } else {
                            idx.index = i-1;
                            return tryParseNumber(program.substring(index, i));
                        }
                    } else {
                        idx.index = i-1;
                        return tryParseNumber(program.substring(index, i));
                    }
                }
            }
            idx.index = program.length() - 1;
            return tryParseNumber(program.substring(start));
        }
    };

    private static ReadTokenOp readStructures = new ReadTokenOp() {
        @Override
        public Object run(String program, Index idx) throws ParsingException {
            int index = idx.index;
            switch (program.charAt(index)) {
            case '{':
                idx.index = index;
                return TokenType.Temporary.LBraces.instance;
            case '}':
                idx.index = index;
                return TokenType.Temporary.RBraces.instance;
            case '(':
                idx.index = index;
                return TokenType.Temporary.LParentheses.instance;
            case ')':
                idx.index = index;
                return TokenType.Temporary.RParentheses.instance;
            case '[':
                idx.index = index;
                return TokenType.Temporary.LBracket.instance;
            case ']':
                idx.index = index;
                return TokenType.Temporary.RBracket.instance;
            case ';':
                idx.index = index;
                return TokenType.Temporary.Semicolon.instance;
            }
            return null;
        }
    };

    private static ReadTokenOp readOperator = new ReadTokenOp() {
        @Override
        public Object run(String program, Index idx) throws ParsingException {
            int index = idx.index;
            for (int p = Operators.OPERATOR_MAX_LENGTH; p >= 1; p--) {
                if (index + p > program.length()) {
                    continue;
                }
                String sub = program.substring(index, index + p);
                Operator op = Operators.getOperator(sub);
                if (op != null) {
                    idx.index = index + p - 1;
                    return op;
                }
            }
            return null;
        }
    };
    
    // they must be in this order
    private static final ReadTokenOp[] READ_TOKEN_OPS = { 
            readString, readIdentifier, readBooleanNull, readNumLiteral
            , readStructures ,readOperator};

    private static BigDecimal tryParseNumber(String raw) {
        if (raw.startsWith("0x") || raw.startsWith("0X")) {
            return new BigDecimal(new BigInteger(raw.substring(2), 16));
        } else if (raw.startsWith("0b") || raw.startsWith("0B")) {
            raw = raw.substring(2);
            if (raw.startsWith("_") || raw.endsWith("_")) {
                return null;
            }
            return new BigDecimal(new BigInteger(raw.replace("_", ""), 2));
        }
        try {
            return new BigDecimal(raw);
        } catch (Exception e) {
            return null;
        }
    }
}
