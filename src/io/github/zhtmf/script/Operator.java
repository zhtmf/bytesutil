package io.github.zhtmf.script;

import static io.github.zhtmf.script.TokenType.*;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import io.github.zhtmf.script.Operators.ColonOperator;
import io.github.zhtmf.script.Operators.IfOperator;

/**
 * Stateless, reusable operator objects.
 * 
 * @author dzh
 */
abstract class Operator {
    
    static final TokenType[] ARITHMETIC_OPERAND_TYPES = new TokenType[] { NUM, STMT, ID };

    static final TokenType[] ALL_TYPES = new TokenType[] { ID, STMT, NUM, STR, BOOL, NULL };

    static final TokenType[] COMPOSITE_TYPES = new TokenType[] { ID, STMT };

    final String name;
    final String op;
    
    private Operator next;
    
    public Operator(String name, String op) {
        this.name = name;
        this.op = op;
    }
    
    /**
     * Reorder the operator itself and its operands to a reverse-polish expression
     * and group them together as an intermediate, temporary statement.
     * 
     * @param tokenList token list
     * @param index     the position where the operator resides in the token list.
     * @return
     */
    public final int reorder(List<Object> tokenList, int index) {
        try {
            checkOperands(tokenList, index);
            index = reorder0(tokenList, index);
        } catch (ParsingException e) {
            Operator next = this.next;
            if(next == null)
                throw e;
            tokenList.set(index, next);
        }
        return index;
    }

    /**
     * Number operands that will be consumed by this operator during evaluation.
     * <p>
     * For most operator this method returns the actual operand consumed which is in
     * accordance with its common definition while for others this number is purely
     * hypothetical. It is only provided for passing the integrity check and their
     * runtime behavior may differ from consuming this many operands. Examples for
     * such operators are {@link IfOperator}, {@link ColonOperator} etc.
     * <p>
     * This is the same for the return value of {@link #returns() returns}.
     * 
     * @return number of operands
     */
    public abstract int arity();

    /**
     * Consumes operands from operand stack of this <tt>Context</tt> and push any
     * possible results back to the operand stack.
     * 
     * @param ctx   <tt>Context</tt> object
     */
    public abstract void eval(Context ctx);
    protected abstract int reorder0(List<Object> tokenList, int index);
    protected abstract void checkOperands(List<Object> tokenList, int index);
    
    @Override
    public String toString() {
        return "@"+name+"["+op+"]";
    }
    
    /**
     * Set the "next" operator with same string representation as this one.
     * <p>
     * The "next" operator must appear later in precedence order than this one, when
     * {@link #reorder(List, int) reorder} of this operator fails it
     * should be replaced by the next operator and {@link #reorder(List, int)
     * reorder} of the next operator get called in later loops.
     * 
     * @param next  next operator
     */
    public void setNext(Operator next) {
        this.next = next;
    }
    
    /**
     * Number of result objects that will be produced by this operator during
     * evaluation.
     * <p>
     * Used in integrity check.
     * 
     * @return Number of result objects that will be produced by this operator
     */
    public int returns() {
        return 1;
    }
    
    static void checkEnoughOperands(
            List<Object> tokenList
            , int current, int leftCount, int rightCount) {
        if(current < leftCount) {
            throw new ParsingException("expected "+leftCount+" preceding tokens but found "+current)
                .withSiteAndOrdinal(Operator.class, 3);
        }
        int gap2 = tokenList.size() - current - 1;
        if(gap2 < rightCount) {
            throw new ParsingException("expected "+rightCount+" succeeding tokens but found "+gap2)
                .withSiteAndOrdinal(Operator.class, 3);
        }
    }
    
    static boolean isComposite(Object token) {
        return isTypes(token, COMPOSITE_TYPES);
    }
    
    static Identifier getReference(Context ctx, Object value) {
        if(ID.is(value)) {
            return (Identifier) value;
        }
        //STMTs should have already been eliminated until this phase
        throw new ParsingException("reference expected but found " + typeof(value))
            .withSiteAndOrdinal(Operator.class, 0);
    }
    
    static BigDecimal getNumber(Context ctx, Identifier ref) {
        Object value = Operator.convertValue(ctx, ref.dereference(ctx));
        if(!NUM.is(value)) {
            throw new ParsingException(
                "number expected but found "+ typeof(value))
                .withSiteAndOrdinal(Operator.class, 1);
        }
        return (BigDecimal)value;
    }
    
    static BigDecimal convertToNumber(Context ctx, Object obj) {
        obj = convertValue(ctx, obj);
        if(!(obj instanceof BigDecimal)) {
            throw new ParsingException(
                "invalid operand "+obj+" of type "+typeof(obj)+" for arithmetic operation")
                .withSiteAndOrdinal(Operator.class, 2);
        }
        return (BigDecimal) obj;
    }
    
