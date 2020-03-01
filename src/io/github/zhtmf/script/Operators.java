package io.github.zhtmf.script;

import static io.github.zhtmf.script.TokenType.BOOL;
import static io.github.zhtmf.script.TokenType.ID;
import static io.github.zhtmf.script.TokenType.NUM;
import static io.github.zhtmf.script.TokenType.STMT;
import static io.github.zhtmf.script.TokenType.STR;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.github.zhtmf.script.TokenType.Temporary.IfBool;

/**
 * Encapsulates basic operations and implementation classes related to Operator
 * 
 * @author dzh
 */
class Operators {
    
    private Operators() {throw new UnsupportedOperationException();}
    
    public static final int OPERATOR_MAX_LENGTH = 6;
    private static final List<List<Operator>> OPERATORLIST_LIST = new ArrayList<>();
    private static final Map<String, Operator> OPERATOR_MAP = new HashMap<>();
    /**
     * Common math context object for all arithmetic operations.
     * <p>
     * This is a must even for seemingly innocent operations like add/subtract as
     * for some rare cases they do throw error or loop forever because of rounding
     * problems.
     * <p>
     * For example, this one will loop forever in the <tt>add</tt> method:
     * 
     * <pre>
     * System.out.println(new BigDecimal("4").add(
     *         new BigDecimal("4").pow(new BigDecimal("999999999999")
     *         .intValue(), MathContext.DECIMAL64),
     *         MathContext.DECIMAL64));
     * </pre>
     */
    private static final MathContext commonMathContext = MathContext.DECIMAL64;
    
    static {
        
        SuffixIncrementOperator sinc = new SuffixIncrementOperator();
        PrefixIncrementOperator pinc = new PrefixIncrementOperator();
        sinc.setNext(pinc);
        
        SuffixDecrementOperator sdec = new SuffixDecrementOperator();
        PrefixDecrementOperator pdec = new PrefixDecrementOperator();
        sdec.setNext(pdec);
        
        PositiveOperator positive = new PositiveOperator();
        PlusOperator plus = new PlusOperator();
        positive.setNext(plus);
        
        NegativeOperator neg = new NegativeOperator();
        SubtractOperator sub = new SubtractOperator();
        neg.setNext(sub);
        
        //level 19
        addOperator(new IfOperator(), 19);
        
        //level 18
        addOperator(new ElseOperator(), 18);
        
        //level 17
        addOperator(new BracketOperator(), 17);
        addOperator(new DotOperator(), 17);
        /*
         * A Call operator created on the fly follows every parameter list parenthesis
         * expression. If the method call expression itself is an operand, put the call
         * operator further down in the precedence sequence will make following +/-
         * operator be mistakenly regarded as Prefix Positive/Negative operator instead
         */
        addOperator(new CallOperator(), 17);
        
        //level 16
        addOperator(new PowerOperator(), 16);
        
        //level 15
        addOperator(sinc, 15);
        addOperator(sdec, 15);
        
        //level 14
        addOperator(pinc, 14);
        addOperator(pdec, 14);
        addOperator(positive, 14);
        addOperator(neg, 14);
        addOperator(new LogicalNotOperator(), 14);
        addOperator(new BitwiseNotOperator(), 14);
        
        //level 12
        addOperator(new MultiplyOperator(), 12);
        addOperator(new DivideOperator(), 12);
        addOperator(new ModOperator(), 12);
        addOperator(new FloorDivideOperator(), 12);
        
        //level 11
        //additive
        addOperator(plus, 11);
        addOperator(sub, 11);
        
        //level 10
        addOperator(new ShiftLeftOperator(), 10);
        addOperator(new ShiftRightOperator(), 10);
        
        //level 9
        //relational
        addOperator(new GtOperator(), 9);
        addOperator(new GteOperator(), 9);
        addOperator(new LtOperator(), 9);
        addOperator(new LteOperator(), 9);
        
        //level 8
        //equality
        addOperator(new EQOperator(), 8);
        addOperator(new NEQOperator(), 8);
        
        //level 7
        //bitwise AND
        addOperator(new BitwiseAndOperator(), 7);
        
        //level 6
        addOperator(new BitwiseXOROperator(), 6);
        
        //level 5
        addOperator(new BitwiseOrOperator(), 5);
        
        //level 4
        addOperator(new LogicalAndOperator(), 4);
        
        //level 3
        addOperator(new LogicalOrOperator(), 3);
        
        //level 2
        //ternary 
        addOperator(new QuestionOperator(), 2);
        addOperator(new ColonOperator(), 2);
        
        //level 1
        addOperator(new AssignOperator(), 1);
        
        //level 0
        addOperator(new CommaOperator(), 0);
        addOperator(new ReturnOperator(), 0);
        
        List<List<Operator>> list = Operators.OPERATORLIST_LIST;
        for(int i=0;i<list.size();++i) {
            list.set(i, Collections.unmodifiableList(list.get(i)));
        }
    }
    
    private static void addOperator(Operator op,int level) {
        List<List<Operator>> stack = Operators.OPERATORLIST_LIST;
        Map<String, Operator> operatorMap = Operators.OPERATOR_MAP;
        List<Operator> list;
        while(stack.size() < level+1) {
            stack.add(new ArrayList<Operator>());
        }
        list = stack.get(level);
        list.add(op);
        if( ! operatorMap.containsKey(op.op)) {
            operatorMap.put(op.op, op);
        }
    }
    
    public static class OperatorIterator implements Iterator<List<Operator>>{
        private List<List<Operator>> stack = Operators.OPERATORLIST_LIST;
        private int level = stack.size()-1;
        public boolean isPrefixOperators() {
            //previous list returned by next() is prefix operators
            return level == 14 - 1;
        }
        @Override
        public boolean hasNext() {
            return level >= 0;
        }
        @Override
        public List<Operator> next() {
            return stack.get(level--);
        }
    }
    
    public static Operator getOperator(String op) {
        return OPERATOR_MAP.get(op);
    }
    
    //ternary level 2
    public static class QuestionOperator extends Operator{

        public QuestionOperator() {
            super("?", "?");
        }
        
        @Override
        public int arity() {
            return 3;
        }
        
        @Override
        protected void checkOperands(List<Object> tokenList, int index) {
            checkEnoughOperands(tokenList, index, 1, 1);
            Object left = tokenList.get(index-1);
            if(!isComposite(left) && !BOOL.is(left)) {
                throw new ParsingException("wrong condition operand for ternary operator")
                .withSiteAndOrdinal(QuestionOperator.class, 1);
            }
        }

        @Override
        protected int reorder0(List<Object> tokenList, int index) {
            Object left = tokenList.get(index-1);
            Statement condition;
            if(left instanceof Statement) {
                condition = (Statement)left;
            }else {
                condition = new Statement();
                condition.tokenList.add(left);
            }
            condition.deferred = true;
            
            int counter = 1;
            for(int k=index+1;k<tokenList.size();++k) {
                Object obj = tokenList.get(k);
                if(obj instanceof QuestionOperator) {
                    ++counter;
                }else if(obj instanceof ColonOperator) {
                    if(--counter == 0) {
                        
                        Statement ifTrue = new Statement();
                        List<Object> subList = tokenList.subList(index+1, k);
                        ifTrue.deferred = true;
                        ifTrue.tokenList.addAll(subList);
                        
                        Object ifFalse = tokenList.get(k+1);
                        if(!(ifFalse instanceof Statement)) {
                            Statement wrapper = new Statement();
                            wrapper.tokenList.add(ifFalse);
                            wrapper.deferred = true;
                            ifFalse = wrapper;
                        }else {
                            ((Statement)ifFalse).deferred = true;
                        }
                        
                        Statement nextStatement = new Statement();
                        nextStatement.tokenList.add(condition);
                        nextStatement.tokenList.add(ifTrue);
                        nextStatement.tokenList.add(ifFalse);
                        nextStatement.tokenList.add(this);
                        nextStatement.ordered = true;
                        
                        subList.clear();
                        tokenList.remove(index+1);
                        tokenList.remove(index+1);
                        //should not be IFStatement
                        //new if operator should not be inserted here 
                        //as if operator has higher priority than question operator
                        tokenList.set(index-1, nextStatement);
                        tokenList.remove(index);
                        return index-2;
                    }
                }
            }
            throw new ParsingException("unpaired ternary operator")
                .withSiteAndOrdinal(QuestionOperator.class, 2);
        }