    static Object convertValue(Context ctx, Object value) {
        if(ID.is(value)) {
            return convertValue(ctx, ((Identifier)value).dereference(ctx));
        }
        if(STMT.is(value)) {
            ((Statement)value).evaluate(ctx);
            return ctx.pop();
        }
        if(NULL.is(value)) {
            return null;
        }
        if(!NUM.is(value)) {
            if(value instanceof BigInteger) {
                return new BigDecimal((BigInteger)value);
            }
            if(value instanceof Number) {
                return new BigDecimal(((Number)value).doubleValue());
            }
        }
        return value;
    }
    
    static String typeof(Object value) {
        if(value == null) {
            return "<null>";
        }
        return value.getClass().getName();
    }
}

abstract class UnaryOperator extends Operator {
    protected TokenType[] operandTypes;
    
    public UnaryOperator(String name,String op,TokenType[] operandTypes) {
        super(name,op);
        this.operandTypes = operandTypes;
    }
    
    @Override
    public int arity() {
        return 1;
    }
    
    @Override
    public void eval(Context ctx) {
        eval(ctx,ctx.pop());
    }
    
    abstract void eval(Context ctx, Object operand);
}

abstract class PrefixOperator extends UnaryOperator {
    
    public PrefixOperator(String name,String op, TokenType[] operandTypes) {
        super(name,op,operandTypes);
    }
    
    protected void checkOperands(List<Object> tokenList, int index) {
        checkEnoughOperands(tokenList, index, 0, 1);
        Object token = tokenList.get(index + 1);
        if(!isTypes(token,super.operandTypes)) {
            throw new ParsingException("unexpected token "+token)
                .withSiteAndOrdinal(PrefixOperator.class, 1);
        }
        /*
         * Without this checking a+b will be parsed as a(+b) and additive operator will
         * never get a chance to be applied, although unary plus operator does have a
         * higher precedence.
         * 
         * Similar checking is done in SuffixOperator.
         * 
         * PrefixOperators must be the first token or preceded by another operator 
         * for it to be potentially valid.
         */
        if(!(index==0 || (tokenList.get(index - 1) instanceof Operator))){
            throw new ParsingException("unexpected operator " + this.op)
                .withSiteAndOrdinal(PrefixOperator.class, 2);
        }
    }
    
    @Override
    protected int reorder0(List<Object> tokenList, int index) {
        Statement statement = new Statement();
        statement.tokenList.add(tokenList.get(index+1));
        statement.tokenList.add(this);
        statement.ordered = true;
        tokenList.set(index, statement);
        tokenList.remove(index+1);
        return index;
    }
}

abstract class ArithmeticPrefixOperator extends PrefixOperator {
    public ArithmeticPrefixOperator(String name,String op) {
        super(name, op, ARITHMETIC_OPERAND_TYPES);
    }
    
    @Override
    protected int reorder0(List<Object> tokenList, int index) {
        Object operand = tokenList.get(index+1);
        if(NUM.is(operand)) {
            try {
                tokenList.set(index, 
                        bigDecimalOperation().invoke((BigDecimal)operand));
            } catch (Exception e) {
                throw new ParsingException(e)
                    .withSiteAndOrdinal(ArithmeticPrefixOperator.class, 1);
            }
            tokenList.remove(index + 1);
            return index;
        }
        return super.reorder0(tokenList, index);
    }
    
    @Override
    void eval(Context ctx, Object val) {
        try {
            ctx.push(bigDecimalOperation().invoke(convertToNumber(ctx, val)));
        } catch (Exception e) {
            throw new ParsingException(e)
                .withSiteAndOrdinal(ArithmeticPrefixOperator.class, 0);
        }
    }
    
    abstract ArithmeticUnaryOperation bigDecimalOperation() throws Exception;
    
}

abstract class SuffixOperator extends UnaryOperator {
    public SuffixOperator(String name,String op, TokenType[] operandTypes) {
        super(name,op,operandTypes);
    }
    protected void checkOperands(List<Object> tokenList, int index) {
        checkEnoughOperands(tokenList, index, 1, 0);
        Object token = tokenList.get(index-1);
        if(!isTypes(token,super.operandTypes)) {
            throw new ParsingException("unexpected token "+token)
                .withSiteAndOrdinal(SuffixOperator.class, 1);
        }
        //SuffixOperators must be the last token or succeeded by another operator 
        //for it to be potentially valid.
        if(!(index == tokenList.size()-1 || (tokenList.get(index+1) instanceof Operator))){
            throw new ParsingException("unexpected operator "+this.op)
                .withSiteAndOrdinal(SuffixOperator.class, 2);
        }
    }
    @Override
    protected int reorder0(List<Object> tokenList, int index) {
        Statement statement = new Statement();
        statement.tokenList.add(tokenList.get(index-1));
        statement.tokenList.add(this);
        statement.ordered = true;
        tokenList.set(index-1, statement);
        tokenList.remove(index);
        return index-1;
    }
}