        @Override
        public void eval(Context ctx) {
            Statement ifFalse = (Statement) ctx.pop();
            Statement ifTrue = (Statement) ctx.pop();
            Statement condition = (Statement) ctx.pop();
            condition.evaluate(ctx);
            Object result = convertValue(ctx, ctx.pop());
            if(!BOOL.is(result)) {
                throw new ParsingException(
                    "condition expression should return value of type boolean")
                    .withSiteAndOrdinal(QuestionOperator.class, 0);
            }
            (((Boolean) result) ? ifTrue : ifFalse).evaluate(ctx);
        }
    }
    
    //colon level 2
    //placeholder
    public static class ColonOperator extends Operator{
        public ColonOperator() {
            super(":", ":");
        }
        @Override
        public int arity() {
            return 1;
        }
        @Override
        protected int reorder0(List<Object> tokenList, int index) {
            throw new UnsupportedOperationException();
        }
        @Override
        public void eval(Context ctx) {
            throw new UnsupportedOperationException();
        }
        @Override
        protected void checkOperands(List<Object> tokenList, int index) {
        }
    }
    
    //IF level 17
    public static class IfOperator extends Operator{
        public IfOperator() {
            super("IF","if");
        }
        //If statements do not really return something.
        //Arity and returns methods are coined for this operator to
        //make it structurally same with other real operators.
        //The same is true for ElseOperator.
        @Override
        public int arity() {
            return 2;
        }
        @Override
        protected void checkOperands(List<Object> tokenList, int index) {
            checkEnoughOperands(tokenList, index, 0, 2);
        }
        @Override
        protected int reorder0(List<Object> tokenList, int index) {
            Object condition = tokenList.get(index+1);
            ((Statement)condition).deferred = true;
            Object next = tokenList.get(index+2);
            ((Statement)next).deferred = true;
            tokenList.set(index, condition);
            tokenList.set(index+1, next);
            tokenList.set(index+2, this);
            return index+2;
        }
        @Override
        public void eval(Context ctx) {
            Statement operation = (Statement) ctx.pop();
            Statement condition = (Statement) ctx.pop();
            condition.evaluate(ctx);
            if(!BOOL.is(ctx.peek())) {
                throw new ParsingException(
                    "condition expression should return value of type boolean")
                    .withSiteAndOrdinal(IfOperator.class, 2);
            }
            Boolean ret = (Boolean) ctx.pop();
            if(ret) {
                operation.evaluate(ctx);
            }
            ctx.push(IfBool.of(ret));
        }
    }
    
    //Else level 17
    public static class ElseOperator extends Operator{
        public ElseOperator() {
            super("else", "else");
        }
        @Override
        protected int reorder0(List<Object> tokenList, int index) {
            Object next = tokenList.get(index + 1);   
            ((Statement)next).deferred = true;
            tokenList.set(index, next);
            tokenList.set(index + 1, this);
            return index+1;
        }
        @Override
        public int arity() {
            return 2;
        }
        @Override
        public void eval(Context ctx) {
            Statement operation = (Statement) ctx.pop();
            IfBool bool = (IfBool) ctx.pop();
            if(!((IfBool)bool).value) {
                operation.evaluate(ctx);
            }else {
                ctx.push(bool);
            }
        }
        @Override
        protected void checkOperands(List<Object> tokenList, int index) {
            checkEnoughOperands(tokenList, index, 0, 1);
        }
    }
    
    //[] access array element/access object property
    //it is intentionally made a suffix binary operator in order to ease detection of method call syntax
    public static class BracketOperator extends SuffixBinaryOperator{
        public BracketOperator() {
            //STR for accessing length/size
            super("B","[]"
                  ,new TokenType[] {ID,STMT,STR} 
                  ,new TokenType[] {STMT});
        }
        @Override
        void eval(Context ctx, Object left, Object right) {
            
            if(STR.is(left)) {
                left = Identifier.ofLiteral(left.toString());
            }
            
            if(ID.is(right)) {
                //chaining literal identifiers like a.b.c are concatenated at compile time
                //right identifier here can only be direct/indirect reference to 
                //formerly defined number literal or string literal
                //the actual value must be restored against the global Context here
                right = ((Identifier)right).dereference(ctx);
            }
            if(NUM.is(right)) {
                right = Identifier.of(((BigDecimal)right).stripTrailingZeros().toPlainString());
            }
            else if(STR.is(right)) {
                right = Identifier.of(right.toString());
            }
            else if(right == null) {
                throw new ParsingException("property cannot be null")
                    .withSiteAndOrdinal(BracketOperator.class, 1);
            }
            else {
                throw new ParsingException("invalid value of type "+typeof(right)+" as property name")
                .withSiteAndOrdinal(BracketOperator.class, 2);
            }
            
            ctx.push(((Identifier)left).add((Identifier)right));
        }
    }
    
    //. access object property
    public static class DotOperator extends AffixBinaryOperator{
        public DotOperator() {
            super("D","."
                    //STR for accessing length/size
                  ,new TokenType[] {ID,STMT,STR}
                  ,new TokenType[] {ID});
        }
        @Override
        protected int reorder0(List<Object> tokenList, int index) {
            Object left = tokenList.get(index-1);
            Object right = tokenList.get(index+1);
            Identifier concatenated = null;
            if(ID.is(left)) {
                //hoister some of the concatenation to compile time
                //but still falls short to deal with expressions like ((a.b).c).d
                //as parentheses create statements
                Identifier leftIdentifier = (Identifier)left;
                concatenated = leftIdentifier.add((Identifier)right);
            }
            //deal with nested round statements like (a.b).c, concatenate them at compile time
            else if(STMT.is(left)) {
                Statement leftStatement = (Statement)left;
                Identifier wrappedSingleIdentifier = unwrapIdentifier(leftStatement);
                if(wrappedSingleIdentifier != null) {
                    concatenated = wrappedSingleIdentifier.add((Identifier) right);
                }
            }
            //left is string literal
            else {
                Identifier leftIdentifier = Identifier.ofLiteral((String) left);
                concatenated = leftIdentifier.add((Identifier)right);
            }
            if(concatenated != null) {
                tokenList.set(index - 1, concatenated);
                tokenList.remove(index);
                tokenList.remove(index);
                return index - 1;
            }
            return super.reorder0(tokenList, index);
        }
        @Override
        void eval(Context ctx, Object left, Object right) {
            if(left instanceof Identifier) {
                Identifier leftIdentifier = (Identifier)left;
                Identifier concatenated = leftIdentifier.add((Identifier)right);
                ctx.push(concatenated);
            }else if(STR.is(left)){
                ctx.push(Identifier.ofLiteral((String)left).add((Identifier) right));
            }else {
                throw new ParsingException("invalid token "+left+" for property reference")
                    .withSiteAndOrdinal(DotOperator.class, 0);
            }
        }
        
        private Identifier unwrapIdentifier(Statement statement) {
            if(statement.tokenList.size() != 1)
                return null;
            Object token = statement.tokenList.get(0);
            if(token instanceof Identifier)
                return (Identifier) token;
            if(token instanceof Statement)
                return unwrapIdentifier((Statement) token);
            return null;
        }
    }
    
    // ++ unary post-increment
    public static class SuffixIncrementOperator extends ChainingSuffixOperator{
        public SuffixIncrementOperator() {
            super("SI","++", COMPOSITE_TYPES);
        }
        @Override
        void eval(Context ctx, Object operand) {
            Identifier ref = getReference(ctx, operand);
            BigDecimal original = getNumber(ctx, ref);
            ref.set(ctx, original.add(BigDecimal.ONE, commonMathContext));
            ctx.push(original);
        }
    }
    // ++ unary pre-increment
    public static class PrefixIncrementOperator extends PrefixOperator{
        public PrefixIncrementOperator() {
            super("PI","++", COMPOSITE_TYPES);
        }
        @Override
        void eval(Context ctx, Object operand) {
            Identifier ref = getReference(ctx, operand);
            BigDecimal original = getNumber(ctx, ref);
            original = original.add(BigDecimal.ONE, commonMathContext);
            ref.set(ctx, original);
            ctx.push(original);
        }
    }
    