abstract class AffixBinaryOperator extends Operator {
    protected TokenType[] leftOperandTypes;
    protected TokenType[] rightOperandTypes;
    public AffixBinaryOperator(
            String name
            ,String op
            ,TokenType[] leftOperandTypes
            ,TokenType[] rightOperandTypes) {
        super(name,op);
        this.leftOperandTypes = leftOperandTypes;
        this.rightOperandTypes = rightOperandTypes;
    }
    @Override
    public int arity() {
        return 2;
    }
    
    protected void checkOperands(List<Object> tokenList, int index) {
        checkEnoughOperands(tokenList, index, 1, 1);
        Object left = tokenList.get(index-1);
        Object right = tokenList.get(index+1);
        if(!isTypes(left,leftOperandTypes)) {
            throw new ParsingException("unexpected left token "+left)
                .withSiteAndOrdinal(AffixBinaryOperator.class, 1);
        }
        if(!isTypes(right,rightOperandTypes)) {
            throw new ParsingException("unexpected right token "+right)
                .withSiteAndOrdinal(AffixBinaryOperator.class, 2);
        }
    }
    @Override
    protected int reorder0(List<Object> tokenList, int index) {
        Object left = tokenList.get(index-1);
        Object right = tokenList.get(index+1);
        Statement statement = new Statement();
        statement.tokenList.add(left);
        statement.tokenList.add(right);
        statement.tokenList.add(tokenList.get(index));
        statement.ordered = true;
        tokenList.set(index-1, statement);
        tokenList.remove(index);
        tokenList.remove(index);
        return index-1;
    }
    @Override
    public void eval(Context ctx) {
        Object right = ctx.pop();
        Object left = ctx.pop();
        eval(ctx,left,right);
    }
    abstract void eval(Context ctx, Object left, Object right);
}

abstract class ArithmeticBinaryOperator extends AffixBinaryOperator {
    public ArithmeticBinaryOperator(String name,String op) {
        super(name,op
            , ARITHMETIC_OPERAND_TYPES
            , ARITHMETIC_OPERAND_TYPES);
    }
    @Override
    protected int reorder0(List<Object> tokenList, int index) {
        Object left = tokenList.get(index-1);
        Object right = tokenList.get(index+1);
        if(NUM.is(left) && NUM.is(right)) {
            try {
                tokenList.set(index-1, bigDecimalOperation()
                        .invoke((BigDecimal)left, (BigDecimal)right));
            } catch (Exception e) {
                throw new ParsingException(e)
                    .withSiteAndOrdinal(ArithmeticBinaryOperator.class, 1);
            }
            tokenList.remove(index);
            tokenList.remove(index);
            return index - 1;
        }
        return super.reorder0(tokenList, index);
    }
    @Override
    void eval(Context ctx, Object left, Object right) {
        try {
            ctx.push(
                    bigDecimalOperation()
                        .invoke(convertToNumber(ctx, left), convertToNumber(ctx, right)));
        } catch (Exception e) {
            throw new ParsingException(e)
                .withSiteAndOrdinal(ArithmeticBinaryOperator.class, 0);
        }
    }
    
    abstract ArithmeticBinaryOperation bigDecimalOperation() throws Exception;
}

abstract class BitwiseOperator extends ArithmeticBinaryOperator{
    public BitwiseOperator(String name,String op) {
        super(name,op);
    }
    @Override
    protected void checkOperands(List<Object> tokenList, int index) {
        try {
            super.checkOperands(tokenList, index);
        } catch (ParsingException e) {
            checkEnoughOperands(tokenList, index, 1, 1);
            Object left = tokenList.get(index-1);
            Object right = tokenList.get(index+1);
            if(BOOL.is(left) && (BOOL.is(right) || isComposite(right))
            || (BOOL.is(right) && isComposite(left))) {
                return;
            }
            throw e;
        }
    }
    @Override
    void eval(Context ctx, Object left, Object right) {
        left = convertValue(ctx, left);
        right = convertValue(ctx, right);
        //overridden to permit boolean values
        if(BOOL.is(left) && BOOL.is(right)) {
            ctx.push(booleanOperation((Boolean)left,(Boolean)right));
            return;
        }
        super.eval(ctx, left, right);
    }
    @Override
    ArithmeticBinaryOperation bigDecimalOperation() throws Exception {
        return new ArithmeticBinaryOperation() {
            
            @Override
            public BigDecimal invoke(BigDecimal self, BigDecimal right) throws Exception {
                return new BigDecimal(
                        bigIntegerOpreation().invoke(self.toBigInteger(), right.toBigInteger()));
            }
            
        };
    }
    abstract boolean booleanOperation(boolean left, boolean right);
    abstract ArithmeticIntegralBinaryOperation bigIntegerOpreation() throws Exception;
}