    // -- unary post-decrement
    public static class SuffixDecrementOperator extends ChainingSuffixOperator{
        public SuffixDecrementOperator() {
            super("SD","--", COMPOSITE_TYPES);
        }
        @Override
        void eval(Context ctx, Object operand) {
            Identifier ref = getReference(ctx, operand);
            BigDecimal original = getNumber(ctx, ref);
            ref.set(ctx, original.subtract(BigDecimal.ONE, commonMathContext));
            ctx.push(original); 
        }
    }
    // -- unary pre-decrement
    public static class PrefixDecrementOperator extends PrefixOperator{
        public PrefixDecrementOperator() {
            super("PD","--", COMPOSITE_TYPES);
        }
        @Override
        void eval(Context ctx, Object operand) {
            Identifier ref = getReference(ctx, operand);
            BigDecimal original = getNumber(ctx, ref);
            original = original.subtract(BigDecimal.ONE, commonMathContext);
            ref.set(ctx, original);
            ctx.push(original);
        }
    }
    
    // + unary prefix
    public static class PositiveOperator extends ArithmeticPrefixOperator{
        public PositiveOperator() {
            super("PP", "+");
        }
        @Override
        ArithmeticUnaryOperation bigDecimalOperation() throws Exception {
            return new ArithmeticUnaryOperation() {

                @Override
                public BigDecimal invoke(BigDecimal self) throws Exception {
                    return self;
                }
                
            };
        }
    }
    
    // + unary logical NOT
    public static class LogicalNotOperator extends PrefixOperator{
        public LogicalNotOperator() {
            super("NOT", "!", new TokenType[] {BOOL,STMT,ID});
        }
        @Override
        void eval(Context ctx, Object operand) {
            ctx.push(!convert(ctx, operand));
        }
        protected boolean convert(Context ctx, Object object) {
            object = convertValue(ctx, object);
            if(!BOOL.is(object)) {
                throw new ParsingException(
                        "invalid left operand for logical operator of type " + typeof(object))
                    .withSiteAndOrdinal(LogicalNotOperator.class, 4);
            }
            return (Boolean)object;
        }
    }
    
    // + unary bitwise NOT
    public static class BitwiseNotOperator extends ArithmeticPrefixOperator{
        public BitwiseNotOperator() {
            super("BNOT", "~");
        }
        @Override
        ArithmeticUnaryOperation bigDecimalOperation() throws Exception {
            return new ArithmeticUnaryOperation() {

                @Override
                public BigDecimal invoke(BigDecimal self) throws Exception {
                    return new BigDecimal(self.toBigInteger().not());
                }
                
            };
        }
    }
    
    // + additive
    /*
     * string concatenation should be implemented by the same class.
     * As they are of same precedence, the string concatenation will never be applied 
     * if implemented by another class.
     * Check
     * 3+4+'t'+4+5 
     * a+'t'+b+'t'+c where a/b/c are numbers
     */
    public static class PlusOperator extends ArithmeticBinaryOperator{
        public PlusOperator() {
            super("P", "+");
        }
        @Override
        protected void checkOperands(List<Object> tokenList, int index) {
            try {
                super.checkOperands(tokenList, index);
            } catch (Exception e) {
                /*
                 * the syntax is "if one of the operands is a string then another one 
                 * can be converted to string", so string concatenation cannot be implemented 
                 * by exposing additional constructor in super class.
                 */
                checkEnoughOperands(tokenList, index, 1, 1);
                Object left = tokenList.get(index-1);
                Object right = tokenList.get(index+1);
                if(STR.is(left) || STR.is(right)
                || isComposite(left) || isComposite(right)) {
                    return;
                }
                throw e;
            }
        }
        @Override
        protected int reorder0(List<Object> tokenList, int index) {
            Object left = tokenList.get(index-1);
            Object right = tokenList.get(index+1);
            if(STR.is(left) && STR.is(right)) {
                tokenList.set(index - 1, left + "" + right);
                tokenList.remove(index);
                tokenList.remove(index);
                return index - 1;
            }
            return super.reorder0(tokenList, index);
        }
        @Override
        ArithmeticBinaryOperation bigDecimalOperation() throws Exception {
            return new ArithmeticBinaryOperation() {
                
                @Override
                public BigDecimal invoke(BigDecimal self, BigDecimal right) throws Exception {
                    return self.add(right, commonMathContext);
                }
            };
        }
        @Override
        void eval(Context ctx, Object left, Object right) {
            left = convertValue(ctx, left);
            right = convertValue(ctx, right);
            if(STR.is(left) || STR.is(right)) {
                if(NUM.is(left)) {
                    left = ((BigDecimal)left).stripTrailingZeros().toPlainString();
                }
                if(NUM.is(right)) {
                    right = ((BigDecimal)right).stripTrailingZeros().toPlainString();
                }
                ctx.push(left + "" + right);
                return;
            }
            super.eval(ctx, left, right);
        }
    }
    
    // - unary prefix
    public static class NegativeOperator extends ArithmeticPrefixOperator{
        public NegativeOperator() {
            super("PM", "-");
        }

        @Override
        ArithmeticUnaryOperation bigDecimalOperation() throws Exception {
            return ArithmeticUnaryOperation.wrap(
                    BigDecimal.class.getMethod("negate"));
        }
        
    }
    // - additive
    public static class SubtractOperator extends ArithmeticBinaryOperator{
        public SubtractOperator() {
            super("M", "-");
        }
        @Override
        ArithmeticBinaryOperation bigDecimalOperation() throws Exception {
            return new ArithmeticBinaryOperation() {

                @Override
                public BigDecimal invoke(BigDecimal self, BigDecimal right) throws Exception {
                    return self.subtract(right, commonMathContext);
                }
                
            };
        }
    }
    
    // ** pow
    public static class PowerOperator extends ArithmeticBinaryOperator{
        public PowerOperator() {
            super("**", "**");
        }
        @Override
        ArithmeticBinaryOperation bigDecimalOperation() throws Exception {
            return new ArithmeticBinaryOperation() {

                @Override
                public BigDecimal invoke(BigDecimal self, BigDecimal right) throws Exception {
                    return self.pow(right.intValue(), commonMathContext);
                }
                
            };
        }
    }
    
    // * multiplicative
    public static class MultiplyOperator extends ArithmeticBinaryOperator{
        public MultiplyOperator() {
            super("*", "*");
        }
        @Override
        ArithmeticBinaryOperation bigDecimalOperation() throws Exception {
            return new ArithmeticBinaryOperation() {

                @Override
                public BigDecimal invoke(BigDecimal self, BigDecimal right) throws Exception {
                    return self.multiply(right, commonMathContext);
                }
                
            };
        }
    }
    
    // / multiplicative
    public static class DivideOperator extends ArithmeticBinaryOperator{
        public DivideOperator() {
            super("/", "/");
        }
        @Override
        ArithmeticBinaryOperation bigDecimalOperation() throws Exception {
            return new ArithmeticBinaryOperation() {

                @Override
                public BigDecimal invoke(BigDecimal self, BigDecimal right) throws Exception {
                    return self.divide(right, commonMathContext);
                }
                
            };
        }
    }
    
    // // Floor division
    public static class FloorDivideOperator extends ArithmeticBinaryOperator{
        public FloorDivideOperator() {
            super("//", "//");
        }
        @Override
        ArithmeticBinaryOperation bigDecimalOperation() throws Exception {
            return new ArithmeticBinaryOperation() {

                @Override
                public BigDecimal invoke(BigDecimal self, BigDecimal right) throws Exception {
                    return self.divide(right, RoundingMode.FLOOR);
                }
                
            };
        }
    }
    
    // % multiplicative
    public static class ModOperator extends ArithmeticBinaryOperator{
        public ModOperator() {
            super("%", "%");
        }
        @Override
        ArithmeticBinaryOperation bigDecimalOperation() throws Exception {
            return new ArithmeticBinaryOperation() {
                
                @Override
                public BigDecimal invoke(BigDecimal self, BigDecimal right) throws Exception {
                    return self.remainder(right, commonMathContext);
                }
            };
        }
    }
    