abstract class MutualExclusivityOperator extends AffixBinaryOperator{

    public MutualExclusivityOperator(String name, String op
            , TokenType[] operandTypes) {
        super(name, op, operandTypes, operandTypes);
    }
    
    @Override
    protected void checkOperands(List<Object> tokenList, int index) {
        super.checkOperands(tokenList, index);
        Object left = tokenList.get(index-1);
        Object right = tokenList.get(index+1);
        if(!isComposite(left) && !isComposite(right)) {
            for(TokenType type:super.leftOperandTypes) {
                if((type.is(left) && !type.is(right))
                || (!type.is(left) && type.is(right))) {
                    throw new ParsingException("both operands should be of type "+type)
                        .withSiteAndOrdinal(MutualExclusivityOperator.class, 0);
                }
            }
        }
    }
}

abstract class RelationalOperator extends MutualExclusivityOperator {
    public RelationalOperator(String name,String op) {
        super(name,op,new TokenType[] {ID,STMT,NUM,STR,BOOL});
    }
    protected RelationalOperator(String name,String op,TokenType[] operandTypes) {
        super(name,op,operandTypes);
    }
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    void eval(Context ctx, Object left, Object right) {
        left = convertValue(ctx, left);
        right = convertValue(ctx, right);
        if(!(left instanceof Comparable) || !(right instanceof Comparable)) {
            throw new ParsingException(
                    "incomparable operands for relational operator of type "
                            +typeof(left)+" and "+typeof(right))
                .withSiteAndOrdinal(RelationalOperator.class, 1);
        }
        dispose(ctx, determine(((Comparable)left).compareTo(right)));
    }
    
    protected void dispose(Context ctx, boolean result) {
        ctx.push(result);
    }
    
    abstract boolean determine(int result);
}

abstract class EqualityOperator extends RelationalOperator {
    public EqualityOperator(String name,String op) {
        super(name,op, ALL_TYPES);
    }
    @Override
    void eval(Context ctx, Object left, Object right) {
        left = convertValue(ctx, left);
        right = convertValue(ctx, right);
        if(right == null) {
            dispose(ctx, determine(left == null ? 0 : 1));
            return;
        }
        if(left == null) {
            dispose(ctx, determine(1));
            return;
        }
        super.eval(ctx, left, right);
    }
}

abstract class LogicalOperator extends MutualExclusivityOperator {
    public LogicalOperator(String name,String op) {
        super(name,op, new TokenType[] {ID,STMT,BOOL});
    }
    @Override
    protected int reorder0(List<Object> tokenList, int index) {
        //shortcut behavior
        Object right = tokenList.get(index+1);
        if(right instanceof Statement) {
            ((Statement)right).deferred = true;
        }
        return super.reorder0(tokenList, index);
    }
    
    protected static boolean convert(Context ctx, Object object) {
        object = convertValue(ctx, object);
        if(!BOOL.is(object)) {
            throw new ParsingException(
                    "invalid left operand for logical operator of type " + typeof(object))
                    .withSiteAndOrdinal(LogicalOperator.class, 0);
        }
        return (Boolean)object;
    }
}

//------- BigDecimal/BigInteger method wrapper interfaces -------

interface ArithmeticBinaryOperation{
    BigDecimal invoke(BigDecimal self, BigDecimal right) throws Exception;
}
interface ArithmeticIntegralBinaryOperation{
    BigInteger invoke(BigInteger self, BigInteger right) throws Exception;
    static ArithmeticIntegralBinaryOperation wrap(Method method) {
        return new ArithmeticIntegralBinaryOperation() {
            
            @Override
            public BigInteger invoke(BigInteger self, BigInteger right) throws Exception {
                return (BigInteger) method.invoke(self, right);
            }
        };
    }
}

interface ArithmeticUnaryOperation{
    BigDecimal invoke(BigDecimal self) throws Exception;
    static ArithmeticUnaryOperation wrap(Method method) {
        return new ArithmeticUnaryOperation() {
            
            @Override
            public BigDecimal invoke(BigDecimal self) throws Exception {
                return (BigDecimal) method.invoke(self);
            }
        };
    }
}