    // shift
    public static class ShiftLeftOperator extends ArithmeticBinaryOperator{
        public ShiftLeftOperator() {
            super("<<", "<<");
        }
        @Override
        ArithmeticBinaryOperation bigDecimalOperation() throws Exception {
            return new ArithmeticBinaryOperation() {

                @Override
                public BigDecimal invoke(BigDecimal self, BigDecimal right) throws Exception {
                    /*
                     * result will be different if ob11111 is replaced by 0b111111 and intValue is replaced 
                     * by longValue. 
                     */
                    int distance = right.intValue() & 0b11111;
                    return new BigDecimal(self.toBigInteger().shiftLeft(distance).intValue());
                }
                
            };
        }
    }
    
    public static class ShiftRightOperator extends ArithmeticBinaryOperator{
        public ShiftRightOperator() {
            super(">>", ">>");
        }
        @Override
        ArithmeticBinaryOperation bigDecimalOperation() throws Exception {
            return new ArithmeticBinaryOperation() {

                @Override
                public BigDecimal invoke(BigDecimal self, BigDecimal right) throws Exception {
                    /*
                     * From the JLS, section 15.19 (Shift Operators):
                     * 
                     * If the promoted type of the left-hand operand is long, then only the six
                     * lowest-order bits of the right-hand operand are used as the shift distance.
                     * It is as if the right-hand operand were subjected to a bitwise logical AND
                     * operator & (§15.22.1) with the mask value 0x3f (0b111111). The shift distance
                     * actually used is therefore always in the range 0 to 63, inclusive.
                     */
                    int distance = right.intValue() & 0b11111;
                    return new BigDecimal(self.toBigInteger().shiftRight(distance).intValue());
                }
                
            };
        }
    }
    
    // = assignment
    public static class AssignOperator extends AffixBinaryOperator{
        public AssignOperator() {
            super("AS","=",COMPOSITE_TYPES, ALL_TYPES);
        }
        @Override
        void eval(Context ctx, Object left, Object right) {
            ((Identifier)left).set(ctx, convertValue(ctx, right));
            ctx.push(right);
        }
    }
    
    // relational
    public static class GtOperator extends RelationalOperator{
        public GtOperator() {
            super("GT",">");
        }
        @Override
        boolean determine(int result) {
            return result>0;
        }
    }
    public static class GteOperator extends RelationalOperator{
        public GteOperator() {
            super("GTE",">=");
        }
        @Override
        boolean determine(int result) {
            return result>=0;
        }
    }
    public static class LtOperator extends RelationalOperator{
        public LtOperator() {
            super("LT","<");
        }
        @Override
        boolean determine(int result) {
            return result<0;
        }
    }
    public static class LteOperator extends RelationalOperator{
        public LteOperator() {
            super("LTE","<=");
        }
        @Override
        boolean determine(int result) {
            return result<=0;
        }
    }
    
    // equality
    public static class EQOperator extends EqualityOperator{
        public EQOperator() {
            super("EQ","==");
        }
        @Override
        boolean determine(int result) {
            return result == 0;
        }
    }
    public static class NEQOperator extends EqualityOperator{
        public NEQOperator() {
            super("NEQ","!=");
        }
        @Override
        boolean determine(int result) {
            return result != 0;
        }
    }
    
    //bitwise AND
    public static class BitwiseAndOperator extends BitwiseOperator{
        public BitwiseAndOperator() {
            super("&","&");
        }
        @Override
        boolean booleanOperation(boolean left, boolean right) {
            return left & right;
        }

        @Override
        ArithmeticIntegralBinaryOperation bigIntegerOpreation() throws Exception {
            return ArithmeticIntegralBinaryOperation.wrap(
                        BigInteger.class.getMethod("and", BigInteger.class));
        }
    }
    
    //bitwise XOR
    public static class BitwiseXOROperator extends BitwiseOperator{
        public BitwiseXOROperator() {
            super("^","^");
        }
        @Override
        boolean booleanOperation(boolean left, boolean right) {
            return left ^ right;
        }
        @Override
        ArithmeticIntegralBinaryOperation bigIntegerOpreation() throws Exception {
            return ArithmeticIntegralBinaryOperation.wrap(
                        BigInteger.class.getMethod("xor", BigInteger.class));
        }
    }
    
    //Bitwise OR
    public static class BitwiseOrOperator extends BitwiseOperator{
        public BitwiseOrOperator() {
            super("|","|");
        }
        @Override
        boolean booleanOperation(boolean left, boolean right) {
            return left | right;
        }
        @Override
        ArithmeticIntegralBinaryOperation bigIntegerOpreation() throws Exception {
            return ArithmeticIntegralBinaryOperation.wrap(
                        BigInteger.class.getMethod("or", BigInteger.class));
        }
    }
    
    //Logical AND
    public static class LogicalAndOperator extends LogicalOperator{
        public LogicalAndOperator() {
            super("&&","&&");
        }
        @Override
        void eval(Context ctx, Object left, Object right) {
            boolean leftValue = convert(ctx, left);
            if(!leftValue) {
                ctx.push(leftValue);
                return;
            }
            boolean rightVaue = convert(ctx, right);
            ctx.push(leftValue && rightVaue);
        }
    }
    
    //Logical OR
    public static class LogicalOrOperator extends LogicalOperator{
        public LogicalOrOperator() {
            super("||","||");
        }
        @Override
        void eval(Context ctx, Object left, Object right) {
            boolean leftValue = convert(ctx, left);
            if(leftValue) {
                ctx.push(leftValue);
                return;
            }
            boolean rightVaue = convert(ctx, right);
            ctx.push(leftValue || rightVaue);
        }
    }
    
    //return
    public static class ReturnOperator extends PrefixOperator{

        public ReturnOperator() {
            super("return", "return", ALL_TYPES);
        }
        
        @Override
        protected int reorder0(List<Object> tokenList, int index) {
            if(index != 0) {
                throw new ParsingException("invalid token return")
                    .withSiteAndOrdinal(getClass(), 0);
            }
            return super.reorder0(tokenList, index);
        }
        
        @Override
        void eval(Context ctx, Object operand) {
            ctx.push(operand);
            throw new ParsingException.ParsingTerminationException();
        }
        
    }
    
    public static class CommaOperator extends SuffixOperator{
        
        public CommaOperator() {
            super(",", ",", ALL_TYPES);
        }

        @Override
        protected int reorder0(List<Object> tokenList, int index) {
            int idx = super.reorder0(tokenList, index);
            ((Statement) tokenList.get(idx)).deferred = true;
            return idx;
        }

        @Override
        void eval(Context ctx, Object operand) {
            ctx.push(convertValue(ctx, operand));
        }
        
    }
    
    public static class CallOperator extends SuffixBinaryOperator{
        
        public CallOperator() {
            super("CALL", "()", new TokenType[] {STMT, ID}, new TokenType[] {STMT});
        }

        @Override
        public int arity() {
            return 2;
        }

        @Override
        void eval(Context ctx, Object first, Object second) {
            //first must be ID
            //literal boolean/null/number or values of these types returned by STMT
            //is illegal for property access syntax and will be blocked by DotOperator
            Statement params = (Statement)second;
            Object[] tokens = params.tokens;
            TokenType[] types = new TokenType[tokens.length];
            for(int p = 0,l = tokens.length;p<l; ++p) {
                Object param = convertValue(ctx, tokens[p]);
                tokens[p] = param;
                TokenType scriptType;
                if(TokenType.STR.is(param)) {
                    scriptType = TokenType.STR;
                }else if(TokenType.NUM.is(param)) {
                    scriptType = TokenType.NUM;
                }else if(TokenType.BOOL.is(param)) {
                    scriptType = TokenType.BOOL;
                }else if(param == null) {
                    scriptType = TokenType.NULL;
                }else {
                    throw new ParsingException("object of type "
                            +typeof(param)+" is not available for method parameter")
                        .withSiteAndOrdinal(CallOperator.class, 0);
                }
                types[p] = scriptType;
            }
            ctx.push(((Identifier)first).call(ctx, tokens, types));
        }
        
    }
}
