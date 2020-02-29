package io.github.zhtmf.script;

import static io.github.zhtmf.script.TokenType.BOOL;
import static io.github.zhtmf.script.TokenType.ID;
import static io.github.zhtmf.script.TokenType.NUM;
import static io.github.zhtmf.script.TokenType.OP;
import static io.github.zhtmf.script.TokenType.STR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.junit.Assert;
import org.junit.Test;

import io.github.zhtmf.script.Operators.BracketOperator;
import io.github.zhtmf.script.Operators.IfOperator;
import io.github.zhtmf.script.Operators.LogicalNotOperator;
import io.github.zhtmf.script.Operators.QuestionOperator;
import io.github.zhtmf.script.Operators.ReturnOperator;
import io.github.zhtmf.script.test.test1.Test2;
import io.github.zhtmf.script.test.test1.TestMethodCall;
import io.github.zhtmf.script.test.test1.TestObject;
import io.github.zhtmf.script.test.test1.TestObjectV;

public class ScriptTest {
    
    private static final ScriptEngine nashorn = new ScriptEngineManager().getEngineByName("nashorn");
    private static final ScriptEngine mine = new ScriptEngineManager().getEngineByName("__zhtmf-script"); 
    
    public static void main(String[] args) throws Exception {
        io.github.zhtmf.script.test.test1.TestMethodCall.class.getMethod("static1", int.class);
    }
    
  //exception for plain statement not ending with semicolon
    @Test
    public void testD() {
        try {
            new ScriptAssertion(" {a = a + (b=4+6)} ").compile();
            Assert.fail();
        } catch (Exception e) {
            testException(e,Script.class,23);
        }
    }

    //no exception for last plain statement without semicolon at the end
    //ignore preceding whitespace of block statements
    @Test
    public void testE() {
        new ScriptAssertion(" b=3;{c--;}a = a +4+6 ")
                .compile()
                .hasChild(3)
                .child(0)
                    .hasChild(3)
                    .token(ID, "b")
                    .token(NUM, "3")
                    .token(OP, "=")
                    .and()
                .child(1)
                    //single statement in a block is flattened
                    .hasChild(2)
                    .token(ID, "c")
                    .token(OP, "--")
                    .and()
                .child(2)
                    .hasChild(7)
                    .token(ID, "a")
                    .token(ID, "a")
                    .token(NUM, "4")
                    .token(OP, "+")
                    .token(NUM, "6")
                    .token(OP, "+")
                    .token(OP, "=")
                    .and()
                 ;
        testEvaluation(" b=3;{c--;}a = a +4+6;a ", asMap("a",10,"b",3,"c",13));
    }

    //keep empty block statement in if structure
    @Test
    public void testF() {
        new ScriptAssertion("if(a==3){}")
                .compile()
                .child(0)
                    .hasChild(3)
                    .child()
                        .token(ID, "a")
                        .token(NUM, "3")
                        .token(OP, "==")
                        .parent()
                    .child()
                        .isEmpty()
                        .parent()
                    .token(OP, "if")
                ;
    }

    //discard isolated empty block statement
    @Test
    public void testG1() {
        //{{}{ { {} {   } } }{}{}}{} is entirely discarded
        //extra ; is discarded
        new ScriptAssertion("a=3;{{}{ { {} {   } } }{}{}}{};b=4") 
                .compile()
                .hasChild(2)
                .child(0)
                    .token(ID, "a")
                    .token(NUM, "3")
                    .token(OP, "=")
                    .and()
                .child(1)
                    .token(ID, "b")
                    .token(NUM, "4")
                    .token(OP, "=")
                ;
    }

    //empty nested parentheses structure
    @Test
    public void testK() {
        new ScriptAssertion("a+((((b))))")
            .tokenize()
            .hasChild(1).child(0)
                .hasChild(3)
                .token(ID, "a")
                .token(OP, "+")
                .child()
                    .child()
                        .child()
                            .child()
                                .token(ID, "b")
                ;
    }
    
    @Test
    public void testK1() {
        assertScriptException(new ScriptAssertion("a+(b+c"), Script.class, 20);
    }
    
    //bracket wrapping single reference
    @Test
    public void testL() {
        new ScriptAssertion("c=a[b]")
            .tokenize()
            .hasChild(1).child(0)
                .hasChild(5)
                .token(ID, "c")
                .token(OP, "=")
                .token(ID, "a")
                .token(OP, "[]")
                .child()
                    .token(ID, "b")
                    .parent()
                ;
    }
    
    //bracket nesting parentheses
    @Test
    public void testM() {
        new ScriptAssertion("c=a[b+(a-4)]")
            .tokenize()
            .child(0)
                .token(ID, "c")
                .token(OP, "=")
                .token(ID, "a")
                .token(OP, "[]")
                .child()
                    .token(ID, "b")
                    .token(OP, "+")
                    .child()
                        .token(ID, "a")
                        .token(OP, "-")
                        .token(NUM, "4")
                ;
    }
    
    //nested bracket structure
    @Test
    public void testN() {
        new ScriptAssertion("c=a[b+c[a-4]] + a+3")
            .tokenize()
            .hasChild(1).child(0)
                .hasChild(9)
                .token(ID, "c")
                .token(OP, "=")
                .token(ID, "a")
                .token(OP, "[]")
                .child()
                    .token(ID, "b")
                    .token(OP, "+")
                    .token(ID, "c")
                    .token(OP, "[]")
                    .child()
                        .token(ID, "a")
                        .token(OP, "-")
                        .token(NUM, "4")
                        .parent()
                    .parent()
                .token(OP, "+")
                .token(ID, "a")
                .token(OP, "+")
                .token(NUM, "3")
                ;
    }
    
    //nested bracket structure1
    @Test
    public void testO() {
        new ScriptAssertion("c=a[b[c]]")
            .tokenize()
            .hasChild(1).child(0)
                .hasChild(5)
                .token(ID, "c")
                .token(OP, "=")
                .token(ID, "a")
                .token(OP, "[]")
                .child()
                    .token(ID, "b")
                    .token(OP, "[]")
                    .child()
                        .token(ID, "c")
                ;
    }
    
    //integration test for parentheses, braces and brackets
    @Test
    public void testP() {
        new ScriptAssertion("if(a+b>3){c=(a[b+a[b[3]]]-c)*4;++c;}")
            .tokenize()
            .hasChild(1).child(0)
                .token(OP, "if")
                .child(1)
                    .token(ID, "a")
                    .token(OP, "+")
                    .token(ID, "b")
                    .token(OP, ">")
                    .token(NUM, "3")
                    .parent()
                .child(2)
                    .hasChild(2)
                    .child()
                        .token(ID, "c")
                        .token(OP, "=")
                        .child()
                            .token(ID, "a")
                            .token(OP, "[]")
                            .child()
                                .token(ID, "b")
                                .token(OP, "+")
                                .token(ID, "a")
                                .token(OP, "[]")
                                .child()
                                    .token(ID, "b")
                                    .token(OP, "[]")
                                    .child()
                                        .token(NUM, "3")
                                        .parent()
                                    .parent()
                                .parent()
                            .token(OP, "-")
                            .token(ID, "c")
                            .parent()
                        .parent()
                    .child()
                        .token(OP, "++")
                        .token(ID, "c")
                ;
    }
    
    /*
     * testP1 - P9
     * within brackets/parentheses or in plain statements:
     * strings appear at the beginning
     * strings appear at he end
     * strings appear after white spaces
     * strings appear before white spaces at the end
     * numbers appear at the beginning
     * numbers appear at he end
     * numbers appear after white spaces
     */
    @Test
    public void testP1() {
        new ScriptAssertion("a['b_{}[]*&#($&*(&#_ASDA']")
            .tokenize()
            .child(0)
                .hasChild(3)
                .token(ID, "a")
                .token(OP, "[]")
                .child(2)
                    .hasChild(1)
                    .token(STR, "b_{}[]*&#($&*(&#_ASDA")
                ;
    }
    
    @Test
    public void testP2() {
        new ScriptAssertion("a[  'b_{}[]*&#($&*(&#_ASDA']")
            .tokenize()
            .child(0)
                .hasChild(3)
                .token(ID, "a")
                .token(OP, "[]")
                .child(2)
                    .hasChild(1)
                    .token(STR, "b_{}[]*&#($&*(&#_ASDA")
                ;
    }
    
    @Test
    public void testP3() {
        new ScriptAssertion("a[  'b_{}[]*&#($&*(&#_ASDA'  ]")
            .tokenize()
            .child(0)
                .hasChild(3)
                .token(ID, "a")
                .token(OP, "[]")
                .child(2)
                    .hasChild(1)
                    .token(STR, "b_{}[]*&#($&*(&#_ASDA")
                ;
    }
    
    @Test
    public void testP4() {
        new ScriptAssertion("a*('bc'+'def')")
            .tokenize()
            .child(0)
                .hasChild(3)
                .token(ID, "a")
                .token(OP, "*")
                .child(2)
                    .hasChild(3)
                    .token(STR, "bc")
                    .token(OP, "+")
                    .token(STR, "def")
                ;
    }
    
    @Test
    public void testP5() {
        new ScriptAssertion("a*( 'bc'+'def' )")
            .tokenize()
            .child(0)
                .hasChild(3)
                .token(ID, "a")
                .token(OP, "*")
                .child(2)
                    .hasChild(3)
                    .token(STR, "bc")
                    .token(OP, "+")
                    .token(STR, "def")
                ;
    }
    
    @Test
    public void testP6() {
        new ScriptAssertion("'bc'+'def';")
            .tokenize()
            .child(0)
                .hasChild(3)
                .token(STR, "bc")
                .token(OP, "+")
                .token(STR, "def")
                ;
    }
    
    @Test
    public void testP7() {
        new ScriptAssertion(" 'bc'+'def'; ")
            .tokenize()
            .child(0)
                .hasChild(3)
                .token(STR, "bc")
                .token(OP, "+")
                .token(STR, "def")
                ;
    }
    
    @Test
    public void testP8() {
        new ScriptAssertion(" 1.234+(3.01+4 )+( 5+6.1)+( 7.1*7.1 )+456.777"
                + "*accc[1.2] + accc[ 1.2] + accc[1.2 ] + accc[ 1.2 ];")
            .tokenize()
            .child(0)
                .hasChild(25)
                .token(NUM,"1.234")
                .token(OP, "+")
                .child(2)
                    .hasChild(3)
                    .token(NUM, "3.01")
                    .token(OP, "+")
                    .token(NUM, "4")
                    .parent()
                .token(OP, "+")
                .child(4)
                    .hasChild(3)
                    .token(NUM, "5")
                    .token(OP, "+")
                    .token(NUM, "6.1")
                    .parent()
                .token(OP, "+")
                .child(6)
                    .hasChild(3)
                    .token(NUM, "7.1")
                    .token(OP, "*")
                    .token(NUM, "7.1")
                    .parent()
                .token(OP, "+")
                .token(NUM, "456.777")
                .token(OP, "*")
                .token(ID, "accc")
                .token(OP, "[]")
                .child()
                    .hasChild(1)
                    .token(NUM, "1.2")
                    .parent()
                .token(OP, "+")
                .token(ID, "accc")
                .token(OP, "[]")
                .child()
                    .hasChild(1)
                    .token(NUM, "1.2")
                    .parent()
                .token(OP, "+")
                .token(ID, "accc")
                .token(OP, "[]")
                .child()
                    .hasChild(1)
                    .token(NUM, "1.2")
                    .parent()
                .token(OP, "+")
                .token(ID, "accc")
                .token(OP, "[]")
                .child()
                    .hasChild(1)
                    .token(NUM, "1.2")
                    .parent()
                ;
    }
    
    @Test
    public void testP9() {
        new ScriptAssertion(" 't'-- +false; ")
        .tokenize()
        .child(0)
            .hasChild(4)
            .token(STR, "t")
            .token(OP, "--")
            .token(OP, "+")
            .token(BOOL, "false")
            ;
    }
    
    //recognition for single operator
    @Test
    public void testQ() {
        new ScriptAssertion("a= '6sadfasf'- --b+3.12*4.567- ++c -false")
            .tokenize()
            .hasChild(1)
                .child(0)
                    .hasChild(15)
                    .token(0, TokenType.ID).token(0, "a")
                    .token(1, TokenType.OP).token(1, "=")
                    .token(2, TokenType.STR).token(2, "6sadfasf")
                    .token(3, TokenType.OP).token(3, "-")
                    .token(4, TokenType.OP).token(4, "--")
                    .token(5, TokenType.ID).token(5, "b")
                    .token(6, TokenType.OP).token(6, "+")
                    .token(7, TokenType.NUM).token(7, "3.12")
                    .token(8, TokenType.OP, "*")
                    .token(9, TokenType.NUM, "4.567")
                    .token(10, TokenType.OP, "-")
                    .token(11, TokenType.OP, "++")
                    .token(12, TokenType.ID, "c")
                    .token(13, TokenType.OP, "-")
                    .token(14, TokenType.BOOL, "false")
            ;
    }
    
    //recognition for consecutive operators
    @Test
    public void testR() {
        new ScriptAssertion("a=+b+++c*--d;")
            .tokenize()
            .hasChild(1)
            .child(0)
                .hasChild(10)
                .token(0, TokenType.ID,"a")
                .token(1, TokenType.OP,"=")
                .token(2, TokenType.OP,"+")
                .token(3, TokenType.ID,"b")
                .token(4, TokenType.OP,"++")
                .token(5, TokenType.OP,"+")
                .token(6, TokenType.ID,"c")
                .token(7, TokenType.OP,"*")
                .token(8, TokenType.OP,"--")
                .token(9, TokenType.ID,"d")
            ;
    }
    
    //recognition for operands as parentheses structure
    @Test
    public void testS() {
        new ScriptAssertion("(a++)")
            .tokenize()
            .hasChild(1)
            .child(0)
                .hasChild(1)
                .child(0)
                    .token(ID, "a")
                    .token(OP, "++")
                ;
        new ScriptAssertion("a=((+b+(1.23+2.45)*3.11)-c---(a++))")
            .tokenize()
            .hasChild(1)
            .child(0)
                .hasChild(3)
                .token(0, TokenType.ID,"a")
                .token(1, TokenType.OP,"=")
                .child(2)
                    .hasChild(6)
                    .child(0)
                        .token(TokenType.OP, "+")
                        .token(TokenType.ID, "b")
                        .token(OP, "+")
                        .child(3)
                            .hasChild(3)
                            .token(NUM, "1.23")
                            .token(OP, "+")
                            .token(NUM, "2.45")
                            .parent()
                        .token(4, OP, "*")
                        .token(NUM, "3.11")
                        .parent()
                    .token(1, OP, "-")
                    .token(ID, "c")
                    .token(OP, "--")
                    .token(OP, "-")
                    .child(5)
                        .hasChild(2)
                        .token(ID, "a")
                        .token(OP, "++")
            ;
    }
    
    //operators within brackets
    @Test
    public void testT() {
        new ScriptAssertion("abdef=((1.234+b+abdef[++a+b++-3.453])-cCcC[4.234-1.234]*5.7+a['$_{}[]./*_$de$'])")
            .tokenize()
            .child(0)
                .hasChild(3)
                .token(ID, "abdef")
                .token(OP, "=")
                .child(2)
                    .hasChild(11)
                    .child(0)
                        .hasChild(7)
                        .token(NUM, "1.234")
                        .token(OP, "+")
                        .token(ID, "b")
                        .token(OP, "+")
                        .token(ID, "abdef")
                        .token(5, OP, "[]")
                        .child(6)
                            .hasChild(7)
                            .token(OP, "++")
                            .token(ID, "a")
                            .token(OP, "+")
                            .token(ID, "b")
                            .token(OP, "++")
                            .token(OP, "-")
                            .token(NUM, "3.453")
                            .parent()
                        .parent()
                    .token(1, OP, "-")
                    .token(ID, "cCcC")
                    .token(OP, "[]")
                    .child(4)
                        .hasChild(3)
                        .token(NUM, "4.234")
                        .token(OP, "-")
                        .token(NUM, "1.234")
                        .parent()
                    .token(5, OP, "*")
                    .token(NUM, "5.7")
                    .token(OP, "+")
                    .token(ID, "a")
                    .token(OP, "[]")
                    .child(10)
                        .hasChild(1)
                        .token(STR, "$_{}[]./*_$de$")
            ;
    }
    
    //integration tests for cases from testQ to T
    @Test
    public void testU() {
        new ScriptAssertion("if( abcdef=cc[3*bb++]*2.1){abcdef[c[3]]=c--*2;++c;d5=((--_$c1_+4.5)/cc_123$1)+4;}")
            .tokenize()
            .child(0)
                .hasChild(3)
                .token(OP, "if")
                .child(1)
                    .hasChild(7)
                    .token(ID, "abcdef")
                    .token(OP, "=")
                    .token(ID, "cc")
                    .token(OP, "[]")
                    .child(4)
                        .hasChild(4)
                        .token(NUM, "3")
                        .token(OP, "*")
                        .token(ID, "bb")
                        .token(OP, "++")
                        .parent()
                    .token(5, OP, "*")
                    .token(NUM, "2.1")
                    .parent()
                .child(2)
                    .hasChild(3)
                    .child(0)
                        .hasChild(8)
                        .token(ID, "abcdef")
                        .token(1, OP, "[]")
                        .child(2)
                            .hasChild(3)
                            .token(ID, "c")
                            .token(1, OP, "[]")
                            .child(2)
                                .hasChild(1)
                                .token(NUM, "3")
                                .parent()
                            .parent()
                        .token(OP, "=")
                        .token(ID, "c")
                        .token(OP, "--")
                        .token(OP, "*")
                        .token(NUM, "2")
                        .parent()
                    .child(1)
                        .hasChild(2)
                        .token(OP, "++")
                        .token(ID, "c")
                        .parent()
                    .child(2)
                        .hasChild(5)
                        .token(ID, "d5")
                        .token(OP, "=")
                        .child(2)
                            .hasChild(3)
                            .child(0)
                                .hasChild(4)
                                .token(OP, "--")
                                .token(ID, "_$c1_")
                                .token(OP, "+")
                                .token(NUM, "4.5")
                                .parent()
                            .token(1, OP, "/")
                            .token(ID, "cc_123$1")
                            .parent()
                        .token(3, OP, "+")
                        .token(NUM, "4")
            ;
    }
    
    @Test
    public void testU1() {
        assertScriptException(new ScriptAssertion("if(a+b>3){a++;}"));
    }
    
    @Test
    public void testU2() {
        //BitwiseOperator - checkEnoughOperands
        assertScriptException(new ScriptAssertion("if(a+b>3){a++;}^^^^"), Operator.class, 3);
    }
    
    @Test
    public void testU3() {
        //BitwiseOperator - checkEnoughOperands
        assertScriptException(new ScriptAssertion("if(a+b>3){a++;}````"), Script.class, 21);
    }
    
    @Test
    public void testU4() {
        Assert.assertEquals(TokenType.Temporary.RParentheses.instance.toString(), ")");
        Assert.assertEquals(TokenType.Temporary.LParentheses.instance.toString(), "(");
        Assert.assertEquals(TokenType.Temporary.RBracket.instance.toString(), "]");
    }
    
    //test cases for all types of operators
    @Test
    public void testV() {
        new ScriptAssertion("a=b+3")
            .tokenize()
            .reorder()
            .child(0)
                .hasChild(1)
                .child(0)
                    .hasChild(3)
                    .token(ID, "a")
                    .child()
                        .hasChild(3)
                        .token(ID, "b")
                        .token(NUM, "3")
                        .token(OP, "+")
                        .parent()
                    .token(OP, "=")
            ;
        new ScriptAssertion("a123=_b$abc+3-$ccc")
            .tokenize()
            .reorder()
            .child(0)
                .hasChild(1)
                .child(0)
                    .hasChild(3)
                    .token(ID, "a123")
                    .child()
                        .hasChild(3)
                        .child()
                            .token(ID, "_b$abc")
                            .token(NUM, "3")
                            .token(OP, "+")
                            .parent()
                        .token(ID, "$ccc")
                        .token(OP, "-")
                        .parent()
                    .token(OP, "=")
            ;
        new ScriptAssertion("a123=_b$abc+3-$ccc*7")
        .tokenize()
        .reorder()
        .child(0)
            .hasChild(1)
            .child(0)
                .hasChild(3)
                .token(ID, "a123")
                .child()
                    .hasChild(3)
                    .child()
                        .token(ID, "_b$abc")
                        .token(NUM, "3")
                        .token(OP, "+")
                        .parent()
                    .child()
                        .token(ID, "$ccc")
                        .token(NUM, "7")
                        .token(OP, "*")
                        .parent()
                    .token(OP, "-")
                    .parent()
                .token(OP, "=")
                ;
    }
    
    @Test
    public void testV1() {
        new ScriptAssertion("a123=(_b$abc + 7.12)+3/(5-$ccc*7)")
        .tokenize()
        .reorder()
        .child(0)
            .hasChild(1)
            .child(0)
                .hasChild(3)
                .token(ID, "a123")
                .child()
                    .hasChild(3)
                    .child()
                        .child()
                            .hasChild(3)
                            .token(ID, "_b$abc")
                            .token(NUM, "7.12")
                            .token(OP, "+")
                            .parent()
                        .parent()
                    .child()
                        .token(NUM, "3")
                        .child()
                            .hasChild(1)
                            .child()
                                .token(NUM, "5")
                                .child()
                                    .hasChild(3)
                                    .token(ID, "$ccc")
                                    .token(NUM, "7")
                                    .token(OP, "*")
                                    .parent()
                                .token(OP, "-")
                                .parent()
                            .parent()
                        .token(OP, "/")
                        .parent()
                    .token(OP, "+")
                    .parent()
                .token(OP, "=")
                ;
    }
    
    @Test
    public void testV2() {
        new ScriptAssertion("if( (a=b++*3) ){Var2 = (++_b$abc -- + 12.303) / 4 - a;"
                + "Var3 = Var2++ * 2; Var2 = --Var3 + 1.523;}")
        .tokenize()
        .reorder()
        .child(0)
            .hasChild(3)
            .child()
                .hasChild(1)
                .child()
                    .hasChild(1)
                    .child()
                        .token(ID, "a")
                        .child()
                            .hasChild(3)
                            .child()
                                .hasChild(2)
                                .token(ID, "b")
                                .token(OP, "++")
                                .parent()
                            .token(NUM, "3")
                            .token(OP, "*")
                            .parent()
                        .token(OP, "=")
                        .parent()
                    .parent()
                .parent()
            .child()
                .hasChild(3)
                .child()
                    .child()
                        //Var2 = (++_b$abc -- + 12.303) / 4 - a;
                        .hasChild(3)
                        .token(ID, "Var2")
                        .child()
                            .hasChild(3)
                            .child()
                                .hasChild(3)
                                .child()
                                    .hasChild(1)
                                    .child()
                                        .hasChild(3)
                                        .child()
                                            .hasChild(2)
                                            .child()
                                                .token(ID, "_b$abc")
                                                .token(OP, "--")
                                                .parent()
                                            .token(OP, "++")
                                            .parent()
                                        .token(NUM, "12.303")
                                        .token(OP, "+")
                                        .parent()
                                    .parent()
                                .token(NUM, "4")
                                .token(OP, "/")
                                .parent()
                            .token(ID, "a")
                            .token(OP, "-")
                            .parent()
                        .token(OP, "=")
                        .parent()
                    .parent()
                .child()
                //Var3 = Var2++ * 2
                    .child()
                        .hasChild(3)
                        .token(ID, "Var3")
                        .child()
                            .hasChild(3)
                            .child()
                                .hasChild(2)
                                .token(ID, "Var2")
                                .token(OP, "++")
                                .parent()
                            .token(NUM, "2")
                            .token(OP, "*")
                            .parent()
                        .token(OP, "=")
                        .parent()
                    .parent()
                .child()
                //Var2 = --Var3 + 1.523;
                    .child()
                        .hasChild(3)
                        .token(ID, "Var2")
                        .child()
                            .hasChild(3)
                            .child()
                                .token(ID, "Var3")
                                .token(OP, "--")
                                .parent()
                            .token(NUM, "1.523")
                            .token(OP, "+")
                            .parent()
                        .token(OP, "=")
                        .parent()
                    .parent()
                .parent()
            .token(OP, "if")
            .and()
            ;
    }
    
    @Test
    public void testV3() {
        new ScriptAssertion("bc_def++;if(a++)Var2=Var3++;a=--$$$a$$$/4;")
            .tokenize()
            .reorder()
            .hasChild(3)
                .child(0)
                    .child()
                        .hasChild(2)
                        .token(ID, "bc_def")
                        .token(OP, "++")
                        .parent()
                    .and()
                .child(1)
                    .hasChild(3)
                    .child()
                        .child()
                            .hasChild(2)
                            .token(ID, "a")
                            .token(OP, "++")
                            .parent()
                        .parent()
                    .child()
                        .hasChild(1)
                        .child()
                            .token(ID, "Var2")
                            .child()
                                .hasChild(2)
                                .token(ID, "Var3")
                                .token(OP, "++")
                                .parent()
                            .token(OP, "=")
                            .parent()
                        .parent()
                    .token(OP, "if")
                    .and()
                .child(2)
                    .hasChild(1)
                    .child()
                        .token(ID, "a")
                        .child()
                            .hasChild(3)
                            .child()
                                .hasChild(2)
                                .token(ID, "$$$a$$$")
                                .token(OP, "--")
                                .parent()
                            .token(NUM, "4")
                            .token(OP, "/")
                            .parent()
                        .token(OP, "=")
                        .parent()
                    .and()
            ;
    }
    
    @Test
    public void testV4() {
        new ScriptAssertion("if(a++)Var2=Var3++; else if(b) a=a+1; else ++a;b++")
                .tokenize()
                .reorder()
                .hasChild(2)
                .child(0)
                    .hasChild(5)
                    .child()
                        .child()
                            .token(ID, "a")
                            .token(OP, "++")
                            .parent()
                        .parent()
                    .child()
                        .child()
                            .token(ID, "Var2")
                            .child()
                                .token(ID, "Var3")
                                .token(OP, "++")
                                .parent()
                             .token(OP, "=")
                            .parent()
                        .parent()
                    .token(OP, "if")
                    .child()
                        .hasChild(5)
                        .child()
                            .token(ID, "b")
                            .parent()
                        .child()
                            .child()
                                .token(ID, "a")
                                .child()
                                    .token(ID, "a")
                                    .token(NUM, "1")
                                    .token(OP, "+")
                                    .parent()
                                 .token(OP, "=")
                                .parent()
                             .parent()
                        .token(OP, "if")
                        .child()
                            .child()
                                .token(ID, "a")
                                .token(OP, "++")
                                .parent()
                            .parent()
                        .token(OP, "else")
                        .parent()
                    .token(OP, "else")
                    .and()
                .child(1)
                    .child()
                        .token(ID, "b")
                        .token(OP, "++")
                        .parent()
                    .and()
                ;
    }
    
    @Test
    public void testV5() {
        new ScriptAssertion("if(a++){Var2++;} else if(b){a=a+1;} else{++a;}")
            .compile()
            .print();
    }
    
    @Test
    public void testW() {
        new ScriptAssertion("bc_def++;if(a++)Var2=Var3++;a=--$$$a$$$/4;")
                .tokenize()
                .reorder()
                .flatten()
                .hasChild(3)
                .child(0)
                    .hasChild(2)
                    .token(ID, "bc_def")
                    .token(OP, "++")
                    .and()
                .child(1)
                    .hasChild(3)
                    .child()
                        .hasChild(2)
                        .token(ID, "a")
                        .token(OP, "++")
                        .parent()
                    .child()
                        .hasChild(4)
                        .token(ID, "Var2")
                        .token(ID, "Var3")
                        .token(OP, "++")
                        .token(OP, "=")
                        .parent()
                    .token(OP, "if")
                    .and()
                .child(2)
                    .hasChild(6)
                    .token(ID, "a")
                    .token(ID, "$$$a$$$")
                    .token(OP, "--")
                    .token(NUM, "4")
                    .token(OP, "/")
                    .token(OP, "=")
                    .and()
            ;
    }
    
    @Test
    public void testW2() {
        new ScriptAssertion("if( (a=b++*3) ){Var2 = (++_b$abc -- + 12.303) / 4 - a;"
                + "Var3 = Var2++ * 2; Var2 = --Var3 + 1.523;}")
            .tokenize()
            .reorder()
            .flatten()
            .hasChild(1)
            .child(0)
                .child()
                    .hasChild(6)
                    .token(ID, "a")
                    .token(ID, "b")
                    .token(OP, "++")
                    .token(NUM, "3")
                    .token(OP, "*")
                    .token(OP, "=")
                    .parent()
                .child()
                    .hasChild(3)
                    .child()
                        .hasChild(11)
                        .token(ID, "Var2")
                        .token(ID, "_b$abc")
                        .token(OP, "--")
                        .token(OP, "++")
                        .token(NUM, "12.303")
                        .token(OP, "+")
                        .token(NUM, "4")
                        .token(OP, "/")
                        .token(ID, "a")
                        .token(OP, "-")
                        .token(OP, "=")
                        .parent()
                    .child()
                        //Var3 = Var2++ * 2
                        .hasChild(6)
                        .token(ID, "Var3")
                        .token(ID, "Var2")
                        .token(OP, "++")
                        .token(NUM, "2")
                        .token(OP, "*")
                        .token(OP, "=")
                        .parent()
                    .child()
                        //Var2 = --Var3 + 1.523
                        .hasChild(6)
                        .token(ID, "Var2")
                        .token(ID, "Var3")
                        .token(OP, "--")
                        .token(NUM, "1.523")
                        .token(OP, "+")
                        .token(OP, "=")
                        .parent()
                    .parent()
                .token(OP, "if")
            ;
    }
    
    @Test
    public void testW3() {
        new ScriptAssertion("if( (a=b*3) + (((b-a*4))) ){{{{{a=a++/4;}}}}"
                + "Var3 = Var2++ * (((((b-a))))); {{{   }}} }")
                .tokenize()
                .reorder()
                .flatten()
                .hasChild(1)
                .child(0)
                    .hasChild(3)
                    .child()
                        .hasChild(11)
                        .token(ID, "a")
                        .token(ID, "b")
                        .token(NUM, "3")
                        .token(OP, "*")
                        .token(OP, "=")
                        .token(ID, "b")
                        .token(ID, "a")
                        .token(NUM, "4")
                        .token(OP, "*")
                        .token(OP, "-")
                        .token(OP, "+")
                        .parent()
                    .child()
                        .hasChild(3)
                        .child()
                            .hasChild(6)
                            .token(ID, "a")
                            .token(ID, "a")
                            .token(OP, "++")
                            .token(NUM, "4")
                            .token(OP, "/")
                            .token(OP, "=")
                            .parent()
                        .child()
                            //Var3 = Var2++ * (((((b-a)))));
                            .hasChild(8)
                            .token(ID, "Var3")
                            .token(ID, "Var2")
                            .token(OP, "++")
                            .token(ID, "b")
                            .token(ID, "a")
                            .token(OP, "-")
                            .token(OP, "*")
                            .token(OP, "=")
                            .parent()
                        .child()
                            .hasChild(0)
                            .parent()
                        .parent()
                    .token(OP, "if")
                    .and()
            ;
    }
    
    @Test
    public void testW4() {
        new ScriptAssertion("if(a=b+3){ if(b=3+4) ++a; else a=b/3; } else a=b++*3; ")
                .tokenize()
                .reorder()
                .flatten()
                .hasChild(1)
                .child(0)
                    .hasChild(5)
                    .child()
                        .hasChild(5)
                        .token(ID, "a")
                        .token(ID, "b")
                        .token(NUM, "3")
                        .token(OP, "+")
                        .token(OP, "=")
                        .parent()
                    .child()
                        .hasChild(5)
                        .child()
                            .hasChild(3)
                            .token(ID, "b")
                            .token(NUM, "7")
                            .token(OP, "=")
                            .parent()
                        .child()
                            .hasChild(2)
                            .token(ID, "a")
                            .token(OP, "++")
                            .parent()
                        .token(OP, "if")
                        .child()
                            .hasChild(5)
                            .token(ID, "a")
                            .token(ID, "b")
                            .token(NUM, "3")
                            .token(OP, "/")
                            .token(OP, "=")
                            .parent()
                        .token(OP, "else")
                        .parent()
                    .token(OP, "if")
                    .child()
                        .hasChild(6)
                        .token(ID, "a")
                        .token(ID, "b")
                        .token(OP, "++")
                        .token(NUM, "3")
                        .token(OP, "*")
                        .token(OP, "=")
                        .parent()
                    .token(OP, "else")
                    .and()
            ;
    }
    
    @Test
    public void testW5() {
        new ScriptAssertion("if(a=b){ if(b=3) ++a; else; } else {}; ")
                .tokenize()
                .reorder()
                .flatten()
                .hasChild(1)
                .child(0)
                    .hasChild(5)
                    .child()
                        .hasChild(3)
                        .token(ID, "a")
                        .token(ID, "b")
                        .token(OP, "=")
                        .parent()
                    .child()
                        .child()
                            .hasChild(3)
                            .token(ID, "b")
                            .token(NUM, "3")
                            .token(OP, "=")
                            .parent()
                        .child()
                            .hasChild(2)
                            .token(ID, "a")
                            .token(OP, "++")
                            .parent()
                        .token(OP, "if")
                        .child()
                            .isEmpty()
                            .parent()
                        .token(OP, "else")
                        .parent()
                    .token(OP, "if")
                    .child()
                        .isEmpty()
                        .parent()
                    .token(OP, "else")
                ;
    }
    
    @Test
    public void testW6() {
        new ScriptAssertion("abc = a++ + (a=='3?' ? b == '4?' ? (a+b+c++)>5 ? a+1 : 'b?+?1?:' : 'c:?+1'+'abc' : ++d);c = a?b?c:d:eeee; ")
                .tokenize()
                .reorder()
                .flatten()
                .hasChild(2)
                .child(0)
                    //(a+b+c++) is unwrapped (flattened)
                    //independent statement before first ? and after last :
                    //all other tokens between them forms another statement
                    .hasChild(9)
                    .token(ID, "abc")
                    .token(ID, "a")
                    .token(OP, "++")
                    .child()
                        .token(ID, "a")
                        .token(STR, "3?")
                        .token(OP, "==")
                        .parent()
                    .child()
                        .child()
                            .token(ID, "b")
                            .token(STR, "4?")
                            .token(OP, "==")
                            .parent()
                        .child()
                            .child()
                                .hasChild(8)
                                .token(ID, "a")
                                .token(ID, "b")
                                .token(OP, "+")
                                .token(ID, "c")
                                .token(OP, "++")
                                .token(OP, "+")
                                .token(NUM, "5")
                                .token(OP, ">")
                                .parent()
                            .child()
                                .token(ID, "a")
                                .token(NUM, "1")
                                .token(OP, "+")
                                .parent()
                            .child()
                                .token(STR, "b?+?1?:")
                                .parent()
                            .token(OP, "?")
                            .parent()
                        .child()
                            .token(STR, "c:?+1abc")
                            .parent()
                        .token(OP, "?")
                        .parent()
                    .child()
                        .token(ID, "d")
                        .token(OP, "++")
                        .parent()
                    .token(OP, "?")
                    .and()
                .child(1)
                    //c = a?b?c:d:eeee
                    .hasChild(6)
                    .token(ID, "c")
                    .child()
                        .token(ID, "a")
                        .parent()
                    .child()
                        .child()
                            .token(ID, "b")
                            .parent()
                        .child()
                            .token(ID, "c")
                            .parent()
                        .child()
                            .token(ID, "d")
                            .parent()
                        .token(OP, "?")
                        .parent()
                    .child()
                        .token(ID, "eeee")
                        .parent()
                    .token(OP, "?")
                    .and()
            ;
    }
    
    @Test
    public void testW7() {
        new ScriptAssertion("abc = a=='3?' ? b_b+3 : 3*ccc+'abcdef234234'")
                
                .tokenize()
                
                .reorder()
                .flatten()
                .hasChild(1)
                .child(0)
                    .hasChild(6)
                    .token(ID, "abc")
                    .child()
                        .hasChild(3)
                        .token(ID, "a")
                        .token(STR, "3?")
                        .token(OP, "==")
                        .parent()
                    .child()
                        .hasChild(3)
                        .token(ID, "b_b")
                        .token(NUM, "3")
                        .token(OP, "+")
                        .parent()
                    .child()
                        .hasChild(5)
                        .token(NUM, "3")
                        .token(ID, "ccc")
                        .token(OP, "*")
                        .token(STR, "abcdef234234")
                        .token(OP, "+")
                        .parent()
                    .token(OP, "?")
                    .token(OP, "=")
                .and()
            ;
    }
    
    //Dot operator with nested empty round statements
    @Test
    public void testW8() {
        new ScriptAssertion("((((a.b.c)))).d.c")
            .compile()
            .hasChild(1)
            .child(0)
                .hasChild(1)
                .token(ID, "a.b.c.d.c")
                .and()
             ;
        new ScriptAssertion("((((a)))).d.c")
        .compile()
        .hasChild(1)
        .child(0)
            .hasChild(1)
            .token(ID, "a.d.c")
            .and()
         ;
        new ScriptAssertion("((((a.b)))).c")
        .compile()
        .hasChild(1)
        .child(0)
            .hasChild(1)
            .token(ID, "a.b.c")
            .and()
         ;
        new ScriptAssertion("((((a.b).d))).c")
        .compile()
        .hasChild(1)
        .child(0)
            .hasChild(1)
            .token(ID, "a.b.d.c")
            .and()
         ;
    }
    
    // ---------------------- Exceptions ----------------------
    
    // parentheses statement exceptions
    @Test
    public void testX() {
        try {
            new ScriptAssertion("a = (a+3  ;  )")
                    
                    .tokenize()
                    
                    .reorder()
                    .flatten()
                    ;
            Assert.fail();
        } catch (Exception e) {
            testException(e, Script.class, 24);
        }
        try {
            new ScriptAssertion("a = (a+3  ; b++  )")
                    .tokenize()
                    .reorder()
                    .flatten()
                    ;
            Assert.fail();
        } catch (Exception e) {
            testException(e, Script.class, 24);
        }
        try {
            new ScriptAssertion("a = ( {a++;} )")
                    .tokenize()
                    .reorder()
                    .flatten()
                    ;
            Assert.fail();
        } catch (Exception e) {
            testException(e, Script.class, 24);
        }
        try {
            new ScriptAssertion("a = ( {{{{}}}} )")
                    .tokenize()
                    .reorder()
                    .flatten()
                    ;
            Assert.fail();
        } catch (Exception e) {
            testException(e, Script.class, 24);
        }
        
        try {
            new ScriptAssertion("a = ( if(a>3)++a; )")
                    .tokenize()
                    .reorder()
                    .flatten()
                    ;
            Assert.fail();
        } catch (Exception e) {
            testException(e, Script.class, 24);
        }
        
        try {
            new ScriptAssertion("a = ( if(a>3)++a )")
                    .tokenize()
                    .reorder()
                    .flatten()
                    ;
            Assert.fail();
        } catch (Exception e) {
            testException(e, Script.class, 24);
        }
        
        try {
            new ScriptAssertion("a = ( else ++a )")
                    .tokenize()
                    .reorder()
                    .flatten()
                    ;
            Assert.fail();
        } catch (Exception e) {
            testException(e, Script.class, 24);
        }
    }
    
    // brace statement exceptions
    @Test
    public void testY() {
        try {
            new ScriptAssertion("'if'+'else'{abc++;}")
                    .tokenize()
                    .reorder()
                    .flatten()
                    ;
            Assert.fail();
        } catch (Exception e) {
            testException(e, Script.class, 23);
        }
        new ScriptAssertion("{a++;b++;} a[3]++;        {abc++;}")
                
                .tokenize()
                
                .reorder()
                .flatten()
                ;
        try {
            new ScriptAssertion("{a++;b++}")
            .tokenize()
            .reorder()
            .flatten()
            ;
        } catch (Exception e) {
            testException(e, Script.class, 23);
        }
        new ScriptAssertion(";{;;;;;};")
        
        .tokenize()
        
        .reorder()
        .flatten()
        ;
    }
    
    // if else structure exceptions
    @Test
    public void testZ() {
        try {
            new ScriptAssertion("if a {a++;}")
                    
                    .tokenize()
                    
                    .reorder()
                    .flatten()
                    ;
            Assert.fail();
        } catch (Exception e) {
            testException(e, Script.class, 27);
        }
        
        try {
            new ScriptAssertion("if {a;} {a++;}")
                    .tokenize()
                    .reorder()
                    .flatten()
                    ;
            Assert.fail();
        } catch (Exception e) {
            testException(e, Script.class, 27);
        }
        
        try {
            new ScriptAssertion("if a+b+c; {a++;}")
                    .tokenize()
                    .reorder()
                    .flatten()
                    ;
            Assert.fail();
        } catch (Exception e) {
            testException(e, Script.class, 27);
        }
        
        //else
        try {
            new ScriptAssertion("if(a){a++;} else abc {c++;}")
                    .tokenize()
                    .reorder()
                    .flatten()
                    ;
            Assert.fail();
        } catch (Exception e) {
            testException(e, Script.class, 23);
        }
        
        new ScriptAssertion("if  (a)  {a++;} else abc")
                
                .tokenize()
                
                .reorder()
                .flatten()
                ;
        new ScriptAssertion("if  (a)  {a++;} else if(a)abc")
            
            .tokenize()
            
            .reorder()
            .flatten()
            ;
        new ScriptAssertion("if  (a)  {a++;} else   if   (a)abc")
            
            .tokenize()
            
            .reorder()
            .flatten()
            ;
        
        //this syntax fails for Java compiler but compiles for C and JS compiler
        new ScriptAssertion("if(a){a++;} else (a=a+1)")
                
                .tokenize()
                
                .reorder()
                .flatten()
                ;
        try {
            new ScriptAssertion("if(a){a++;} else (a++)(a=a+1);")
                    
                    .tokenize()
                    
                    .reorder()
                    .flatten()
                    .print();
                    ;
            Assert.fail();
        } catch (Exception e) {
            testException(e, Script.class, 6);
        }
        
        try {
            new ScriptAssertion("if(a){a++;} else bbb+{a++;};")
                    .tokenize()
                    .reorder()
                    .flatten()
                    .print();
                    ;
            Assert.fail();
        } catch (Exception e) {
            testException(e, Script.class, 23);
        }
        
        try {
            new ScriptAssertion("if(a){a++;} else (bbb+c)+{a++;};")
                    .tokenize()
                    .reorder()
                    .flatten()
                    .print();
                    ;
            Assert.fail();
        } catch (Exception e) {
            testException(e, Script.class, 23);
        }
        
        try {
            new ScriptAssertion("if(a){a++;} else (bbb+c){a++;};")
                    .tokenize()
                    .reorder()
                    .flatten()
                    .print();
                    ;
            Assert.fail();
        } catch (Exception e) {
            testException(e, Script.class, 23);
        }
        
    }
    
    // bracket statements exceptions
    @Test
    public void testAA() {
        assertScriptException(new ScriptAssertion("a[()]"),Script.class,28);
        assertScriptException(new ScriptAssertion("{a+b+c;}[3]"),Script.class,22);
        assertScriptException(new ScriptAssertion("a+b;[3]"),Script.class,22);
        assertScriptException(new ScriptAssertion("(a[3])[3]"));
        assertScriptException(new ScriptAssertion("(a[3])[]"),Script.class,26);
        assertScriptException(new ScriptAssertion("(a[3])[   ]"),Script.class,26);
        assertScriptException(new ScriptAssertion("a[3]"));
        assertScriptException(new ScriptAssertion("a[]"),Script.class,26);
        assertScriptException(new ScriptAssertion("a[   ]"),Script.class,26);
        assertScriptException(new ScriptAssertion("3[3]"),AffixBinaryOperator.class,1);
        assertScriptException(new ScriptAssertion("true[3]"),AffixBinaryOperator.class,1);
        assertScriptException(new ScriptAssertion("false[3]"),AffixBinaryOperator.class,1);
        assertScriptException(new ScriptAssertion("(a+b+c)[3]"));
        assertScriptException(new ScriptAssertion("a[3]"));
        assertScriptException(new ScriptAssertion("a[b]"));
        assertScriptException(new ScriptAssertion("a['bsdfsdfsfd_&#@&$*#$']"));
        assertScriptException(new ScriptAssertion("a[(a+b+c)[3]*3/5]"));
        assertScriptException(new ScriptAssertion("a[a+3;]"),Script.class,25);
        assertScriptException(new ScriptAssertion("a[{(a++)}]"),Script.class,25);
        assertScriptException(new ScriptAssertion("a[if(a>3)abc]"),Script.class,25);
    }
    
    // . dot operator exceptions
    @Test
    public void testAB() {
        assertScriptException(new ScriptAssertion("a_efg.bcd_e"));
        assertScriptException(new ScriptAssertion("(a+b+c).bedf$$"));
        assertScriptException(new ScriptAssertion("a[3].beee"));
        assertScriptException(new ScriptAssertion("((((a+b*c)))).bccc"));
        
        assertScriptException(new ScriptAssertion("a.'abc'"),AffixBinaryOperator.class,2);
        assertScriptException(new ScriptAssertion("a.3.12345"),AffixBinaryOperator.class,2);
        assertScriptException(new ScriptAssertion("a.3"),AffixBinaryOperator.class,2);
        assertScriptException(new ScriptAssertion("a.(a+b+c)"),AffixBinaryOperator.class,2);
        assertScriptException(new ScriptAssertion("a.false"),AffixBinaryOperator.class,2);
        assertScriptException(new ScriptAssertion("a.true"),AffixBinaryOperator.class,2);
        assertScriptException(new ScriptAssertion("a.null"),AffixBinaryOperator.class,2);
        
        assertScriptException(new ScriptAssertion("{a+b+c;}.b"),Operator.class, 3);
        assertScriptException(new ScriptAssertion("if(a){a+b;}.b"),Operator.class, 3);
        assertScriptException(new ScriptAssertion("3.12345.b"),AffixBinaryOperator.class,1);
        assertScriptException(new ScriptAssertion("true.b"),AffixBinaryOperator.class,1);
        assertScriptException(new ScriptAssertion("false.b"),AffixBinaryOperator.class,1);
        assertScriptException(new ScriptAssertion("null.b"),AffixBinaryOperator.class,1);
    }
    
    // ++ post/prefix increment
    @Test
    public void testAC() {
        //Post Increment SIncOperator
        assertScriptException(new ScriptAssertion("(a+b+c)++"));
        assertScriptException(new ScriptAssertion("( ( (a+b+c) ) )++"));
        assertScriptException(new ScriptAssertion("a[3]++"));
        
        //SInc failed and PIncOperator throws an error
        assertScriptException(new ScriptAssertion("3++"), Script.class, 9);
        assertScriptException(new ScriptAssertion("4.123123++"), Script.class, 9);
        assertScriptException(new ScriptAssertion("true++"), Script.class, 9);
        assertScriptException(new ScriptAssertion("false++"), Script.class, 9);
        assertScriptException(new ScriptAssertion("null++"), Script.class, 9);
        assertScriptException(new ScriptAssertion("'asdfsafd_sdf'++"), Script.class, 9);
        
        //Pre Increment SIncOperator
        assertScriptException(new ScriptAssertion("++(a+b+c)"));
        assertScriptException(new ScriptAssertion("++( ( (a+b+c) ) )"));
        assertScriptException(new ScriptAssertion("++a[3]"));
        
        assertScriptException(new ScriptAssertion("++3"), PrefixOperator.class, 1);
        assertScriptException(new ScriptAssertion("++4.123123"), PrefixOperator.class, 1);
        assertScriptException(new ScriptAssertion("++true"), PrefixOperator.class, 1);
        assertScriptException(new ScriptAssertion("++false"), PrefixOperator.class, 1);
        assertScriptException(new ScriptAssertion("++null"), PrefixOperator.class, 1);
        assertScriptException(new ScriptAssertion("++'asdfsafd_sdf'"), PrefixOperator.class, 1);
        
    }
    
    // -- post/prefix increment
    @Test
    public void testAD() {
        //Post Increment SIncOperator
        assertScriptException(new ScriptAssertion("(a+b+c)--"));
        assertScriptException(new ScriptAssertion("( ( (a+b+c) ) )--"));
        assertScriptException(new ScriptAssertion("a[3]--"));
        
        assertScriptException(new ScriptAssertion("3--"), Script.class, 9);
        assertScriptException(new ScriptAssertion("4.123123--"), Script.class, 9);
        assertScriptException(new ScriptAssertion("true--"), Script.class, 9);
        assertScriptException(new ScriptAssertion("false--"), Script.class, 9);
        assertScriptException(new ScriptAssertion("null--"), Script.class, 9);
        assertScriptException(new ScriptAssertion("'asdfsafd_sdf'--"), Script.class, 9);
        
        assertScriptException(new ScriptAssertion("--(a+b+c)"));
        assertScriptException(new ScriptAssertion("--( ( (a+b+c) ) )"));
        assertScriptException(new ScriptAssertion("--a[3]"));
        
        assertScriptException(new ScriptAssertion("--3"), PrefixOperator.class, 1);
        assertScriptException(new ScriptAssertion("--4.123123"), PrefixOperator.class, 1);
        assertScriptException(new ScriptAssertion("--true"), PrefixOperator.class, 1);
        assertScriptException(new ScriptAssertion("--false"), PrefixOperator.class, 1);
        assertScriptException(new ScriptAssertion("--null"), PrefixOperator.class, 1);
        assertScriptException(new ScriptAssertion("--'asdfsafd_sdf'"), PrefixOperator.class, 1);
    }
    
    // unary plus +, additive +
    // integration test with prefix/suffix increment
    @Test
    public void testAE() {
        
        assertScriptException(new ScriptAssertion("+3"));
        assertScriptException(new ScriptAssertion("+aasdfasfd"));
        assertScriptException(new ScriptAssertion("+(a+b+c)"));
        assertScriptException(new ScriptAssertion("+a[3]"));
        //positive failed and turned to plus
        assertScriptException(new ScriptAssertion("+true"),Operator.class, 3);
        assertScriptException(new ScriptAssertion("+false"),Operator.class, 3);
        assertScriptException(new ScriptAssertion("+null"),Operator.class, 3);
        assertScriptException(new ScriptAssertion("+'abcdef'"),Operator.class, 3);
        assertScriptException(new ScriptAssertion("+{a++;}"),Script.class,23);
        
        assertScriptException(new ScriptAssertion("3+3"));
        assertScriptException(new ScriptAssertion("3+'asdfsdf'"));
        assertScriptException(new ScriptAssertion("3+asadfsfd_sdfsfd"));
        assertScriptException(new ScriptAssertion("3+(a+b+c)"));
        assertScriptException(new ScriptAssertion("3+a[3]"));
        
        assertScriptException(new ScriptAssertion("'asdfsdf'+3"));
        assertScriptException(new ScriptAssertion("'asdfsdf'+'asdfsdf'"));
        assertScriptException(new ScriptAssertion("'asdfsdf'+asadfsfd_sdfsfd"));
        assertScriptException(new ScriptAssertion("'asdfsdf'+(a+b+c)"));
        assertScriptException(new ScriptAssertion("'asdfsdf'+a[3]"));
        
        assertScriptException(new ScriptAssertion("(a+b+c)+3"));
        assertScriptException(new ScriptAssertion("(a+b+c)+'asdfsdf'"));
        assertScriptException(new ScriptAssertion("(a+b+c)+asadfsfd_sdfsfd"));
        assertScriptException(new ScriptAssertion("(a+b+c)+(a+b+c)"));
        assertScriptException(new ScriptAssertion("(a+b+c)+a[3]"));
        
        assertScriptException(new ScriptAssertion("a[3]+3"));
        assertScriptException(new ScriptAssertion("a[3]+'asdfsdf'"));
        assertScriptException(new ScriptAssertion("a[3]+asadfsfd_sdfsfd"));
        assertScriptException(new ScriptAssertion("a[3]+(a+b+c)"));
        assertScriptException(new ScriptAssertion("a[3]+a[3]"));
        
        assertScriptException(new ScriptAssertion("asadfsfd_sdfsfd+3"));
        assertScriptException(new ScriptAssertion("asadfsfd_sdfsfd+'asdfsdf'"));
        assertScriptException(new ScriptAssertion("asadfsfd_sdfsfd+asadfsfd_sdfsfd"));
        assertScriptException(new ScriptAssertion("asadfsfd_sdfsfd+(a+b+c)"));
        assertScriptException(new ScriptAssertion("asadfsfd_sdfsfd+a[3]"));
        
        assertScriptException(new ScriptAssertion("true+3"), AffixBinaryOperator.class, 1);
        assertScriptException(new ScriptAssertion("false+3"), AffixBinaryOperator.class, 1);
        assertScriptException(new ScriptAssertion("null+3"), AffixBinaryOperator.class, 1);
        
        assertScriptException(new ScriptAssertion("3.234234+true"), AffixBinaryOperator.class, 2);
        assertScriptException(new ScriptAssertion("3.234234+false"), AffixBinaryOperator.class, 2);
        assertScriptException(new ScriptAssertion("3.234234+null"), AffixBinaryOperator.class, 2);
        
        assertScriptException(new ScriptAssertion("a++++3"), Script.class, 6);
        
        new ScriptAssertion("a+++3")
            .compile()
            .child(0)
            .hasChild(4)
            .token(ID, "a")
            .token(OP, "++")
            .token(NUM, "3")
            .token(OP, "+")
            ;
        
        assertScriptException(new ScriptAssertion("3+++a"), PrefixOperator.class, 2);
        new ScriptAssertion("3+ ++a")
            .compile()
            .child(0)
            .hasChild(4)
            .token(NUM, "3")
            .token(ID, "a")
            .token(OP, "++")
            .token(OP, "+")
            ;
        
        assertScriptException(new ScriptAssertion("+3++a"), PrefixOperator.class, 2);
        new ScriptAssertion("+3+ +a")
            .compile()
            .child(0)
            .hasChild(4)
            .token(NUM, "3")
            .token(ID, "a")
            .token(OP, "+")
            .token(OP, "+")
            ;
        
        assertScriptException(new ScriptAssertion("+3++a++"), PrefixOperator.class, 2);
        new ScriptAssertion("+3+ +a++")
            .compile()
            .child(0)
            .hasChild(5)
            .token(NUM, "3")
            .token(ID, "a")
            .token(OP, "++")
            .token(OP, "+")
            .token(OP, "+")
            ;
        
        assertScriptException(new ScriptAssertion("+3+++a"), PrefixOperator.class, 2);
        new ScriptAssertion("+3+ ++a")
            .compile()
            .child(0)
            .hasChild(4)
            .token(NUM, "3")
            .token(ID, "a")
            .token(OP, "++")
            .token(OP, "+")
            ;
        new ScriptAssertion("+b+++a")
            .compile()
            .child(0)
            .hasChild(5)
            .token(ID, "b")
            .token(OP, "++")
            .token(OP, "+")
            .token(ID, "a")
            .token(OP, "+")
            ;
        
        assertScriptException(new ScriptAssertion("a++++3"), Script.class, 6);
    }
    
    // unary plus -, additive -
    // integeration test with prefix/suffix --
    @Test
    public void testAF() {
        
        assertScriptException(new ScriptAssertion("-3"));
        assertScriptException(new ScriptAssertion("-aasdfasfd"));
        assertScriptException(new ScriptAssertion("-(a-b-c)"));
        assertScriptException(new ScriptAssertion("-a[3]"));
        
        assertScriptException(new ScriptAssertion("-true"),Operator.class, 3);
        assertScriptException(new ScriptAssertion("-false"),Operator.class, 3);
        assertScriptException(new ScriptAssertion("-null"),Operator.class, 3);
        assertScriptException(new ScriptAssertion("-'abcdef'"),Operator.class, 3);
        assertScriptException(new ScriptAssertion("-{a--;}"),Script.class,23);
        
        assertScriptException(new ScriptAssertion("3-3"));
        assertScriptException(new ScriptAssertion("3-'asdfsdf'"),AffixBinaryOperator.class,2);
        assertScriptException(new ScriptAssertion("3-asadfsfd_sdfsfd"));
        assertScriptException(new ScriptAssertion("3-(a-b-c)"));
        assertScriptException(new ScriptAssertion("3-a[3]"));
        
        assertScriptException(new ScriptAssertion("'asdfsdf'-3"),AffixBinaryOperator.class,1);
        
        assertScriptException(new ScriptAssertion("(a-b-c)-3"));
        assertScriptException(new ScriptAssertion("(a-b-c)-'asdfsdf'"),AffixBinaryOperator.class,2);
        assertScriptException(new ScriptAssertion("(a-b-c)-asadfsfd_sdfsfd"));
        assertScriptException(new ScriptAssertion("(a-b-c)-(a-b-c)"));
        assertScriptException(new ScriptAssertion("(a-b-c)-a[3]"));
        
        assertScriptException(new ScriptAssertion("a[3]-3"));
        assertScriptException(new ScriptAssertion("a[3]-'asdfsdf'"),AffixBinaryOperator.class,2);
        assertScriptException(new ScriptAssertion("a[3]-asadfsfd_sdfsfd"));
        assertScriptException(new ScriptAssertion("a[3]-(a-b-c)"));
        assertScriptException(new ScriptAssertion("a[3]-a[3]"));
        
        assertScriptException(new ScriptAssertion("asadfsfd_sdfsfd-3"));
        assertScriptException(new ScriptAssertion("asadfsfd_sdfsfd-'asdfsdf'"), AffixBinaryOperator.class, 2);
        assertScriptException(new ScriptAssertion("asadfsfd_sdfsfd-asadfsfd_sdfsfd"));
        assertScriptException(new ScriptAssertion("asadfsfd_sdfsfd-(a-b-c)"));
        assertScriptException(new ScriptAssertion("asadfsfd_sdfsfd-a[3]"));
        
        assertScriptException(new ScriptAssertion("true-3"), AffixBinaryOperator.class, 1);
        assertScriptException(new ScriptAssertion("false-3"), AffixBinaryOperator.class, 1);
        assertScriptException(new ScriptAssertion("null-3"), AffixBinaryOperator.class, 1);
        
        assertScriptException(new ScriptAssertion("3.234234-true"), AffixBinaryOperator.class, 2);
        assertScriptException(new ScriptAssertion("3.234234-false"), AffixBinaryOperator.class, 2);
        assertScriptException(new ScriptAssertion("3.234234-null"), AffixBinaryOperator.class, 2);
        
        assertScriptException(new ScriptAssertion("a----3"), Script.class, 6);
        
        new ScriptAssertion("a---3")
            .compile()
            .child(0)
            .hasChild(4)
            .token(ID, "a")
            .token(OP, "--")
            .token(NUM, "3")
            .token(OP, "-")
            ;
        
        assertScriptException(new ScriptAssertion("3---a"), PrefixOperator.class, 2);
        new ScriptAssertion("3- --a")
            .compile()
            .child(0)
            .hasChild(4)
            .token(NUM, "3")
            .token(ID, "a")
            .token(OP, "--")
            .token(OP, "-")
            ;
        
        assertScriptException(new ScriptAssertion("-3--a"), PrefixOperator.class, 2);
        new ScriptAssertion("-3- -a")
            .compile()
            .child(0)
            .hasChild(4)
            .token(NUM, "-3")
            .token(ID, "a")
            .token(OP, "-")
            .token(OP, "-")
            ;
        
        assertScriptException(new ScriptAssertion("-3--a--"), PrefixOperator.class, 2);
        new ScriptAssertion("-3- -a--")
            .compile()
            .child(0)
            .hasChild(5)
            .token(NUM, "-3")
            .token(ID, "a")
            .token(OP, "--")
            .token(OP, "-")
            .token(OP, "-")
            ;
        
        assertScriptException(new ScriptAssertion("-3---a"), PrefixOperator.class, 2);
        new ScriptAssertion("-3- --a")
            .compile()
            .child(0)
            .hasChild(4)
            .token(NUM, "-3")
            .token(ID, "a")
            .token(OP, "--")
            .token(OP, "-")
            ;
        new ScriptAssertion("-b---a")
            .compile()
            .child(0)
            .hasChild(5)
            .token(ID, "b")
            .token(OP, "--")
            .token(OP, "-")
            .token(ID, "a")
            .token(OP, "-")
            ;
        
        assertScriptException(new ScriptAssertion("a----3"), Script.class, 6);
    }
    
    // logical not
    @Test
    public void testAG() {
        assertScriptException(new ScriptAssertion("!true"));
        assertScriptException(new ScriptAssertion("!false"));
        assertScriptException(new ScriptAssertion("!3.234234"), PrefixOperator.class,1);
        assertScriptException(new ScriptAssertion("!null"), PrefixOperator.class,1);
        assertScriptException(new ScriptAssertion("!abcdef"));
        assertScriptException(new ScriptAssertion("!(a+b+c)"));
        assertScriptException(new ScriptAssertion("!(((a+b+c)))"));
        assertScriptException(new ScriptAssertion("!'adsfasdf'"), PrefixOperator.class,1);
        assertScriptException(new ScriptAssertion("!{a++;}"), Script.class,23);
        assertScriptException(new ScriptAssertion("!(if(a)true)"), Script.class,24);
        assertScriptException(new ScriptAssertion("true!"), Script.class, 9);
    }
    
    // bitwise not
    @Test
    public void testAH() {
        assertScriptException(new ScriptAssertion("~true"), PrefixOperator.class, 1);
        assertScriptException(new ScriptAssertion("~false"), PrefixOperator.class, 1);
        assertScriptException(new ScriptAssertion("~3.234234"));
        assertScriptException(new ScriptAssertion("~null"), PrefixOperator.class,1);
        assertScriptException(new ScriptAssertion("~abcdef"));
        assertScriptException(new ScriptAssertion("~(a+b+c)"));
        assertScriptException(new ScriptAssertion("~(((a+b+c)))"));
        assertScriptException(new ScriptAssertion("~'adsfasdf'"), PrefixOperator.class,1);
        assertScriptException(new ScriptAssertion("~{a++;}"), Script.class,23);
        assertScriptException(new ScriptAssertion("~(if(a)true)"), Script.class,24);
        assertScriptException(new ScriptAssertion("true~"), Script.class, 9);
        assertScriptException(new ScriptAssertion("~a[3]++"));
    }
    
    @Test
    public void testAH1() {
        new ScriptAssertion("a=~+ ++c+~d")
            .compile()
            .child(0)
            .hasChild(9)
            .token(ID, "a")
            .token(ID, "c")
            .token(OP, "++")
            .token(OP, "+")
            .token(OP, "~")
            .token(ID, "d")
            .token(OP, "~")
            .token(OP, "+")
            .token(OP, "=")
            ;
        
        new ScriptAssertion("a=+~c+++~d")
            .compile()
            .child(0)
            .hasChild(9)
            .token(ID, "a")
            .token(ID, "c")
            .token(OP, "++")
            .token(OP, "~")
            .token(OP, "+")
            .token(ID, "d")
            .token(OP, "~")
            .token(OP, "+")
            .token(OP, "=")
            ;
        
        new ScriptAssertion("a=+~++c+~d")
            .compile()
            .child(0)
            .hasChild(9)
            .token(ID, "a")
            .token(ID, "c")
            .token(OP, "++")
            .token(OP, "~")
            .token(OP, "+")
            .token(ID, "d")
            .token(OP, "~")
            .token(OP, "+")
            .token(OP, "=")
            ;
        
        new ScriptAssertion("a=+ ++~c+~d")
            .compile()
            .child(0)
            .hasChild(9)
            .token(ID, "a")
            .token(ID, "c")
            .token(OP, "~")
            .token(OP, "++")
            .token(OP, "+")
            .token(ID, "d")
            .token(OP, "~")
            .token(OP, "+")
            .token(OP, "=")
            ;
        
        new ScriptAssertion("if(~+a){((~+a*~b)-~c+c++ +~d);c=c+1;}else (~b[3]++);")
            .compile()
            .hasChild(1)
            .child(0)
                .hasChild(5)
                .child()
                    .hasChild(3)
                    .token(ID, "a")
                    .token(OP, "+")
                    .token(OP, "~")
                    .parent()
                .child()
                    .hasChild(2)
                    .child()
                        .token(ID, "a")
                        .token(OP, "+")
                        .token(OP, "~")
                        .token(ID, "b")
                        .token(OP, "~")
                        .token(OP, "*")
                        .token(ID, "c")
                        .token(OP, "~")
                        .token(OP, "-")
                        .token(ID, "c")
                        .token(OP, "++")
                        .token(OP, "+")
                        .token(ID, "d")
                        .token(OP, "~")
                        .token(OP, "+")
                        .parent()
                    .child()
                        .token(ID, "c")
                        .token(ID, "c")
                        .token(NUM, "1")
                        .token(OP, "+")
                        .token(OP, "=")
                        .parent()
                    .parent()
                .token(OP, "if")
                .child()
                    .hasChild(5)
                    .token(ID, "b")
                    .token(NUM, "3")
                    .token(OP, "[]")
                    .token(OP, "++")
                    .token(OP, "~")
                    .parent()
                .token(OP, "else")
                .and()
            ;
    }
    
    // multiplicative * / %
    @Test
    public void testAI() {
        assertScriptException(new ScriptAssertion("3*a_sdfsfd"));
        assertScriptException(new ScriptAssertion("3*a_$$[3]"));
        assertScriptException(new ScriptAssertion("3*(a+b+c)"));
        assertScriptException(new ScriptAssertion("3*4.234243"));
        
        assertScriptException(new ScriptAssertion("3.123*true"), AffixBinaryOperator.class, 2);
        assertScriptException(new ScriptAssertion("3.444*false"), AffixBinaryOperator.class, 2);
        assertScriptException(new ScriptAssertion("3*null"), AffixBinaryOperator.class, 2);
        assertScriptException(new ScriptAssertion("3*'asdfasfd'"), AffixBinaryOperator.class, 2);
        
        assertScriptException(new ScriptAssertion("true*3"), AffixBinaryOperator.class, 1);
        assertScriptException(new ScriptAssertion("null*3"), AffixBinaryOperator.class, 1);
        assertScriptException(new ScriptAssertion("'asdfasfd'*3"), AffixBinaryOperator.class, 1);
        
        assertScriptException(new ScriptAssertion("3/a_sdfsfd"));
        assertScriptException(new ScriptAssertion("3/a_$$[3]"));
        assertScriptException(new ScriptAssertion("3/(a+b+c)"));
        assertScriptException(new ScriptAssertion("3/4.234243"));
        
        assertScriptException(new ScriptAssertion("3.123/true"), AffixBinaryOperator.class, 2);
        assertScriptException(new ScriptAssertion("3.444/false"), AffixBinaryOperator.class, 2);
        assertScriptException(new ScriptAssertion("3/null"), AffixBinaryOperator.class, 2);
        assertScriptException(new ScriptAssertion("3/'asdfasfd'"), AffixBinaryOperator.class, 2);
        
        assertScriptException(new ScriptAssertion("true/3"), AffixBinaryOperator.class, 1);
        assertScriptException(new ScriptAssertion("null/3"), AffixBinaryOperator.class, 1);
        assertScriptException(new ScriptAssertion("'asdfasfd'/3"), AffixBinaryOperator.class, 1);
        
        assertScriptException(new ScriptAssertion("3%a_sdfsfd"));
        assertScriptException(new ScriptAssertion("3%a_$$[3]"));
        assertScriptException(new ScriptAssertion("3%(a+b+c)"));
        assertScriptException(new ScriptAssertion("3%4.234243"));
        
        assertScriptException(new ScriptAssertion("3.123%true"), AffixBinaryOperator.class, 2);
        assertScriptException(new ScriptAssertion("3.444%false"), AffixBinaryOperator.class, 2);
        assertScriptException(new ScriptAssertion("3%null"), AffixBinaryOperator.class, 2);
        assertScriptException(new ScriptAssertion("3%'asdfasfd'"), AffixBinaryOperator.class, 2);
        
        assertScriptException(new ScriptAssertion("true%3"), AffixBinaryOperator.class, 1);
        assertScriptException(new ScriptAssertion("null%3"), AffixBinaryOperator.class, 1);
        assertScriptException(new ScriptAssertion("'asdfasfd'%3"), AffixBinaryOperator.class, 1);
    }
    
    @Test
    public void testAI1() {
        new ScriptAssertion("bsdf=(a+b*3.0%4.0)/5.12%6.12;")
            .compile()
            .child(0)
                .hasChild(13)
                .token(ID, "bsdf")
                .token(ID, "a")
                .token(ID, "b")
                .token(NUM, "3.0")
                .token(OP, "*")
                .token(NUM, "4.0")
                .token(OP, "%")
                .token(OP, "+")
                .token(NUM, "5.12")
                .token(OP, "/")
                .token(NUM, "6.12")
                .token(OP, "%")
                ;
    }
    
    //shift
    @Test
    public void testAJ() {
        assertScriptException(new ScriptAssertion("3>>3"));
        assertScriptException(new ScriptAssertion("3.111>>3.123"));
        assertScriptException(new ScriptAssertion("3.111>>a[3]"));
        assertScriptException(new ScriptAssertion("3.111>>a++"));
        assertScriptException(new ScriptAssertion("3.111>>(a+b+c)++"));
        assertScriptException(new ScriptAssertion("3.111>>a_sdfsfd"));
        assertScriptException(new ScriptAssertion("a>>a_sdfsfd"));
        assertScriptException(new ScriptAssertion("(a+b+c)>>a_sdfsfd"));
        assertScriptException(new ScriptAssertion("(a+b+c)>>a[3]"));
        
        assertScriptException(new ScriptAssertion("3.123>>true"), AffixBinaryOperator.class, 2);
        assertScriptException(new ScriptAssertion("3.123>>false"), AffixBinaryOperator.class, 2);
        assertScriptException(new ScriptAssertion("3.123>>null"), AffixBinaryOperator.class, 2);
        assertScriptException(new ScriptAssertion("3.123>>'asdfsadf'"), AffixBinaryOperator.class, 2);
        
        assertScriptException(new ScriptAssertion("true>>1.20"), AffixBinaryOperator.class, 1);
        assertScriptException(new ScriptAssertion("false>>1.20"), AffixBinaryOperator.class, 1);
        assertScriptException(new ScriptAssertion("null>>1.20"), AffixBinaryOperator.class, 1);
        assertScriptException(new ScriptAssertion("'aaaa_&*(&*#($*#'>>1.20"), AffixBinaryOperator.class, 1);
        
        assertScriptException(new ScriptAssertion(">>1.123123"), Operator.class, 3);
        
        //---
        
        assertScriptException(new ScriptAssertion("3<<3"));
        assertScriptException(new ScriptAssertion("3.111<<3.123"));
        assertScriptException(new ScriptAssertion("3.111<<a[3]"));
        assertScriptException(new ScriptAssertion("3.111<<a++"));
        assertScriptException(new ScriptAssertion("3.111<<(a+b+c)++"));
        assertScriptException(new ScriptAssertion("3.111<<a_sdfsfd"));
        assertScriptException(new ScriptAssertion("a<<a_sdfsfd"));
        assertScriptException(new ScriptAssertion("(a+b+c)<<a_sdfsfd"));
        assertScriptException(new ScriptAssertion("(a+b+c)<<a[3]"));
        
        assertScriptException(new ScriptAssertion("3.123<<true"), AffixBinaryOperator.class, 2);
        assertScriptException(new ScriptAssertion("3.123<<false"), AffixBinaryOperator.class, 2);
        assertScriptException(new ScriptAssertion("3.123<<null"), AffixBinaryOperator.class, 2);
        assertScriptException(new ScriptAssertion("3.123<<'asdfsadf'"), AffixBinaryOperator.class, 2);
        
        assertScriptException(new ScriptAssertion("true<<1.20"), AffixBinaryOperator.class, 1);
        assertScriptException(new ScriptAssertion("false<<1.20"), AffixBinaryOperator.class, 1);
        assertScriptException(new ScriptAssertion("null<<1.20"), AffixBinaryOperator.class, 1);
        assertScriptException(new ScriptAssertion("'aaaa_&*(&*#($*#'<<1.20"), AffixBinaryOperator.class, 1);
        
        assertScriptException(new ScriptAssertion("<<1.123123"), Operator.class, 3);
        
        //---
        
        new ScriptAssertion("a<<b>>1")
            .compile()
            .child(0)
            .hasChild(5)
            .token(ID, "a")
            .token(ID, "b")
            .token(OP, "<<")
            .token(NUM, "1")
            .token(OP, ">>")
            ;
        new ScriptAssertion("3.123 << bb >> 1.234")
            .compile()
            .child(0)
            .hasChild(5)
            .token(NUM, "3.123")
            .token(ID, "bb")
            .token(OP, "<<")
            .token(NUM, "1.234")
            .token(OP, ">>")
            ;
        new ScriptAssertion("3.123 >> (bb << 1.234)")
            .compile()
            .child(0)
            .hasChild(5)
            .token(NUM, "3.123")
            .token(ID, "bb")
            .token(NUM, "1.234")
            .token(OP, "<<")
            .token(OP, ">>")
            ;
    }
    
    //relational
    @Test
    public void testAK() {
        assertScriptException(new ScriptAssertion("3>3"));
        assertScriptException(new ScriptAssertion("3>a_sdfsfd"));
        assertScriptException(new ScriptAssertion("3.123>aaaa_aaa[3]"));
        assertScriptException(new ScriptAssertion("3.123>(a+b+c)[3]"));
        assertScriptException(new ScriptAssertion("3.123>'asdfsafd'"),MutualExclusivityOperator.class,0);
        
        assertScriptException(new ScriptAssertion("aaaa>3"));
        assertScriptException(new ScriptAssertion("aaaa>a_sdfsfd"));
        assertScriptException(new ScriptAssertion("aaaa>aaaa_aaa[3]"));
        assertScriptException(new ScriptAssertion("aaaa>(a+b+c)[3]"));
        assertScriptException(new ScriptAssertion("aaaa>'asdfsafd'"));
        
        assertScriptException(new ScriptAssertion("aaaa_aaa[3]>3"));
        assertScriptException(new ScriptAssertion("aaaa_aaa[3]>a_sdfsfd"));
        assertScriptException(new ScriptAssertion("aaaa_aaa[3]>aaaa_aaa[3]"));
        assertScriptException(new ScriptAssertion("aaaa_aaa[3]>(a+b+c)[3]"));
        assertScriptException(new ScriptAssertion("aaaa_aaa[3]>'asdfsafd'"));
        
        assertScriptException(new ScriptAssertion("'asdfsadf&^*#^(&(@!#:{}'>3"),MutualExclusivityOperator.class,0);
        assertScriptException(new ScriptAssertion("'asdfsadf&^*#^(&(@!#:{}'>a_sdfsfd"));
        assertScriptException(new ScriptAssertion("'asdfsadf&^*#^(&(@!#:{}'>aaaa_aaa[3]"));
        assertScriptException(new ScriptAssertion("'asdfsadf&^*#^(&(@!#:{}'>(a+b+c)[3]"));
        assertScriptException(new ScriptAssertion("'asdfsadf&^*#^(&(@!#:{}'>'asdfsafd'"));
        
        //----
        
        assertScriptException(new ScriptAssertion("3>=3"));
        assertScriptException(new ScriptAssertion("3>=a_sdfsfd"));
        assertScriptException(new ScriptAssertion("3.123>=aaaa_aaa[3]"));
        assertScriptException(new ScriptAssertion("3.123>=(a+b+c)[3]"));
        assertScriptException(new ScriptAssertion("3.123>='asdfsafd'"),MutualExclusivityOperator.class,0);
        
        assertScriptException(new ScriptAssertion("aaaa>=3"));
        assertScriptException(new ScriptAssertion("aaaa>=a_sdfsfd"));
        assertScriptException(new ScriptAssertion("aaaa>=aaaa_aaa[3]"));
        assertScriptException(new ScriptAssertion("aaaa>=(a+b+c)[3]"));
        assertScriptException(new ScriptAssertion("aaaa>='asdfsafd'"));
        
        assertScriptException(new ScriptAssertion("aaaa_aaa[3]>=3"));
        assertScriptException(new ScriptAssertion("aaaa_aaa[3]>=a_sdfsfd"));
        assertScriptException(new ScriptAssertion("aaaa_aaa[3]>=aaaa_aaa[3]"));
        assertScriptException(new ScriptAssertion("aaaa_aaa[3]>=(a+b+c)[3]"));
        assertScriptException(new ScriptAssertion("aaaa_aaa[3]>='asdfsafd'"));
        
        assertScriptException(new ScriptAssertion("'asdfsadf&^*#^(&(@!#:{}'>=3"),MutualExclusivityOperator.class,0);
        assertScriptException(new ScriptAssertion("'asdfsadf&^*#^(&(@!#:{}'>=a_sdfsfd"));
        assertScriptException(new ScriptAssertion("'asdfsadf&^*#^(&(@!#:{}'>=aaaa_aaa[3]"));
        assertScriptException(new ScriptAssertion("'asdfsadf&^*#^(&(@!#:{}'>=(a+b+c)[3]"));
        assertScriptException(new ScriptAssertion("'asdfsadf&^*#^(&(@!#:{}'>='asdfsafd'"));
        
        //----
        
        assertScriptException(new ScriptAssertion("3<3"));
        assertScriptException(new ScriptAssertion("3<a_sdfsfd"));
        assertScriptException(new ScriptAssertion("3.123<aaaa_aaa[3]"));
        assertScriptException(new ScriptAssertion("3.123<(a+b+c)[3]"));
        assertScriptException(new ScriptAssertion("3.123<'asdfsafd'"),MutualExclusivityOperator.class,0);
        
        assertScriptException(new ScriptAssertion("aaaa<3"));
        assertScriptException(new ScriptAssertion("aaaa<a_sdfsfd"));
        assertScriptException(new ScriptAssertion("aaaa<aaaa_aaa[3]"));
        assertScriptException(new ScriptAssertion("aaaa<(a+b+c)[3]"));
        assertScriptException(new ScriptAssertion("aaaa<'asdfsafd'"));
        
        assertScriptException(new ScriptAssertion("aaaa_aaa[3]<3"));
        assertScriptException(new ScriptAssertion("aaaa_aaa[3]<a_sdfsfd"));
        assertScriptException(new ScriptAssertion("aaaa_aaa[3]<aaaa_aaa[3]"));
        assertScriptException(new ScriptAssertion("aaaa_aaa[3]<(a+b+c)[3]"));
        assertScriptException(new ScriptAssertion("aaaa_aaa[3]<'asdfsafd'"));
        
        assertScriptException(new ScriptAssertion("'asdfsadf&^*#^(&(@!#:{}'<3"),MutualExclusivityOperator.class,0);
        assertScriptException(new ScriptAssertion("'asdfsadf&^*#^(&(@!#:{}'<a_sdfsfd"));
        assertScriptException(new ScriptAssertion("'asdfsadf&^*#^(&(@!#:{}'<aaaa_aaa[3]"));
        assertScriptException(new ScriptAssertion("'asdfsadf&^*#^(&(@!#:{}'<(a+b+c)[3]"));
        assertScriptException(new ScriptAssertion("'asdfsadf&^*#^(&(@!#:{}'<'asdfsafd'"));
        
        //----
        
        assertScriptException(new ScriptAssertion("3<=3"));
        assertScriptException(new ScriptAssertion("3<=a_sdfsfd"));
        assertScriptException(new ScriptAssertion("3.123<=aaaa_aaa[3]"));
        assertScriptException(new ScriptAssertion("3.123<=(a+b+c)[3]"));
        assertScriptException(new ScriptAssertion("3.123<='asdfsafd'"),MutualExclusivityOperator.class,0);
        
        assertScriptException(new ScriptAssertion("aaaa<=3"));
        assertScriptException(new ScriptAssertion("aaaa<=a_sdfsfd"));
        assertScriptException(new ScriptAssertion("aaaa<=aaaa_aaa[3]"));
        assertScriptException(new ScriptAssertion("aaaa<=(a+b+c)[3]"));
        assertScriptException(new ScriptAssertion("aaaa<='asdfsafd'"));
        
        assertScriptException(new ScriptAssertion("aaaa_aaa[3]<=3"));
        assertScriptException(new ScriptAssertion("aaaa_aaa[3]<=a_sdfsfd"));
        assertScriptException(new ScriptAssertion("aaaa_aaa[3]<=aaaa_aaa[3]"));
        assertScriptException(new ScriptAssertion("aaaa_aaa[3]<=(a+b+c)[3]"));
        assertScriptException(new ScriptAssertion("aaaa_aaa[3]<='asdfsafd'"));
        
        assertScriptException(new ScriptAssertion("'asdfsadf&^*#^(&(@!#:{}'<=3"),MutualExclusivityOperator.class,0);
        assertScriptException(new ScriptAssertion("'asdfsadf&^*#^(&(@!#:{}'<=a_sdfsfd"));
        assertScriptException(new ScriptAssertion("'asdfsadf&^*#^(&(@!#:{}'<=aaaa_aaa[3]"));
        assertScriptException(new ScriptAssertion("'asdfsadf&^*#^(&(@!#:{}'<=(a+b+c)[3]"));
        assertScriptException(new ScriptAssertion("'asdfsadf&^*#^(&(@!#:{}'<='asdfsafd'"));
    }
    
    //equality
    @Test
    public void testAL() {
        assertScriptException(new ScriptAssertion("3 == 3"));
        assertScriptException(new ScriptAssertion("3 == (a+b+c)"));
        assertScriptException(new ScriptAssertion("3 == a"));
        assertScriptException(new ScriptAssertion("3 == true"), MutualExclusivityOperator.class, 0);
        assertScriptException(new ScriptAssertion("3 == false"), MutualExclusivityOperator.class, 0);
        assertScriptException(new ScriptAssertion("3 == null"), MutualExclusivityOperator.class, 0);
        assertScriptException(new ScriptAssertion("3 == 'asdfsafd+sdfsfd'"), MutualExclusivityOperator.class, 0);
        
        assertScriptException(new ScriptAssertion("true == 3"), MutualExclusivityOperator.class, 0);
        assertScriptException(new ScriptAssertion("true == (a+b+c)"));
        assertScriptException(new ScriptAssertion("true == a"));
        assertScriptException(new ScriptAssertion("true == true"));
        assertScriptException(new ScriptAssertion("true == false"));
        assertScriptException(new ScriptAssertion("true == null"), MutualExclusivityOperator.class, 0);
        assertScriptException(new ScriptAssertion("true == 'asdfsafd+sdfsfd'"), MutualExclusivityOperator.class, 0);
        
        assertScriptException(new ScriptAssertion("a == 3"));
        assertScriptException(new ScriptAssertion("a == (a+b+c)"));
        assertScriptException(new ScriptAssertion("a == a"));
        assertScriptException(new ScriptAssertion("a == true"));
        assertScriptException(new ScriptAssertion("a == false"));
        assertScriptException(new ScriptAssertion("a == null"));
        assertScriptException(new ScriptAssertion("a == 'asdfsafd+sdfsfd'"));
        
        assertScriptException(new ScriptAssertion("(a+b+c) == 3"));
        assertScriptException(new ScriptAssertion("(a+b+c) == (a+b+c)"));
        assertScriptException(new ScriptAssertion("(a+b+c) == a"));
        assertScriptException(new ScriptAssertion("(a+b+c) == true"));
        assertScriptException(new ScriptAssertion("(a+b+c) == false"));
        assertScriptException(new ScriptAssertion("(a+b+c) == null"));
        assertScriptException(new ScriptAssertion("(a+b+c) == 'asdfsafd+sdfsfd'"));
        
        assertScriptException(new ScriptAssertion("true == 3"), MutualExclusivityOperator.class, 0);
        assertScriptException(new ScriptAssertion("true == (a+b+c)"));
        assertScriptException(new ScriptAssertion("true == a"));
        assertScriptException(new ScriptAssertion("true == true"));
        assertScriptException(new ScriptAssertion("true == false"));
        assertScriptException(new ScriptAssertion("true == null"), MutualExclusivityOperator.class, 0);
        assertScriptException(new ScriptAssertion("true == 'asdfsafd+sdfsfd'"), MutualExclusivityOperator.class, 0);
        
        assertScriptException(new ScriptAssertion("'asdfsafd_S(*DF' == 3"), MutualExclusivityOperator.class, 0);
        assertScriptException(new ScriptAssertion("'asdfsafd_S(*DF' == (a+b+c)"));
        assertScriptException(new ScriptAssertion("'asdfsafd_S(*DF' == a"));
        assertScriptException(new ScriptAssertion("'asdfsafd_S(*DF' == true"), MutualExclusivityOperator.class, 0);
        assertScriptException(new ScriptAssertion("'asdfsafd_S(*DF' == false"), MutualExclusivityOperator.class, 0);
        assertScriptException(new ScriptAssertion("'asdfsafd_S(*DF' == null"), MutualExclusivityOperator.class, 0);
        assertScriptException(new ScriptAssertion("'asdfsafd_S(*DF' == 'asdfsafd+sdfsfd'"));
        
        //-----
        
        assertScriptException(new ScriptAssertion("3 != 3"));
        assertScriptException(new ScriptAssertion("3 != (a+b+c)"));
        assertScriptException(new ScriptAssertion("3 != a"));
        assertScriptException(new ScriptAssertion("3 != true"), MutualExclusivityOperator.class, 0);
        assertScriptException(new ScriptAssertion("3 != false"), MutualExclusivityOperator.class, 0);
        assertScriptException(new ScriptAssertion("3 != null"), MutualExclusivityOperator.class, 0);
        assertScriptException(new ScriptAssertion("3 != 'asdfsafd+sdfsfd'"), MutualExclusivityOperator.class, 0);
        
        assertScriptException(new ScriptAssertion("true != 3"), MutualExclusivityOperator.class, 0);
        assertScriptException(new ScriptAssertion("true != (a+b+c)"));
        assertScriptException(new ScriptAssertion("true != a"));
        assertScriptException(new ScriptAssertion("true != true"));
        assertScriptException(new ScriptAssertion("true != false"));
        assertScriptException(new ScriptAssertion("true != null"), MutualExclusivityOperator.class, 0);
        assertScriptException(new ScriptAssertion("true != 'asdfsafd+sdfsfd'"), MutualExclusivityOperator.class, 0);
        
        assertScriptException(new ScriptAssertion("a != 3"));
        assertScriptException(new ScriptAssertion("a != (a+b+c)"));
        assertScriptException(new ScriptAssertion("a != a"));
        assertScriptException(new ScriptAssertion("a != true"));
        assertScriptException(new ScriptAssertion("a != false"));
        assertScriptException(new ScriptAssertion("a != null"));
        assertScriptException(new ScriptAssertion("a != 'asdfsafd+sdfsfd'"));
        
        assertScriptException(new ScriptAssertion("(a+b+c) != 3"));
        assertScriptException(new ScriptAssertion("(a+b+c) != (a+b+c)"));
        assertScriptException(new ScriptAssertion("(a+b+c) != a"));
        assertScriptException(new ScriptAssertion("(a+b+c) != true"));
        assertScriptException(new ScriptAssertion("(a+b+c) != false"));
        assertScriptException(new ScriptAssertion("(a+b+c) != null"));
        assertScriptException(new ScriptAssertion("(a+b+c) != 'asdfsafd+sdfsfd'"));
        
        assertScriptException(new ScriptAssertion("true != 3"), MutualExclusivityOperator.class, 0);
        assertScriptException(new ScriptAssertion("true != (a+b+c)"));
        assertScriptException(new ScriptAssertion("true != a"));
        assertScriptException(new ScriptAssertion("true != true"));
        assertScriptException(new ScriptAssertion("true != false"));
        assertScriptException(new ScriptAssertion("true != null"), MutualExclusivityOperator.class, 0);
        assertScriptException(new ScriptAssertion("true != 'asdfsafd+sdfsfd'"), MutualExclusivityOperator.class, 0);
        
        assertScriptException(new ScriptAssertion("'asdfsafd_S(*DF' != 3"), MutualExclusivityOperator.class, 0);
        assertScriptException(new ScriptAssertion("'asdfsafd_S(*DF' != (a+b+c)"));
        assertScriptException(new ScriptAssertion("'asdfsafd_S(*DF' != a"));
        assertScriptException(new ScriptAssertion("'asdfsafd_S(*DF' != true"), MutualExclusivityOperator.class, 0);
        assertScriptException(new ScriptAssertion("'asdfsafd_S(*DF' != false"), MutualExclusivityOperator.class, 0);
        assertScriptException(new ScriptAssertion("'asdfsafd_S(*DF' != null"), MutualExclusivityOperator.class, 0);
        assertScriptException(new ScriptAssertion("'asdfsafd_S(*DF' != 'asdfsafd+sdfsfd'"));
    }
    
    //bitwise and
    @Test
    public void testAM() {
        assertScriptException(new ScriptAssertion("3&3"));
        assertScriptException(new ScriptAssertion("3&'asdfsdf'"), AffixBinaryOperator.class, 2);
        assertScriptException(new ScriptAssertion("3&asadfsfd_sdfsfd"));
        assertScriptException(new ScriptAssertion("3&(a&b&c)"));
        assertScriptException(new ScriptAssertion("3&a[3]"));
        
        assertScriptException(new ScriptAssertion("'asdfsdf'&3"), AffixBinaryOperator.class, 1);
        assertScriptException(new ScriptAssertion("'asdfsdf'&'asdfsdf'"), AffixBinaryOperator.class, 1);
        assertScriptException(new ScriptAssertion("'asdfsdf'&asadfsfd_sdfsfd"), AffixBinaryOperator.class, 1);
        assertScriptException(new ScriptAssertion("'asdfsdf'&(a&b&c)"), AffixBinaryOperator.class, 1);
        assertScriptException(new ScriptAssertion("'asdfsdf'&a[3]"), AffixBinaryOperator.class, 1);
        
        assertScriptException(new ScriptAssertion("(a&b&c)&3"));
        assertScriptException(new ScriptAssertion("(a&b&c)&'asdfsdf'"), AffixBinaryOperator.class, 2);
        assertScriptException(new ScriptAssertion("(a&b&c)&asadfsfd_sdfsfd"));
        assertScriptException(new ScriptAssertion("(a&b&c)&(a&b&c)"));
        assertScriptException(new ScriptAssertion("(a&b&c)&a[3]"));
        
        assertScriptException(new ScriptAssertion("a[3]&3"));
        assertScriptException(new ScriptAssertion("a[3]&'asdfsdf'"), AffixBinaryOperator.class, 2);
        assertScriptException(new ScriptAssertion("a[3]&asadfsfd_sdfsfd"));
        assertScriptException(new ScriptAssertion("a[3]&(a&b&c)"));
        assertScriptException(new ScriptAssertion("a[3]&a[3]"));
        
        assertScriptException(new ScriptAssertion("asadfsfd_sdfsfd&3"));
        assertScriptException(new ScriptAssertion("asadfsfd_sdfsfd&'asdfsdf'"), AffixBinaryOperator.class, 2);
        assertScriptException(new ScriptAssertion("asadfsfd_sdfsfd&asadfsfd_sdfsfd"));
        assertScriptException(new ScriptAssertion("asadfsfd_sdfsfd&(a&b&c)"));
        assertScriptException(new ScriptAssertion("asadfsfd_sdfsfd&a[3]"));
        assertScriptException(new ScriptAssertion("a&false"));
        
        assertScriptException(new ScriptAssertion("true&3"), AffixBinaryOperator.class, 1);
        assertScriptException(new ScriptAssertion("false&3"), AffixBinaryOperator.class, 1);
        assertScriptException(new ScriptAssertion("null&3"), AffixBinaryOperator.class, 1);
        assertScriptException(new ScriptAssertion("null&false"), AffixBinaryOperator.class, 1);
        
        assertScriptException(new ScriptAssertion("3.234234&true"), AffixBinaryOperator.class, 2);
        assertScriptException(new ScriptAssertion("3.234234&false"), AffixBinaryOperator.class, 2);
        assertScriptException(new ScriptAssertion("3.234234&null"), AffixBinaryOperator.class, 2);
        
        new ScriptAssertion("a & b && c & d")
            .compile()
            .child(0)
            .hasChild(5)
            .token(ID, "a")
            .token(ID, "b")
            .token(OP, "&")
            .child()
                .hasChild(3)
                .token(ID, "c")
                .token(ID, "d")
                .token(OP, "&")
                .parent()
            .token(OP, "&&")
            ;
    }
    
    //logical and
    @Test
    public void testAN() {
        assertScriptException(new ScriptAssertion("a+b && false"));
        assertScriptException(new ScriptAssertion("a[3] && false"));
        assertScriptException(new ScriptAssertion("aaaa && false"));
        assertScriptException(new ScriptAssertion("true && false"));
        
        assertScriptException(new ScriptAssertion("false && a + b"));
        assertScriptException(new ScriptAssertion("false && a[3]"));
        assertScriptException(new ScriptAssertion("false && aaaa"));
        assertScriptException(new ScriptAssertion("false && true"));
        
        new ScriptAssertion("((a=a+3)>5) && ((b=b-4)<10)")
            .compile()
            .child(0)
                .hasChild(9)
                .token(ID, "a")
                .token(ID, "a")
                .token(NUM, "3")
                .token(OP, "+")
                .token(OP, "=")
                .token(NUM, "5")
                .token(OP, ">")
                .child()
                    .hasChild(7)
                    .token(ID, "b")
                    .token(ID, "b")
                    .token(NUM, "4")
                    .token(OP, "-")
                    .token(OP, "=")
                    .token(NUM, "10")
                    .token(OP, "<")
                    .parent()
                .token(OP, "&&")
                .and()
            ;
    }
    
    //bitwise xor
    @Test
    public void testAO() {
        assertScriptException(new ScriptAssertion("3^3"));
        assertScriptException(new ScriptAssertion("3^'asdfsdf'"), AffixBinaryOperator.class, 2);
        assertScriptException(new ScriptAssertion("3^asadfsfd_sdfsfd"));
        assertScriptException(new ScriptAssertion("3^(a^b^c)"));
        assertScriptException(new ScriptAssertion("3^a[3]"));
        
        assertScriptException(new ScriptAssertion("'asdfsdf'^3"), AffixBinaryOperator.class, 1);
        assertScriptException(new ScriptAssertion("'asdfsdf'^'asdfsdf'"), AffixBinaryOperator.class, 1);
        assertScriptException(new ScriptAssertion("'asdfsdf'^asadfsfd_sdfsfd"), AffixBinaryOperator.class, 1);
        assertScriptException(new ScriptAssertion("'asdfsdf'^(a^b^c)"), AffixBinaryOperator.class, 1);
        assertScriptException(new ScriptAssertion("'asdfsdf'^a[3]"), AffixBinaryOperator.class, 1);
        
        assertScriptException(new ScriptAssertion("(a^b^c)^3"));
        assertScriptException(new ScriptAssertion("(a^b^c)^'asdfsdf'"), AffixBinaryOperator.class, 2);
        assertScriptException(new ScriptAssertion("(a^b^c)^asadfsfd_sdfsfd"));
        assertScriptException(new ScriptAssertion("(a^b^c)^(a^b^c)"));
        assertScriptException(new ScriptAssertion("(a^b^c)^a[3]"));
        
        assertScriptException(new ScriptAssertion("a[3]^3"));
        assertScriptException(new ScriptAssertion("a[3]^'asdfsdf'"), AffixBinaryOperator.class, 2);
        assertScriptException(new ScriptAssertion("a[3]^asadfsfd_sdfsfd"));
        assertScriptException(new ScriptAssertion("a[3]^(a^b^c)"));
        assertScriptException(new ScriptAssertion("a[3]^a[3]"));
        
        assertScriptException(new ScriptAssertion("asadfsfd_sdfsfd^3"));
        assertScriptException(new ScriptAssertion("asadfsfd_sdfsfd^'asdfsdf'"), AffixBinaryOperator.class, 2);
        assertScriptException(new ScriptAssertion("asadfsfd_sdfsfd^asadfsfd_sdfsfd"));
        assertScriptException(new ScriptAssertion("asadfsfd_sdfsfd^(a^b^c)"));
        assertScriptException(new ScriptAssertion("asadfsfd_sdfsfd^a[3]"));
        
        assertScriptException(new ScriptAssertion("true^3"), AffixBinaryOperator.class, 1);
        assertScriptException(new ScriptAssertion("false^3"), AffixBinaryOperator.class, 1);
        assertScriptException(new ScriptAssertion("null^3"), AffixBinaryOperator.class, 1);
        
        assertScriptException(new ScriptAssertion("3.234234^true"), AffixBinaryOperator.class, 2);
        assertScriptException(new ScriptAssertion("3.234234^false"), AffixBinaryOperator.class, 2);
        assertScriptException(new ScriptAssertion("3.234234^null"), AffixBinaryOperator.class, 2);
        
        new ScriptAssertion("a = r ^ s&4+f && c")
            .compile()
            .child(0)
            .hasChild(11)
            .token(ID, "a")
            .token(ID, "r")
            .token(ID, "s")
            .token(NUM, "4")
            .token(ID, "f")
            .token(OP, "+")
            .token(OP, "&")
            .token(OP, "^")
            .token(ID, "c")
            .token(OP, "&&")
            .token(OP, "=")
            ;
        
        new ScriptAssertion("a = aa ^ c&&5+6 & c")
            .compile()
            .child(0)
            .hasChild(7)
            .token(ID, "a")
            .token(ID, "aa")
            .token(ID, "c")
            .token(OP, "^")
            .child()
                .token(NUM, "11")
                .token(ID, "c")
                .token(OP, "&")
                .parent()
            .token(OP, "&&")
            .token(OP, "=")
            ;
    }
    
    //bitwise or
    //unlike bitwise and, this operator is only defined for numeric types
    @Test
    public void testAP() {
        assertScriptException(new ScriptAssertion("3|3"));
        assertScriptException(new ScriptAssertion("3|'asdfsdf'"), AffixBinaryOperator.class, 2);
        assertScriptException(new ScriptAssertion("3|asadfsfd_sdfsfd"));
        assertScriptException(new ScriptAssertion("3|(a|b|c)"));
        assertScriptException(new ScriptAssertion("3|a[3]"));
        
        assertScriptException(new ScriptAssertion("'asdfsdf'|3"), AffixBinaryOperator.class, 1);
        assertScriptException(new ScriptAssertion("'asdfsdf'|'asdfsdf'"), AffixBinaryOperator.class, 1);
        assertScriptException(new ScriptAssertion("'asdfsdf'|asadfsfd_sdfsfd"), AffixBinaryOperator.class, 1);
        assertScriptException(new ScriptAssertion("'asdfsdf'|(a|b|c)"), AffixBinaryOperator.class, 1);
        assertScriptException(new ScriptAssertion("'asdfsdf'|a[3]"), AffixBinaryOperator.class, 1);
        
        assertScriptException(new ScriptAssertion("(a|b|c)|3"));
        assertScriptException(new ScriptAssertion("(a|b|c)|'asdfsdf'"), AffixBinaryOperator.class, 2);
        assertScriptException(new ScriptAssertion("(a|b|c)|asadfsfd_sdfsfd"));
        assertScriptException(new ScriptAssertion("(a|b|c)|(a|b|c)"));
        assertScriptException(new ScriptAssertion("(a|b|c)|a[3]"));
        
        assertScriptException(new ScriptAssertion("a[3]|3"));
        assertScriptException(new ScriptAssertion("a[3]|'asdfsdf'"), AffixBinaryOperator.class, 2);
        assertScriptException(new ScriptAssertion("a[3]|asadfsfd_sdfsfd"));
        assertScriptException(new ScriptAssertion("a[3]|(a|b|c)"));
        assertScriptException(new ScriptAssertion("a[3]|a[3]"));
        
        assertScriptException(new ScriptAssertion("asadfsfd_sdfsfd|3"));
        assertScriptException(new ScriptAssertion("asadfsfd_sdfsfd|'asdfsdf'"), AffixBinaryOperator.class, 2);
        assertScriptException(new ScriptAssertion("asadfsfd_sdfsfd|asadfsfd_sdfsfd"));
        assertScriptException(new ScriptAssertion("asadfsfd_sdfsfd|(a|b|c)"));
        assertScriptException(new ScriptAssertion("asadfsfd_sdfsfd|a[3]"));
        
        assertScriptException(new ScriptAssertion("true|3"), AffixBinaryOperator.class, 1);
        assertScriptException(new ScriptAssertion("false|3"), AffixBinaryOperator.class, 1);
        assertScriptException(new ScriptAssertion("null|3"), AffixBinaryOperator.class, 1);
        
        assertScriptException(new ScriptAssertion("3.234234|true"), AffixBinaryOperator.class, 2);
        assertScriptException(new ScriptAssertion("3.234234|false"), AffixBinaryOperator.class, 2);
        assertScriptException(new ScriptAssertion("3.234234|null"), AffixBinaryOperator.class, 2);
        
        new ScriptAssertion("a | b || c | d")
            .compile()
            .child(0)
            .hasChild(5)
            .token(ID, "a")
            .token(ID, "b")
            .token(OP, "|")
            .child()
                .token(ID, "c")
                .token(ID, "d")
                .token(OP, "|")
                .parent()
            .token(OP, "||")
            ;
    }
    
    //logical or
    @Test
    public void testAQ() {
        assertScriptException(new ScriptAssertion("a+b || false"));
        assertScriptException(new ScriptAssertion("a[3] || false"));
        assertScriptException(new ScriptAssertion("aaaa || false"));
        assertScriptException(new ScriptAssertion("true || false"));
        
        assertScriptException(new ScriptAssertion("false || a + b"));
        assertScriptException(new ScriptAssertion("false || a[3]"));
        assertScriptException(new ScriptAssertion("false || aaaa"));
        assertScriptException(new ScriptAssertion("false || true"));
    }
    
    //------------ evaluation ----------------
    //Most cases are tested against Nashorn script engine
    
    // additive + 
    @Test
    public void testAR() throws ScriptException {
        //plain additive
        testEvaluation("aaaa+b+c-3.333*1.2345", 
                asMap("aaaa",new BigDecimal("3.434")
                        ,"b",new BigDecimal("1.0")
                        ,"c",new BigDecimal("17.343")));
        //add by 0
        testEvaluation("aaaa+0+b+0", 
                asMap("aaaa",new BigDecimal("0")
                        ,"b",new BigDecimal("1.0")
                        ,"c",new BigDecimal("17.343")));
        
        //number concatenated with string
        testEvaluation("aaaa+'sdfasdf'+b+'*(&@(#^(*&@*(#@'+c", 
                asMap("aaaa",new BigDecimal("0")
                        ,"b",new BigDecimal("1.0")
                        ,"c",new BigDecimal("17.343")));
        testEvaluation("1.11103+'sdfasdf'+b+'*(&@(#^(*&@*(#@'+3.007", 
                asMap("aaaa",new BigDecimal("0")
                        ,"b",new BigDecimal("1.0")
                        ,"c",new BigDecimal("17.343")));
        testEvaluation("1+2+'abc'+4+5", 
                asMap("aaaa",new BigDecimal("0")
                        ,"b",new BigDecimal("1.0")
                        ,"c",new BigDecimal("17.343")));
        //string concatenation
        testEvaluation("aaaa+'asdfasdf'+aaaa", 
                asMap("aaaa","abcdef"
                        ,"b",new BigDecimal("1.0")
                        ,"c",new BigDecimal("17.343")));
        
        //null concatenated with string
        testEvaluation("null+'asdfasdf'+aaaa", 
                asMap("aaaa",null
                        ,"b",new BigDecimal("1.0")
                        ,"c",new BigDecimal("17.343")));
        
        //boolean values concatenated with string
        testEvaluation("false+aaaa+true+'asdfasfd'+false+'sssss'+true", 
                asMap("aaaa","abcdef"
                        ,"b",new BigDecimal("1.0")
                        ,"c",new BigDecimal("17.343")));
        
        //object(references) concatenated with string
        testEvaluation("aaaa+'sadfsadf'", 
                asMap("aaaa",new Object()
                        ,"b",new BigDecimal("1.0")
                        ,"c",new BigDecimal("17.343")));
        
        //invalid additive operator made valid by string concatenation
        testEvaluation("aaaa+true+null+0+false+3+4+b", 
                asMap("aaaa","abcdef"
                        ,"b",new Object()
                        ,"c",new BigDecimal("17.343")));
    }
    
    // additive -
    @Test
    public void testAS() throws ScriptException {
        
        //plain subtraction
        testEvaluation("1.23-aaaa-2.444-c-3.333*1.2345", 
                asMap("aaaa",new BigDecimal("3.434")
                        ,"b",new BigDecimal("1.0")
                        ,"c",new BigDecimal("17.343")));
        
        //0.9-0.8 / 1.2-1.0 can't be tested as 
        //we use BigDecimal while Nashorn uses Double
        
        testEvaluation("9.2", 
                asMap("aaaa",new BigDecimal("3.434")
                        ,"b",new BigDecimal("1.0")
                        ,"c",new BigDecimal("17.343")));
        
        //0 subtracted by some value
        testEvaluation("0 - aaaa", 
                asMap("aaaa",new BigDecimal("3.434")
                        ,"b",new BigDecimal("1.0")
                        ,"c",new BigDecimal("17.343")));
        
        //fractional part longer than 16 digits
        testEvaluation("-0.0000000000000001-0.00000000000000001", 
                asMap("aaaa",new BigDecimal("3.434")
                        ,"b",new BigDecimal("1.0")
                        ,"c",new BigDecimal("17.343")));
    }
    
    // multiplicative *
    @Test
    public void testAT() throws ScriptException {
        //plain multiplication
        //stripTrailingZeros
        testEvaluation("aaaa*b*c*c*c", 
                asMap("aaaa",new BigDecimal("3.4")
                        ,"b",new BigDecimal("1.0")
                        ,"c",new BigDecimal("17.5")));
        //multiply by 0
        testEvaluation("aaaa*0*c*c", 
                asMap("aaaa",new BigDecimal("3.434")
                        ,"b",new BigDecimal("1.0")
                        ,"c",new BigDecimal("17.343")));
        //fractional part longer than 16 digits
        testEvaluation("aaaa*0.00000017*0.000000000000012456", 
                asMap("aaaa",new BigDecimal("3.434")
                        ,"b",new BigDecimal("1.0")
                        ,"c",new BigDecimal("17.343")));
        //inaccurate float numbers
        //Nashorn is inaccurate while BigDecimal is
//        testEvaluation("92*0.1", 
//                asMap("aaaa",new BigDecimal("3.434")
//                        ,"b",new BigDecimal("1.0")
//                        ,"c",new BigDecimal("17.343")));
        //multiply by negative numbers
        //multiply by parentheses structure
        testEvaluation("92*-(1.010101+2.777+3234234.121+4+5-6*-7.123*-aaaa-b-c*-c)-c*b*aaaa", 
                asMap("aaaa",new BigDecimal("3.434")
                        ,"b",new BigDecimal("1.0")
                        ,"c",new BigDecimal("17.343")));
    }
    
    // multiplicative /
    @Test
    public void testAU() throws ScriptException {
        
        //plain division
        //fractional part longer than 16 digits
        //Nashorn results are different from ours 
        //no matter we use Decimal32, 16 or 64 MathContext
//        testEvaluation("aaaa/b/c/aaaa", 
//                asMap("aaaa",new BigDecimal("3.434")
//                        ,"b",new BigDecimal("1.0")
//                        ,"c",new BigDecimal("17.343")));
        testEvaluation("aaaa/77", 
        asMap("aaaa",new BigDecimal("3.434")
                ,"b",new BigDecimal("1.0")
                ,"c",new BigDecimal("17.343")));
        
        //positive number / negative number
        //negative number / positive number
        //negative number / negative number
        //negative number / fractional number
        testEvaluation("aaaa/1.0", 
                asMap("aaaa",new BigDecimal("-3.404")
                        ,"b",new BigDecimal("1.0")
                        ,"c",new BigDecimal("17.343")));
        testEvaluation("b/aaaa", 
                asMap("aaaa",new BigDecimal("-3.404")
                        ,"b",new BigDecimal("1.0")
                        ,"c",new BigDecimal("17.343")));
        testEvaluation("aaaa/-123123/b", 
                asMap("aaaa",new BigDecimal("-3.404")
                        ,"b",new BigDecimal("1.0")
                        ,"c",new BigDecimal("17.343")));
        testEvaluation("b/aaaa/c/b/aaaa/222", 
                asMap("aaaa",new BigDecimal("-3.404")
                        ,"b",new BigDecimal("1.0")
                        ,"c",new BigDecimal("17.343")));
        
        //divide by sub expressions
        testEvaluation("aaaa/-(1*5-(6+7)/-10)", 
                asMap("aaaa",new BigDecimal("-3.4")
                        ,"b",new BigDecimal("1.0")
                        ,"c",new BigDecimal("17.343")));
        
        //Division not exact
        testEvaluation("1/3", 
                asMap("aaaa",new BigDecimal("-3.4")
                        ,"b",new BigDecimal("1.0")
                        ,"c",new BigDecimal("17.343")));
        
        //divide by zero
        //BigDecimal does not have Infinity
    }
    
    // power **
    @Test
    public void testAU1() throws Exception {
        assertEquals(evaluateMine("0**0", asMap("a",0)).toString(), "1");
        assertEquals(evaluateMine("3*4**0", asMap("a",3)).toString(), "3");
        assertEquals(evaluateMine("a*a**2", asMap("a",3)).toString(), "27");
        assertEquals(evaluateMine("3*4**2", asMap("a",4)).toString(), "48");
        try {
            evaluateMine("a+4**1999999999", asMap("a",4)).toString();
            Assert.fail();
        } catch (ScriptException e) {
            testException(e, ArithmeticBinaryOperator.class, 1);
        }
        try {
            evaluateMine("a+4**b", asMap("a",4,"b",1999999999)).toString();
            Assert.fail();
        } catch (Exception e) {
            testException(e, ArithmeticBinaryOperator.class, 0);
        }
    }
    
    // floor division
    @Test
    public void testAU2() throws Exception {
        try {
            evaluateMine("3//0", asMap("a",3)).toString();
        } catch (Exception e) {
            testException(e, ArithmeticBinaryOperator.class, 1);
        }
        assertEquals(evaluateMine("a//2", asMap("a",3)).toString(), "1");
        assertEquals(evaluateMine("3//2", asMap("a",0)).toString(), "1");
        assertEquals(evaluateMine("3//1", asMap("a",0)).toString(), "3");
        assertEquals(evaluateMine("a//b", asMap("a",34,"b",13)).toString(), "2");
        assertEquals(evaluateMine("a//b", asMap("a",-11.0,"b",3)).toString(), "-4");

    }
    
    // property access [] .
    @Test
    public void testAV() throws Exception {
        //indexing array by []
        testEvaluation("a$$$$[3]+'rrr'", 
                asMap("a$$$$",new String[] {"abc","def","tttt","3333"}));
        //indexing list by []
        testEvaluation("a$$$$[3]+'rrr'", 
                asMap("a$$$$",Arrays.asList("abc","def","tttt","3333")));
        //accessing map property by []
        testEvaluation("a$$$$['abc']+'rrr'", 
                asMap("a$$$$",asMap("abc",new BigDecimal("345.6"))));
        //accessing object property by []
        //obtaining length/size
        testEvaluation("'abc'.length", asMap());
        testEvaluation("a$$$$['length']*4", 
                asMap("a$$$$",new String[] {"abc","def","tttt","3333"}));
        {
            String script = "a$$$$['size']*4";
            Object mine = evaluateMine("a$$$$['size']*4", asMap("a$$$$",new String[] {"abc","def","tttt","3333"}));
            Object nashornResult = evaluateNashorn("a$$$$['length']*4", asMap("a$$$$",new String[] {"abc","def","tttt","3333"}));
            compareScriptResults(null, script, mine, nashornResult);
            Assert.assertEquals(mine, new BigDecimal(16));
        }
        testEvaluation("ccccc_$$$['abc']*4", 
                asMap("ccccc_$$$",new TestObject()));
        
        //accessing property of result of an expression by []
        testEvaluation("('abc'+'def')['length']*4", 
                asMap("ccccc_$$$",new TestObject()));
        
        //accessing property of result of an expression by [] wrapping a complex expression
        testEvaluation("('abc'+'def')['le'+'ng'+'t'+'h']*4", 
                asMap("ccccc_$$$",new TestObject()));
        testEvaluation("a$$$$[3*1+1-(1*2)+(0/1+1)]", 
                asMap("a$$$$",new String[] {"abc","def","tttt","3333"}));
        
        //indexing list by consecutive [] expressions
        {
            Object mine = evaluateMine("ccccc_$$$['list'][2][2]['size']*ccccc_$$$['list'][2][2][0]", 
                    asMap("ccccc_$$$",new TestObject()));
            Object nashornResult = evaluateNashorn("ccccc_$$$['list'][2][2]['length']*ccccc_$$$['list'][2][2][0]", 
                    asMap("ccccc_$$$",new TestObject()));
            compareScriptResults(null, "ccccc_$$$['list'][2][2]['length']*ccccc_$$$['list'][2][2][0]", mine, nashornResult);
        }
        
        //indexing array by consecutive [] expressions
        testEvaluation("ccccc_$$$['array'][0][2][4][2]*ccccc_$$$['array'][0][2][4]['length']", 
                asMap("ccccc_$$$",new TestObject()));
        
        //accessing map property by consecutive [] expressions
        testEvaluation("a$$$$['abcdef']['tttt'][3]", 
                asMap("a$$$$"
                        ,asMap("abcdef"
                                ,asMap("tttt",new String[] {"abc","def","tttt","3333"}))));
        
        //accessing object property by consecutive [] expressions
        testEvaluation("ccccc_$$$['object2']['flt']*4", 
                asMap("ccccc_$$$",new TestObject()));
        
        //obtaining length/size of string
        {
            Object mine = evaluateMine("ccccc_$$$['size']", 
                    asMap("ccccc_$$$","abcdef"));
            Object nashornResult = evaluateNashorn("ccccc_$$$['length']", 
                    asMap("ccccc_$$$","abcdef"));
            compareScriptResults(null, "ccccc_$$$['size']", mine, nashornResult);
        }
        testEvaluation("ccccc_$$$['length']", asMap("ccccc_$$$","abcdef"));
        testEvaluation("'abcdef'['length']", asMap("ccccc_$$$","abcdef"));
        
        assertEquals(evaluateMine("'abcdef'.empty || true", asMap("ccccc_$$$","abcdef")), Boolean.TRUE);
        assertEquals(evaluateMine("'abcdef'.empty", asMap("ccccc_$$$","abcdef")), Boolean.FALSE);
        assertEquals(evaluateMine("ccccc_$$$.toEpochDay", asMap("ccccc_$$$",LocalDate.of(1970, 1, 3))).toString(), "2");
        
        //indexing non-array and non-list object
        try {
            evaluateMine("a[3]", asMap("a",3));
        } catch (Exception e) {
            testException(e, Identifier.class, 1);
        }
        
        //getter throws exception
        try {
            evaluateMine("abc.property2333", asMap("abc",new TestObject()));
        } catch (Exception e) {
            testException(e, Identifier.class, 3);
        }
        
        //use setter to set property value
        testEvaluation("abc.setterProperty1 = 1;abc.setterProperty1 = 2;abc.setterProperty1", asMap("abc",new TestObject()));
        
        //incompatible types
        try {
            evaluateMine("abc.str1 = 3", asMap("abc",new TestObject()));
        } catch (Exception e) {
            testException(e, Identifier.class, 5);
        }
        //incompatible types, not implicitly convertible
        try {
            evaluateMine("abc.numberImpl1 = 3", asMap("abc",new TestObject()));
        } catch (Exception e) {
            testException(e, Identifier.class, 5);
        }
        
        //return null for non-existing property
        Assert.assertEquals(evaluateMine("abc.asdfasdf123123;abc.asdfasdf123123", asMap("abc",new TestObject())), null);
        //return null for length/size property of non-array, non-list object
        assertEquals(evaluateMine("abc.length", asMap("abc",new TestObject()))+"", "333");
        assertEquals(evaluateMine("abc.size", asMap("abc",asMap("a","b","b","c","d",1)))+"", "3");
        
        //setter throws exception
        try {
            evaluateMine("abc.propertyX = 333;", asMap("abc",new TestObject()));
        } catch (Exception e) {
            testException(e, Identifier.class, 5);
        }
        
        testEvaluation("''['']", asMap("abc",new TestObject()));
    }
    
    @Test
    public void testAW() throws Exception {
        //access object property by .
        //access property of result of an expression by .
        testEvaluation("('abc'+'def'+'111').length*4", asMap("ccccc_$$$",new TestObject()));
        testEvaluation("(ccccc_$$$.object2).flt*4", asMap("ccccc_$$$",new TestObject()));
        testEvaluation("(ccccc_$$$['object2']).flt*4", asMap("ccccc_$$$",new TestObject()));
        //consecutive .
        testEvaluation("ccccc_$$$.object2.flt*4", asMap("ccccc_$$$",new TestObject()));
        testEvaluation("(((ccccc_$$$).object2).flt)*4", asMap("ccccc_$$$",new TestObject()));
        testEvaluation("dcccsdf.object2.date.time*1", asMap("dcccsdf",new TestObject()));
        Assert.assertEquals(evaluateMine("('asdfasdf').size", Collections.emptyMap())+"", "8");
        Assert.assertEquals(evaluateMine("'asdfasdf'.size", Collections.emptyMap())+"", "8");
        //access static member of a class by its full qualified name
        Assert.assertEquals(evaluateMine(TestObject.class.getName()+".static1*456+7"
                , Collections.emptyMap())+"", "4567");
        Assert.assertEquals(evaluateMine("java.lang.Byte.MIN_VALUE*2"
                , Collections.emptyMap())+"", "-256");
        //access static member of a class which full qualified name 
        //is implicit by another global object
        Assert.assertEquals(evaluateMine("Constants.constant1"
                , asMap("abc",new TestObject()))+"", "11111");
    }
    
    //relational > < >= <= == !=
    @Test
    public void testAX() throws Exception {
        //compare strings
        testEvaluation("ccccc_>'abcdef'", asMap("ccccc_","11111","hehehehe","22222"));
        testEvaluation("'11111'>'abcdef'", asMap("ccccc_","11111","hehehehe","22222"));
        testEvaluation("ccccc_>='abcdef'", asMap("ccccc_","11111","hehehehe","22222"));
        testEvaluation("'11111'>='abcdef'", asMap("ccccc_","11111","hehehehe","22222"));
        testEvaluation("ccccc_<'abcdef'", asMap("ccccc_","11111","hehehehe","22222"));
        testEvaluation("'11111'<'abcdef'", asMap("ccccc_","11111","hehehehe","22222"));
        testEvaluation("ccccc_<='abcdef'", asMap("ccccc_","11111","hehehehe","22222"));
        testEvaluation("'11111'<='11112'", asMap("ccccc_","11111","hehehehe","22222"));
        //equality of strings
        testEvaluation("'11111'=='11112'", asMap("ccccc_","11111","hehehehe","22222"));
        testEvaluation("'11111'!='11112'", asMap("ccccc_","11111","hehehehe","22222"));
        
        //compare numbers
        //equality of numbers
        testEvaluation("1.0<=ccccc_", asMap("ccccc_",3.4,"hehehehe",4.4));
        testEvaluation("ccccc_>1.0", asMap("ccccc_",3.4,"hehehehe",4.4));
        testEvaluation("ccccc_!=1.0", asMap("ccccc_",3.4,"hehehehe",4.4));
        testEvaluation("ccccc_!=hehehehe", asMap("ccccc_",3.4,"hehehehe",4.4));
        testEvaluation("ccccc_==hehehehe", asMap("ccccc_",3.4,"hehehehe",4.4));
        testEvaluation("ccccc_>=hehehehe", asMap("ccccc_",3.4,"hehehehe",4.4));
        testEvaluation("ccccc_<=hehehehe", asMap("ccccc_",3.4,"hehehehe",4.4));
        testEvaluation("ccccc_<hehehehe", asMap("ccccc_",3.4,"hehehehe",4.4));
        testEvaluation("ccccc_>hehehehe", asMap("ccccc_",3.4,"hehehehe",4.4));
        
        //equality of boolean values
        testEvaluation("true==false", asMap("ccccc_",3.4,"hehehehe",4.4));
        testEvaluation("true!=false", asMap("ccccc_",3.4,"hehehehe",4.4));
        testEvaluation("true>false", asMap("ccccc_",3.4,"hehehehe",4.4));
        testEvaluation("true>=false", asMap("ccccc_",3.4,"hehehehe",4.4));
        testEvaluation("true<=false", asMap("ccccc_",3.4,"hehehehe",4.4));
        testEvaluation("true<false", asMap("ccccc_",3.4,"hehehehe",4.4));
        testEvaluation("true==hehehehe", asMap("ccccc_",true,"hehehehe",false));
        testEvaluation("ccccc_<hehehehe", asMap("ccccc_",true,"hehehehe",false));
        testEvaluation("ccccc_>hehehehe", asMap("ccccc_",true,"hehehehe",false));
        testEvaluation("false!=hehehehe", asMap("ccccc_",true,"hehehehe",false));
        
        //compare null with strings and vice versa
        testEvaluation("ccccc_==null", asMap("ccccc_","11111","hehehehe","22222"));
        testEvaluation("null==ccccc_", asMap("ccccc_","11111","hehehehe","22222"));
        try {
            evaluateMine("null==3.4", Collections.emptyMap());
            Assert.fail();
        } catch (Exception e) {
            testException(e, MutualExclusivityOperator.class, 0);
        }
        
        //compare null with arbitrary objects
        testEvaluation("ccccc_.list[2]==null", asMap("ccccc_",new TestObject()));
        testEvaluation("null!=ccccc_.list[2][2]", asMap("ccccc_",new TestObject()));
        
        //compare two identifiers
        testEvaluation("obj.abc != obj.def", asMap("obj",new TestObject()));
        testEvaluation("obj.str1 == obj.str2", asMap("obj",new TestObject()));
        testEvaluation("obj.list[0] != obj.list[2][0]", asMap("obj",new TestObject()));
        
        //compare nulls
        testEvaluation("obj != obj2", asMap("obj",null,"obj2",null));
        testEvaluation("obj == obj2", asMap("obj",null,"obj2",null));
        
        //compare two complex expressions
        testEvaluation("(obj.list[0] != null) < (obj.list[2][0].size >= 4)", asMap("obj",new TestObject()));
    }
    
    //assign = 
    @Test
    public void testAY() throws Exception{
        //assign to new references
        testEvaluation("aaaa = null;aaaa", asMap("obj",new TestObject()));
        testEvaluation("aaaa = false;aaaa", asMap("obj",new TestObject()));
        testEvaluation("aaaa=true;aaaa", asMap("obj",new TestObject()));
        testEvaluation("aaaa='asdf';aaaa", asMap("obj",new TestObject()));
        testEvaluation("aaaa=3.1212;aaaa", asMap("obj",new TestObject()));
        
        //assign to a property in global context
        //assign to an object property
        Assert.assertEquals(evaluateMine("obj.object2.decimal = null;obj.object2.decimal", asMap("obj",new TestObject()))+"", "null");
        Assert.assertEquals(evaluateMine("obj.object2.decimal = 0.9898;obj.object2.decimal", asMap("obj",new TestObject()))+"", "0.9898");
        Assert.assertEquals(evaluateMine("obj.static2 = 999;obj.static2", asMap("obj",new TestObject()))+"", "999");
        Assert.assertEquals(evaluateMine("obj.abc = 999;obj.abc", asMap("obj",new TestObject()))+"", "999");
        testEvaluation("obj.str1 = '090909';obj.str1", asMap("obj",new TestObject()));
        
        //assign values to element in array
        testEvaluation("obj.array[0][0] = 'ttttt';obj.array[0][0]", asMap("obj",new TestObject()));
        testEvaluation("obj.array[0][2][5] = '11111111';obj.array[0][2][5]", asMap("obj",new TestObject()));
        
        //assign values to element in list
        testEvaluation("obj.list[2][1] = '12345';obj.list[2][1]", asMap("obj",new TestObject()));

        //assign values to map property
        testEvaluation("ghi.def = '12345';ghi.def"
                     ,asMap("obj","def","ghi",asMap("obj","def","ghi", new TestObject())));
        testEvaluation("ghi.ghi.str1 = '12345';ghi.ghi.str1"
                ,asMap("obj","def","ghi",asMap("obj","def","ghi", new TestObject())));
        
        //assign values to property represented by interspersed [] and . expressions
        testEvaluation("ghi['ghi'].list[2][1] = '12345';ghi['ghi'].list[2][1]"
                ,asMap("obj","def","ghi",asMap("obj","def","ghi", new TestObject())));
        testEvaluation("a='ghi';ghi[a].list[2][1] = '12345';ghi[a].list[2][1]"
                ,asMap("obj","def","ghi",asMap("obj","def","ghi", new TestObject())));
        
        //assign values of local reference to a global variable
        Assert.assertEquals(evaluateMine("a=3.0;obj.abc=a;obj.abc", asMap("obj",new TestObject()))+"", "3.0");
        
        //assign values of local reference to value of property of global object
        testEvaluation("a=obj.list[2][1];a.length*2/3", asMap("obj",new TestObject()));
        
        //assign to non-existing property
        try {
            evaluateMine("obj.list2.abc = 5", asMap("obj",new TestObject()));
        } catch (Exception e) {
            testException(e, Identifier.class, 8);
        }
        
        //both value to be assigned and property accessing are complex expression
        testEvaluation("(ghi['ghi'].list[2][1]) = ('abc'+'def'+ghi.ghi.str1.length+ghi.ghi.str1 + 34567);ghi['ghi'].list[2][1]"
                ,asMap("obj","def","ghi",asMap("obj","def","ghi", new TestObject())));
    }
    
    //if
    @Test
    public void testAZ() throws Exception{
        //if A B else C / A = false
        testEvaluation("if(a+b == 5){a=a*2;} else {c=c+1;} a+b;",asMap("a",3,"b",4,"c",5));
        //if A B else C / A = true
        testEvaluation("if(a+b >= 7){a=a*2;} else {c=c+1;} a+b;",asMap("a",3,"b",4,"c",5));
        //if A B else if A1 B1 / A = true A1 = true
        testEvaluation("if(a+b >= 7){a=a*2;} else if(a+b>5){c=c+1;b=b+1;} a+b+c;",asMap("a",3,"b",4,"c",5));
        //if A B else A1 B1 / A = false A1 = true
        testEvaluation("if(a+b >= 17){a=a*2;} else if(a+b>5){c=c+1;b=b+1;} a+b+c;",asMap("a",3,"b",4,"c",5));
        //if A B else A1 B1 / A = false A1 = false
        testEvaluation("if(a+b >= 17){a=a*2;} else if(a+b>15){c=c+1;b=b+1;} a+b+c;",asMap("a",3,"b",4,"c",5));
        //if A B else A1 B1 else C / A = true A1 = true
        testEvaluation("if(a+b >= 3){a=a*2;} else if(a+b>4){c=c+1;b=b+1;}else {c=c*10;} a+b+c;",asMap("a",3,"b",4,"c",5));
        //if A B else A1 B1 else C / A = false A1 = true
        testEvaluation("if(a+b >= 13){a=a*2;} else if(a+b>4){c=c+1;b=b+1;}else {c=c*10;} a+b+c;",asMap("a",3,"b",4,"c",5));
        //if A B else A1 B1 else C / A = false A1 = false
        testEvaluation("if(a+b >= 13){a=a*2;} else if(a+b>14){c=c+1;b=b+1;}else {c=c*10;} a+b+c;",asMap("a",3,"b",4,"c",5));
    }
    
    @Test
    public void testAZ1() throws Exception{
        //if A B else C / A = false
//        testEvaluation("if(a+b == 5)a=a*2; else c=c+1; a+b;",asMap("a",3,"b",4,"c",5));
        //if A B else C / A = true
        testEvaluation("if(a+b >= 7)a=a*2; else c=c+1; a+b;",asMap("a",3,"b",4,"c",5));
        //if A B else if A1 B1 / A = true A1 = true
        testEvaluation("if(a+b >= 7)a=a*2; else if(a+b>5)c=c+1; a+b+c;",asMap("a",3,"b",4,"c",5));
        //if A B else A1 B1 / A = false A1 = true
        testEvaluation("if(a+b >= 17)a=a*2; else if(a+b>5)c=c+1; a+b+c;",asMap("a",3,"b",4,"c",5));
        //if A B else A1 B1 / A = false A1 = false
        testEvaluation("if(a+b >= 17)a=a*2; else if(a+b>15)c=c+1; a+b+c;",asMap("a",3,"b",4,"c",5));
        //if A B else A1 B1 else C / A = true A1 = true
        testEvaluation("if(a+b >= 3)a=a*2; else if(a+b>4)c=c+1;else c=c*10; a+b+c;",asMap("a",0,"b",1,"c",5));
        //if A B else A1 B1 else C / A = false A1 = true
        testEvaluation("if(a+b >= 13)a=a*2; else if(a+b>4)c=c+1;else c=c*10; a+b+c;",asMap("a",3,"b",4,"c",5));
        //if A B else A1 B1 else C / A = false A1 = false
        testEvaluation("if(a+b >= 13)a=a*2; else if(a+b>14)c=c+1;else c=c*10; a+b+c;",asMap("a",3,"b",4,"c",5));
        testEvaluation("if(a+b >= 13)a=a*2; else if(a+b>14)c=c+1; else if(a+b>1551) { a = a * 100; } else c=c*10; a+b+c;",asMap("a",3,"b",4,"c",5));
    }
    
    @Test
    public void testAZ2() throws Exception{
        //nested if structure
        testEvaluation("if(a+b >= 3){if(a+b>10){a=a*2;}else if(a+b>3){a=a*10;} else c = c + 1;}"
               + " else if(a+b > 15){c=c+1;if(c>2){c=c*10;}else if(c>10){c=c+1;} b=b+1;}"
               + " else {c=c*10; if(c>7){c=c*10;}else if(c>10){c=c+1;} } "
               + " a+b+c;",asMap("a",3,"b",4,"c",5));
        testEvaluation("if(a+b >= 13){if(a+b>10){a=a*2;}else if(a+b>3){a=a*10;} else c = c + 1;}"
                + " else if(a+b > 0 ){c=c+1;if(c>2){c=c*10;}else if(c>10){c=c+1;} b=b+1;}"
                + " else {c=c*10; if(c>7){c=c*10;}else if(c>10){c=c+1;} } "
                + " a+b+c;",asMap("a",3,"b",4,"c",5));
        testEvaluation("if(a+b >= 13){if(a+b>10){a=a*2;}else if(a+b>3){a=a*10;} else c = c + 1;}"
                + " else if(a+b > 1){c=c+1;if(c>2){c=c*10;}else if(c>10){c=c+1;} b=b+1;}"
                + " else {c=c*10; if(c>7){c=c*10;}else if(c>10){c=c+1;} } "
                + " a+b+c;",asMap("a",3,"b",4,"c",5));
    }
    
    @Test
    public void testAZ3() throws Exception{
        //logical last statement contained in if structure
        testEvaluation("if(a+b >= 13)a=a*2; else if(a+b>14)c=c+1;else c=c*10;",asMap("a",3,"b",4,"c",5));
        //logical last statement contained in else if / else structure
        testEvaluation("if(a+b >= 13){a=a*2;} else if(a+b>14){c=c+1;}else {c=c*10;}",asMap("a",3,"b",4,"c",5));
        testEvaluation("if(a+b >= 13){a=a*2;} else if(a+b>14){c=c+1;}else {c=c*10;b;}",asMap("a",3,"b",4,"c",5));
    }
    
    //ternary ?:
    @Test
    public void testBA() throws Exception{
        
        //plain ternary operator
        testEvaluation("b>3 ? (b=b+1): (c=c*10); c+b;",asMap("a",3,"b",4,"c",5));
        testEvaluation("b+a>3 ? (b=b+1)*a+b : b+(c=c*10)-c; c+b;",asMap("a",3,"b",4,"c",5));
        
        //result of ternary operator expression as an operand
        testEvaluation("dddd = a*(b>3 ? (b=b+1): (c=c*10)); c+b-dddd;",asMap("a",3,"b",4,"c",5));
        testEvaluation("dddd = (a>5?b:c)*(b>3 ? (b=b+1): (c=c*10)) + (b+b>10?3.234234:1.2222) "
                + "+ ('a'+'b'+(a*a>10 ? 'abc' : 'def')); c+b+dddd;",asMap("a",3,"b",4,"c",5));
        
        //ternary operator expression as the logical last statement
        testEvaluation("b>3 ? a+c : c+c",asMap("a",3,"b",4,"c",5));
        
        //nested ternary operator
        testEvaluation("b>3 ? a>4 ? c>5 ? (a=a+1) : (b=b+1) : b>7 ? c : c*c : a+a-1;",asMap("a",3,"b",4,"c",5));
        
        try {
            evaluateMine("(1+2) ? a : b",asMap("a",true,"b",false));
        } catch (Exception e) {
            testException(e, QuestionOperator.class, 0);
        }
    }
    
    //bitwise and &
    @Test
    public void testBB() throws Exception{
        
        //plain bitwise and
        testEvaluation("a & b & c & (a+b-c) & a*b/c",asMap("a",3,"b",4,"c",5));
        testEvaluation("a & b & c & (a+b-c) & a*b/c & 3.333 & 4.1414",asMap("a",3,"b",4,"c",5));
        
        //bitwise and of two boolean values
        Assert.assertEquals(evaluateMine("false & false", asMap("a",3,"b",4,"c",5))+"", Boolean.logicalAnd(false, false)+"");
        Assert.assertEquals(evaluateMine("false & true", asMap("a",3,"b",4,"c",5))+"", Boolean.logicalAnd(false, true)+"");
        Assert.assertEquals(evaluateMine("true & true", asMap("a",3,"b",4,"c",5))+"", Boolean.logicalAnd(true, true)+"");
        Assert.assertEquals(evaluateMine("true & false", asMap("a",3,"b",4,"c",5))+"", Boolean.logicalAnd(true, false)+"");
        Assert.assertEquals(evaluateMine("a & false", asMap("a",true))+"", Boolean.logicalAnd(true, false)+"");
        Assert.assertEquals(evaluateMine("a & a", asMap("a",true))+"", Boolean.logicalAnd(true, true)+"");
        Assert.assertEquals(evaluateMine("true & a", asMap("a",true))+"", Boolean.logicalAnd(true, true)+"");
        
        //consecutive bitwise and operators
        Assert.assertEquals(evaluateMine("false & true & true", asMap("a",3,"b",4,"c",5))+"", Boolean.logicalAnd(false, true)+"");
        
        //bitwise and of two float numbers
        testEvaluation("a & 1.33333333 & (a+b) & b*c & c*c+1-a & 8.98888 & a & b", asMap("a",3.134,"b",4.113,"c",55.033));

        //bitwise and of two expressions
        testEvaluation("(a+b+c) & (a-b-c) & a*a", asMap("a",3.134,"b",4.113,"c",55.033));
        Assert.assertEquals(evaluateMine("(a>1) & (b-2<1) & (b+b+c>10)", asMap("a",3,"b",4,"c",5))+"", "false");
    }
    
    //bitwise or |
    @Test
    public void testBC() throws Exception{
        
        //plain bitwise or
        testEvaluation("a | b | c | (a+b-c) | a*b/c",asMap("a",3,"b",4,"c",5));
        testEvaluation("a | b | c | (a+b-c) | a*b/c | 3.333 | 4.1414",asMap("a",3,"b",4,"c",5));
        
        //bitwise or of two boolean values
        Assert.assertEquals(evaluateMine("false | false", asMap("a",3,"b",4,"c",5))+"", (false | false)+"");
        Assert.assertEquals(evaluateMine("false | true", asMap("a",3,"b",4,"c",5))+"", (false | true)+"");
        Assert.assertEquals(evaluateMine("true | true", asMap("a",3,"b",4,"c",5))+"", (true | true)+"");
        Assert.assertEquals(evaluateMine("true | false", asMap("a",3,"b",4,"c",5))+"", (true | false)+"");
        
        //consecutive bitwise or
        Assert.assertEquals(evaluateMine("false | true | true", asMap("a",3,"b",4,"c",5))+"", (false | true | true)+"");
        
        //bitwise or of two float numbers
        testEvaluation("a | 1.33333333 | (a+b) | b*c | c*c+1-a | 8.98888 | a | b", asMap("a",3.134,"b",4.113,"c",55.033));

        //bitwise or of two expressions
        testEvaluation("(a+b+c) | (a-b-c) | a*a", asMap("a",3.134,"b",4.113,"c",55.033));
        Assert.assertEquals(evaluateMine("(a>1) | (b-2<1) | (b+b+c>10)", asMap("a",3,"b",4,"c",5))+"", "true");
    }
    
    //bitwise xor ^
    @Test
    public void testBD() throws Exception{
        
        //plain bitwise xor
        testEvaluation("a ^ b ^ c ^ (a+b-c) ^ a*b/c",asMap("a",3,"b",4,"c",5));
        testEvaluation("a ^ b ^ c ^ (a+b-c) ^ a*b/c ^ 3.333 ^ 4.1414",asMap("a",3,"b",4,"c",5));
        
        //bitwise xor of two boolean values
        Assert.assertEquals(evaluateMine("false ^ false", asMap("a",3,"b",4,"c",5))+"", (false ^ false)+"");
        Assert.assertEquals(evaluateMine("false ^ true", asMap("a",3,"b",4,"c",5))+"", (false ^ true)+"");
        Assert.assertEquals(evaluateMine("true ^ true", asMap("a",3,"b",4,"c",5))+"", (true ^ true)+"");
        Assert.assertEquals(evaluateMine("true ^ false", asMap("a",3,"b",4,"c",5))+"", (true ^ false)+"");
        
        //consecutive bitwise xor
        Assert.assertEquals(evaluateMine("false ^ true ^ true", asMap("a",3,"b",4,"c",5))+"", (false ^ true ^ true)+"");
        
        //bitwise xor of two float numbers
        testEvaluation("a ^ 1.33333333 ^ (a+b) ^ b*c ^ c*c+1-a ^ 8.98888 ^ a ^ b", asMap("a",3.134,"b",4.113,"c",55.033));

        //bitwise xor of two expressions
        testEvaluation("(a+b+c) ^ (a-b-c) ^ a*a", asMap("a",3.134,"b",4.113,"c",55.033));
        Assert.assertEquals(evaluateMine("(a>1) ^ (b-2<1) ^ (b+b+c>10)", asMap("a",3,"b",4,"c",5))+"", "false");
    }
    
    //logical && ||
    @Test
    public void testBE() throws Exception{
        //between two boolean values
        //between two expressions
        //short-circuit test
        //consecutive logical operations
        testEvaluation("true && false",asMap("a",3,"b",4,"c",5));
        testEvaluation("false && true",asMap("a",3,"b",4,"c",5));
        testEvaluation("true && true",asMap("a",3,"b",4,"c",5));
        testEvaluation("false && false",asMap("a",3,"b",4,"c",5));
        testEvaluation("((a=a+1)>15) && ((b=b-3)<4); b+a",asMap("a",3,"b",4,"c",5));
        testEvaluation("((a=a+1)>1) && ((b=b-3)<4); b+a",asMap("a",3,"b",4,"c",5));
        testEvaluation("((a=a+1)>1) && ((b=b-3)<-1) && (c=c*10)>7; b+a*c",asMap("a",3,"b",4,"c",5));
        testEvaluation("((a=a+1)>15) && ((b=b-3)<-1) && (c=c*10)>7; b+a*c",asMap("a",3,"b",4,"c",5));
        testEvaluation("((a=a+1)>1) && ((b=b-3)<1000) && (c=c*10)>70000; b+a*c",asMap("a",3,"b",4,"c",5));
        
        testEvaluation("true || false",asMap("a",3,"b",4,"c",5));
        testEvaluation("false || true",asMap("a",3,"b",4,"c",5));
        testEvaluation("true || true",asMap("a",3,"b",4,"c",5));
        testEvaluation("false || false",asMap("a",3,"b",4,"c",5));
        testEvaluation("((a=a+1)>15) || ((b=b-3)<4); b+a",asMap("a",3,"b",4,"c",5));
        testEvaluation("((a=a+1)>1) || ((b=b-3)<4); b+a",asMap("a",3,"b",4,"c",5));
        testEvaluation("((a=a+1)>1) || ((b=b-3)<-1) || (c=c*10)>7; b+a*c",asMap("a",3,"b",4,"c",5));
        testEvaluation("((a=a+1)>15) || ((b=b-3)<-1) || (c=c*10)>7; b+a*c",asMap("a",3,"b",4,"c",5));
        testEvaluation("((a=a+1)>1) || ((b=b-3)<1000) || (c=c*10)>70000; b+a*c",asMap("a",3,"b",4,"c",5));
        
        testEvaluation("(a++ > 13) && (a++ > 5)",asMap("a",3,"b",4,"c",5));
        
        testEvaluation("++b>5;",asMap("b", 5),asMap("b", 5));
        testEvaluation("++b<=5;",asMap("b", 5),asMap("b", 5));
    }
    
    //post ++
    @Test
    public void testBF() throws Exception{
        // plain post increment
//        testEvaluation("b++;b",asMap("vvvv",new TestObject(), "b", 2.5));
        // post increment on deep property path
//        testEvaluation("vvvv.object2.flt++;vvvv.object2.flt",asMap("vvvv",new TestObject()),asMap("vvvv",new TestObject()));
//        testEvaluation("vvvv.list[2][2][2]++;vvvv.list[2][2][2]",asMap("vvvv",new TestObject()),asMap("vvvv",new TestObject()));
        // post increment on an expression
//        testEvaluation("(((((vvvv.list[2])[2][2]))))++;vvvv.list[2][2][2]",asMap("vvvv",new TestObject()),asMap("vvvv",new TestObject()));
        // check post increment behavior
        testEvaluation("d = b + b++;d++;",asMap("b", 5),asMap("b", 5));
        testEvaluation("d = b++ + b;d++;",asMap("b", 5),asMap("b", 5));
        testEvaluation("d = b + (3 + b++);d++;",asMap("b", 5),asMap("b", 5));
        testEvaluation("d=b++ + b++ + b++ + b; d + b;",asMap("b", 5),asMap("b", 5));
        testEvaluation("d=b + b++ * b++ + b++;d + b;",asMap("b", 5),asMap("b", 5));
        testEvaluation("d=b + b++ + b + b++ + b++;d + b;",asMap("b", 5),asMap("b", 5));
        testEvaluation("d=b + b++ + b++ + b++;d + b;",asMap("b", 5),asMap("b", 5));
        testEvaluation("d=vvvv.object2.flt + ++vvvv.object2.flt + vvvv.object2.flt++;vvvv.object2.flt+d;"
                ,asMap("vvvv", new TestObject()),asMap("vvvv", new TestObject()));
        testEvaluation("d=vvvv.list3[2] + ++vvvv.list3[2] + vvvv.list3[2]++;vvvv.list3[2]+d;"
                ,asMap("vvvv", new TestObject()),asMap("vvvv", new TestObject()));
        
        testEvaluation("b++>5;",asMap("b", 5),asMap("b", 5));
        testEvaluation("b++<=5;",asMap("b", 5),asMap("b", 5));
    }
    
    //post --
    @Test
    public void testBG() throws Exception{
        
        testEvaluation("b--;b",asMap("vvvv",new TestObject(), "b", 2.5));
        
        testEvaluation("vvvv.object2.flt--;vvvv.object2.flt",asMap("vvvv",new TestObject()),asMap("vvvv",new TestObject()));
        
        testEvaluation("vvvv.list[2][2][2]--;vvvv.list[2][2][2]",asMap("vvvv",new TestObject()),asMap("vvvv",new TestObject()));
        
        testEvaluation("(((((vvvv.list[2])[2][2]))))--;vvvv.list[2][2][2]",asMap("vvvv",new TestObject()),asMap("vvvv",new TestObject()));
        
        //check post decrement behavior
        testEvaluation("d = b + b--;d--;",asMap("b", 5),asMap("b", 5));
        testEvaluation("d = b-- + b;d--;",asMap("b", 5),asMap("b", 5));
        testEvaluation("d = b + (3 + b--);d--;",asMap("b", 5),asMap("b", 5));
        testEvaluation("d=b-- + b-- + b-- + b;d + b;",asMap("b", 5),asMap("b", 5));
        testEvaluation("d=b + b-- * b-- + b--;d + b;",asMap("b", 5),asMap("b", 5));
        testEvaluation("d=b + b-- + b + b-- + b--;d + b;",asMap("b", 5),asMap("b", 5));
        testEvaluation("d=b + b-- + b-- + b--;d + b;",asMap("b", 5),asMap("b", 5));
        testEvaluation("d=vvvv.object2.flt + vvvv.object2.flt--;vvvv.object2.flt+d;"
                ,asMap("vvvv", new TestObject()),asMap("vvvv", new TestObject()));
        testEvaluation("d=vvvv.list3[2] + vvvv.list3[2]--;vvvv.list3[2]+d;"
                ,asMap("vvvv", new TestObject()),asMap("vvvv", new TestObject()));
    }
    
    //prefix ++
    @Test
    public void testBH() throws Exception{
        
        testEvaluation("++b;b",asMap("vvvv",new TestObject(), "b", 2.5));

        testEvaluation("++vvvv.object2.flt;vvvv.object2.flt",asMap("vvvv",new TestObject()),asMap("vvvv",new TestObject()));
        
        testEvaluation("++vvvv.list[2][2][2];vvvv.list[2][2][2]",asMap("vvvv",new TestObject()),asMap("vvvv",new TestObject()));

        testEvaluation("++(((((vvvv.list[2])[2][2]))));vvvv.list[2][2][2]",asMap("vvvv",new TestObject()),asMap("vvvv",new TestObject()));

        //check prefix increment behavior
        testEvaluation("d = b + ++b;++d;",asMap("b", 5),asMap("b", 5));
        testEvaluation("d = ++b + b;++d;",asMap("b", 5),asMap("b", 5));
        testEvaluation("d = b + (3 + ++b);++d;",asMap("b", 5),asMap("b", 5));
        testEvaluation("d=++b + ++b + ++b + b;d + b;",asMap("b", 5),asMap("b", 5));
        testEvaluation("d=b + ++b * ++b + ++b;d + b;",asMap("b", 5),asMap("b", 5));
        testEvaluation("d=b + ++b + b + ++b + ++b;d + b;",asMap("b", 5),asMap("b", 5));
        testEvaluation("d=b + ++b + ++b + ++b;d + b;",asMap("b", 5),asMap("b", 5));
        testEvaluation("d=vvvv.object2.flt + ++vvvv.object2.flt;vvvv.object2.flt+d;"
                ,asMap("vvvv", new TestObject()),asMap("vvvv", new TestObject()));
        testEvaluation("d=vvvv.list3[2] + ++vvvv.list3[2];vvvv.list3[2]+d;"
                ,asMap("vvvv", new TestObject()),asMap("vvvv", new TestObject()));
        testEvaluation("d=b + ++b + ++b + b; d + b;",asMap("b", 5),asMap("b", 5));
    }
    
    @Test
    public void testBH1() throws Exception{
        //parentheses surrounded property reference expression
        testEvaluation("a=(test.test3).bb+(test.test3).bb++ + (test.test3).bb-- + 3", asMap("test",new Test2()));
    }

    //prefix --
    @Test
    public void testBI() throws Exception{

        testEvaluation("--b;b",asMap("vvvv",new TestObject(), "b", 2.5));
        
        testEvaluation("--vvvv.object2.flt;vvvv.object2.flt",asMap("vvvv",new TestObject()),asMap("vvvv",new TestObject()));
        
        testEvaluation("--vvvv.list[2][2][2];vvvv.list[2][2][2]",asMap("vvvv",new TestObject()),asMap("vvvv",new TestObject()));

        testEvaluation("--(((((vvvv.list[2])[2][2]))));vvvv.list[2][2][2]",asMap("vvvv",new TestObject()),asMap("vvvv",new TestObject()));
       
        //check prefix decrement behavior
        testEvaluation("d = b + --b;--d;",asMap("b", 5),asMap("b", 5));
        testEvaluation("d = --b + b;--d;",asMap("b", 5),asMap("b", 5));
        testEvaluation("d = b + (3 + --b);--d;",asMap("b", 5),asMap("b", 5));
        testEvaluation("d=--b + --b + --b + b;d + b;",asMap("b", 5),asMap("b", 5));
        testEvaluation("d=b + --b * --b + --b;d + b;",asMap("b", 5),asMap("b", 5));
        testEvaluation("d=b + --b + b + --b + --b;d + b;",asMap("b", 5),asMap("b", 5));
        testEvaluation("d=b + --b + --b + --b;d + b;",asMap("b", 5),asMap("b", 5));
        testEvaluation("d=vvvv.object2.flt + --vvvv.object2.flt;vvvv.object2.flt+d;"
                ,asMap("vvvv", new TestObject()),asMap("vvvv", new TestObject()));
        testEvaluation("d=vvvv.list3[2] + --vvvv.list3[2];vvvv.list3[2]+d;"
                ,asMap("vvvv", new TestObject()),asMap("vvvv", new TestObject()));
    }
    
    //unary plus/minus
    @Test
    public void testBJ() throws Exception{
        
        testEvaluation("+a + +b + -c + -1.0- -3 - -a -b*+a*-b/-b/-1.0;",asMap("a",3,"b",4,"c",5));
        //consecutive unary plua/minus
        testEvaluation("+ + + + a - - - - - b + + ++c - - --b * - - - - -a*b*c;",asMap("a",3,"b",4,"c",5));
        testEvaluation("+ + + + 1.20 - - - - -3.0 + + +9.0 - - --a * - - - - -c;",asMap("a",3,"b",4,"c",5));
        testEvaluation("+ + + + (a+b+c) - - - - -(a+b+c) + + +(a+b+c) - - -(a+b+c) * - - - - -(a+b+c);",asMap("a",3,"b",4,"c",5));
    }
    
    //unary logical not
    @Test
    public void testBK() throws Exception{
        testEvaluation("!(a>3) && a==4 || false;",asMap("a",3,"b",4,"c",5));
        //consecutive logical not
        testEvaluation("!!!!!(a>3) && !!!(a==4) || !!!false || !!!true",asMap("a",3,"b",4,"c",5));
    }
    
    //unary bitwise NOT
    @Test
    public void testBL() throws Exception{
        testEvaluation("~a+~b*~c/~a;",asMap("a",3,"b",4,"c",5));
        //consecutive bitwise not
        testEvaluation("~~~~3777 + ~~- - -(a+b+c)& + + + ~ + ~(b+ - - a * ~b) / - - - ~- - -a;"
                ,asMap("a",3333,"b",44444,"c",55555));
    }
    
    //mod
    @Test
    public void testBM() throws Exception{
        //positive number mod positive number
        testEvaluation("a % b",asMap("a",3,"b",4,"c",5));
        testEvaluation("a % 3.333",asMap("a",3,"b",4,"c",5));
        testEvaluation("(a+b+c) % 13",asMap("a",3,"b",4,"c",5));
        testEvaluation("13 % (a+b+c)",asMap("a",3,"b",4,"c",5));
        
        //negative number mod negative number
        testEvaluation("-a % -b",asMap("a",3,"b",4,"c",5));
        testEvaluation("-a % - - -3.333",asMap("a",3,"b",4,"c",5));
        testEvaluation("-(a+b+c) % -13",asMap("a",3,"b",4,"c",5));
        testEvaluation("- - -13 % -(a+b+c)",asMap("a",3,"b",4,"c",5));
        
        //positive number mod negative number
        testEvaluation("a % - --b",asMap("a",3,"b",4,"c",5));
        testEvaluation("a % -3.333",asMap("a",3,"b",4,"c",5));
        testEvaluation("(a+b+c) % - - -13*b*c",asMap("a",3,"b",4,"c",5));
        testEvaluation("13 % - - -(a+b+c)",asMap("a",3,"b",4,"c",5));
        
        //negative number mod positive number
        testEvaluation("-a % --b",asMap("a",3,"b",4,"c",5));
        testEvaluation("-a % 3.333",asMap("a",3,"b",4,"c",5));
        testEvaluation("-(a+b+c) % 13",asMap("a",3,"b",4,"c",5));
        testEvaluation("- - -13 % (a+b+c)",asMap("a",3,"b",4,"c",5));
        
        //0 mod positive number
        testEvaluation("0 % --b",asMap("a",3,"b",4,"c",5));
        testEvaluation("0 % 3.333",asMap("a",3,"b",4,"c",5));
        testEvaluation("0 % 13",asMap("a",3,"b",4,"c",5));
        testEvaluation("0 % (a+b+c)",asMap("a",3,"b",4,"c",5));
        
        //0 mod negative number
        testEvaluation("0 % - --b",asMap("a",3,"b",4,"c",5));
        testEvaluation("0 % - 3.333",asMap("a",3,"b",4,"c",5));
        testEvaluation("0 % - - - - - 13",asMap("a",3,"b",4,"c",5));
        testEvaluation("0 % - - -(a+b+c)",asMap("a",3,"b",4,"c",5));
        
        //check result when division is exact
        testEvaluation("-a % a++",asMap("a",3,"b",4,"c",5));
        testEvaluation("a % -a++",asMap("a",3,"b",4,"c",5));
        testEvaluation("-a % -a++",asMap("a",3,"b",4,"c",5));
        testEvaluation("a % a++",asMap("a",3,"b",4,"c",5));
        testEvaluation("a % a",asMap("a",3,"b",4,"c",5));
    }
    
    //shift left / right
    @Test
    public void testBN() throws Exception{
        //negative number and positive number
        //negative number and negative number
        //positive number and positive number
        //positive number and negative number
        testEvaluation("(a+b+c*a) >> - - -3",asMap("a",3,"b",4,"c",5));
        testEvaluation("(a+b+c*a) >> - - -1",asMap("a",3,"b",4,"c",5));
        testEvaluation("(a+b+c*a) >> - - -11010101010",asMap("a",3,"b",4,"c",5));
        testEvaluation("(a+b+c*a) >> - - -a*b",asMap("a",3,"b",4,"c",5));
        testEvaluation("(a+b+c*a) >> a*b",asMap("a",3,"b",4,"c",5));
        testEvaluation("-(a+b+c*a) >> - -a*b",asMap("a",3,"b",4,"c",5));
        testEvaluation("-(a+b+c*a) >> a",asMap("a",3,"b",4,"c",5));
        
        testEvaluation("(a+b+c*a) << - - -3",asMap("a",3,"b",4,"c",5));
        testEvaluation("(a+b+c*a) << - - -1",asMap("a",3,"b",4,"c",5));
        testEvaluation("(a+b+c*a) << - - -11010101010",asMap("a",3,"b",4,"c",5));
        testEvaluation("(a+b+c*a) << - - -a*b",asMap("a",3,"b",4,"c",5));
        testEvaluation("(a+b+c*a) << a*b",asMap("a",3,"b",4,"c",5));
        testEvaluation("-(a+b+c*a) << - -a*b",asMap("a",3,"b",4,"c",5));
        testEvaluation("-(a+b+c*a) << a",asMap("a",3,"b",4,"c",5));
    }
    
    @Test
    public void testBO() throws Exception{
        //return a parentheses structure
        testEvaluation("(a+b+c);",asMap("a",3,"b",4,"c",5));
        //hex and binary number literal
        Assert.assertEquals(
                ((BigDecimal)evaluateMine("(0xABCDEF + 0b111010101+7777.5);",asMap("a",3,"b",4,"c",5))).toPlainString(),
                new BigDecimal(0xABCDEF + 0b111010101+7777.5).toPlainString());
        Assert.assertEquals(
                ((BigDecimal)evaluateMine("(0xAbcdef + 0b111010101+7777.5);",asMap("a",3,"b",4,"c",5))).toPlainString(),
                new BigDecimal(0xABCDEF + 0b111010101+7777.5).toPlainString());
        Assert.assertEquals(
                ((BigDecimal)evaluateMine("(0XAbCdEF + 0B111010101+7777.5);",asMap("a",3,"b",4,"c",5))).toPlainString(),
                new BigDecimal(0xABCDEF + 0b111010101+7777.5).toPlainString());
        Assert.assertEquals(
                ((BigDecimal)evaluateMine("(0XAbCdEF + 0B1__1__0+7777.5);",asMap("a",3,"b",4,"c",5))).toPlainString(),
                new BigDecimal(0xABCDEF + 0B1__1__0+7777.5).toPlainString());
        
        assertScriptException(new ScriptAssertion("(0XAbCdEF + 0B1__1__0___+7777.5);"), Script.class, 21);
        assertScriptException(new ScriptAssertion("(0XAbCdEF + 0B_1__1__0+7777.5);"), Script.class, 21);
        assertScriptException(new ScriptAssertion("(0XAbCdEF.CCC + 0B1__1__0+7777.5);"), AffixBinaryOperator.class, 1);
    }
    
    @Test
    public void testBQ() throws Exception{
        //refer to a class with implicit full qualified name
        Object obj = new Script(
                "a+b+c+Constants.constant1")
                .compile()
                .evaluate(asMap("a",3,"b",4,"c",5,"vvvv",new TestObjectV()))+"";
        Assert.assertEquals(obj, "11123");
    }
    
    @Test
    public void testBR() throws Exception{
        //invalid if/else structure
        assertScriptException(new ScriptAssertion("{if(a+b>3){}else if(a>15){a++;}else {a++;}}if(b+c==5){b++;}"));
        assertScriptException(new ScriptAssertion("if(a+b>3){}else if(a>15){a++;}else if(a>15){a++;}else{b++;}"));
        assertScriptException(new ScriptAssertion("if(a+b>3){}else{++a;}else if(a>15){a++;}"), Script.class, 22);
        assertScriptException(new ScriptAssertion("if(a+b>3){}else{++a;}else{a++;}"), Script.class, 22);
        assertScriptException(new ScriptAssertion("if(a+b>3){}else{++a;}{a++;}"));
    }
    
    @Test
    public void testBS() throws Exception{
        //return operator
        testEvaluation2("if(a+b>3){++a;++b;return ((a*b)+3-4);} a=a+3*b;",asMap("a",15,"b",16));
        testEvaluation2("if(a+b>3)return a+b",asMap("a",15,"b",16));
        testEvaluation2("if(a+b>3000){++a;++b;return a*b;} else return 3;",asMap("a",15,"b",16));
        testEvaluation2("if(a+b>3000){++a;++b;return a*b;} return 3;",asMap("a",15,"b",16));
        testEvaluation2("if(a+b>3){++a;++b;return a*b;} a=a+3*b;",asMap("a",15,"b",16));
        testEvaluation2("a=a+1;{return(a=a+3*b);}a=a++;",asMap("a",15,"b",16));
        testEvaluation2("return(a=a+3*b)",asMap("a",15,"b",16));
        testEvaluation2("return(a=a+3*b);",asMap("a",15,"b",16));
        assertScriptException(new ScriptAssertion("a + return(a=a+3*b);"), ReturnOperator.class, 0);
    }
    
    //concurrency test
    @Test
    public void testBT() throws Exception{
        String script = "+ + + + (a++ + ++a + b + c) - - - - -(++a + b+c) + +"
                + " +(a+ --b - b-- - c--) - - -(a+b+c) * - - - - -(a+b+c);if(a+b>>3>0){a++;}else{b++;}a+b*c/d&0^5>>7;";
        testEvaluation(script,asMap("a",3,"b",4,"c",5,"d",20));
        testEvaluationConcurrently(script,asMap("a",3,"b",4,"c",5,"d",20),10,20);
    }
    
    //missed instructions, branches
    @Test
    public void testBU() throws Exception{
        
        //esacpe sequences in string
        testEvaluation("a+b+c+'\\'\\b\\f\\n\\r\\t \\\\ abcdef'", asMap("a","abc","b","bac","c","cab"));
        assertScriptException(new ScriptAssertion("'"), Script.class, 2);
        assertScriptException(new ScriptAssertion("'abcdef"), Script.class, 2);
        assertScriptException(new ScriptAssertion("'abcdef"+"\\"+"\"'"), Script.class, 11);
        
        Script script = new Script("a+b").compile();
        Assert.assertTrue(script == script.compile());
        
        try {
            new Script("a+b").evaluate(null);
            Assert.fail();
        } catch (IllegalStateException e) {
        }
        
        assertScriptException(new ScriptAssertion("abcde`+defghi"), Script.class, 21);
        
        assertScriptException(new ScriptAssertion(null));

        try {
            @SuppressWarnings("rawtypes")
            Constructor cons = Operators.class.getDeclaredConstructor();
            cons.setAccessible(true);
            cons.newInstance();
            Assert.fail();
        } catch (Exception e) {
        }
        
        testEvaluation("a?3:4", asMap("a",true,"b",false));
        
        assertScriptException(new ScriptAssertion("null ?  "), Operator.class, 3);
        assertScriptException(new ScriptAssertion("true ? a"), QuestionOperator.class, 2);
        assertScriptException(new ScriptAssertion("null ? a : b"), QuestionOperator.class, 1);
        try {
            evaluateMine("1+2 ? a : b", asMap("a",3,"b",4));
            Assert.fail();
        } catch (Exception e) {
            testException(e, QuestionOperator.class, 1);
        }
        
        try {
            evaluateMine("if(1+2){return a++;}", asMap("a",3,"b",4));
            Assert.fail();
        } catch (Exception e) {
            testException(e, IfOperator.class, 2);
        }
        
        assertScriptException(new ScriptAssertion("if(1+2){a++;}else b a c"), Script.class, 6);
        
        try {
            evaluateMine("else a++;", asMap("a",3,"b",4));
            Assert.fail();
        } catch (Exception e) {
            testException(e, Script.class, 22);
        }
        try {
            evaluateMine("1+2+a else a++;", asMap("a",3,"b",4));
            Assert.fail();
        } catch (Exception e) {
            testException(e, Script.class, 23);
        }
        
        try {
            evaluateMine("str1=null;map1[str2]", asMap("map1",asMap("abc","def","ttt",3),"map2",false));
            Assert.fail();
        } catch (Exception e) {
            testException(e, BracketOperator.class, 3);
        }
        testEvaluation("str1='map';str2=str1+1;map1[str2]", asMap("map1",asMap("abc","def","ttt",3),"map2",false));
        
        try {
            evaluateMine("!(a+b)", asMap("a",1,"b",2));
            Assert.fail();
        } catch (Exception e) {
            testException(e, LogicalNotOperator.class, 4);
        }
        
        Assert.assertEquals(new Operators.ColonOperator().arity(), 1);
        try {
            new Operators.ColonOperator().reorder0(null, 0);
            Assert.fail();
        } catch (UnsupportedOperationException e) {
        }
        try {
            new Operators.ColonOperator().eval(null);
            Assert.fail();
        } catch (UnsupportedOperationException e) {
        }
        
        new Operators.ColonOperator().checkOperands(null, -1);
        
        try {
            evaluateMine("++(a+b)", asMap("a",1,"b",2));
            Assert.fail();
        } catch (Exception e) {
            testException(e, Operator.class, 0);
        }
        
        try {
            evaluateMine("++a", asMap("a","true","b",2));
            Assert.fail();
        } catch (Exception e) {
            testException(e, Operator.class, 1);
        }
        
        try {
            evaluateMine("3*(a+'abcdef')", asMap("a","true","b",2));
            Assert.fail();
        } catch (Exception e) {
            testException(e, Operator.class, 2);
        }
        
        evaluateMine("3+a+b", asMap("a",new BigInteger("3"),"b",2));
        
        try {
            evaluateMine("+a", asMap("a",false));
            Assert.fail();
        } catch (Exception e) {
            testException(e, Operator.class, 2);
        }
        
        try {
            evaluateMine("a>3", asMap("a",null));
            Assert.fail();
        } catch (Exception e) {
            testException(e, RelationalOperator.class, 1);
        }
        
        try {
            evaluateMine("a>b", asMap("a",new Object(),"b",new Object()));
            Assert.fail();
        } catch (Exception e) {
            testException(e, RelationalOperator.class, 1);
        }
        
        try {
            evaluateMine("a & b", asMap("a",true,"b",3));
            Assert.fail();
        } catch (Exception e) {
            testException(e, Operator.class, 2);
        }
        
        try {
            evaluateMine("a && b", asMap("a",true,"b",3));
            Assert.fail();
        } catch (Exception e) {
            testException(e, LogicalOperator.class, 0);
        }
        
        {
            Context ctx = new Context(asMap("a",3,"b",4));
            Set<Map.Entry<String,Object>> str = ctx.entrySet();
            Assert.assertTrue(str.size() == 2);
        }
        {
            Context ctx = new Context(asMap("a",new TestObject(),"b",new Date(),"c",null,"d"
                    ,Class.forName("TestDefaultObject").newInstance()));
            List<String> names = ctx.getImplicitPackageNames();
            Assert.assertTrue(names == ctx.getImplicitPackageNames());
            //io.github.zhtmf.script.ScriptTest.
            Assert.assertEquals(names.size(), 1);
            Assert.assertTrue(names.contains("io.github.zhtmf.script.test.test1"));
        }
        {
            Object ret = Identifier.of("a")
                    .add(Identifier.of("length")).dereference(asMap("a","abc"));
            Assert.assertEquals(ret.toString(),"3");
        }
        {
            Identifier.of("1").set(asMap("a","def"), "defghi");
        }
        try {
            Identifier.of("abc").add(Identifier.of("1")).set(asMap("abc",new TestObject()), "def");
        } catch (Exception e) {
            testException(e, Identifier.class, 4);
        }
        try {
            Identifier.of("abc").add(Identifier.of("xxxxxx")).set(asMap("abc",new TestObject()), "def");
        } catch (Exception e) {
            testException(e, Identifier.class, 6);
        }
        
        testEvaluation("test.b=1;test.s=1;test.i=1;test.l=1;test.f=1;test.d=1;"
                + "test.b+test.s+test.i+test.l+test.f+test.d", asMap("test",new Test2()));
        testEvaluation("test.b1=1;test.s1=1;test.i1=1;test.l1=1;test.f1=1;test.d1=1;"
                + "test.b1+test.s1+test.i1+test.l1+test.f1+test.d1", asMap("test",new Test2()));
        assertEquals(evaluateMine("test.bi=333", asMap("test",new Test2()))+"", "333");
    }
    
    @Test
    public void testBU1() throws Exception{
        assertScriptException(new ScriptAssertion("a+0abcdef"), Script.class, 6);
    }
    
    @Test
    public void testBU2() throws Exception{
        evaluateMine("if(a+b>10){a--;}else {}",asMap("a",1,"b",2));
    }
    
    @Test
    public void testBU3() throws Exception{
        Object obj = evaluateMine("java.lang.System.currentTimeMillis",asMap("a",1,"b",2));
        assertTrue(obj instanceof Number);
    }
    
    @Test
    public void testBU4() throws Exception{
        try {
            evaluateMine("a.b=3;",asMap("a",null));
            fail();
        } catch (Exception e) {
            testException(e, Identifier.class, 8);
        }
        try {
            evaluateMine("a=java.lang.System.abc;a.b=3;",asMap("a",null));
            fail();
        } catch (Exception e) {
            testException(e, Identifier.class, 8);
        }
    }
    
    @Test
    public void testBV() throws Exception{
        //check parameter values
        //test case for boolean
        testEvaluation("obj.testBoolean(true|false,'abc'.length()>2) && true", asMap("obj",new TestMethodCall()));
        //test case for number
        assertEquals(((BigDecimal)evaluateMine("obj.testNumber(3.3,5.2) + 1"
                , asMap("obj",new TestMethodCall()))).stripTrailingZeros().toString(), "9.5");
        assertEquals(((BigDecimal)evaluateMine("obj.testNumber2(3.3,5.2) + 1"
                , asMap("obj",new TestMethodCall()))).stripTrailingZeros().toString(), "9.5");
        assertEquals(((BigDecimal)evaluateMine("obj.testNumber3(3.3,5.2) + 1"
                , asMap("obj",new TestMethodCall()))).stripTrailingZeros().toString(), "9");
        assertEquals(((BigDecimal)evaluateMine("obj.testNumber4(3.3,5.2) + 1"
                , asMap("obj",new TestMethodCall()))).stripTrailingZeros().toString(), "9");
        assertEquals(((BigDecimal)evaluateMine("obj.testNumber5(3.3,5.2) + 1"
                , asMap("obj",new TestMethodCall()))).stripTrailingZeros().toString(), "9");
        assertEquals(((BigDecimal)evaluateMine("obj.testNumber6(3.3,5.2) + 1"
                , asMap("obj",new TestMethodCall()))).stripTrailingZeros().toString(), "9");
        assertEquals(((BigDecimal)evaluateMine("obj.testNumber8(3.3,5.2) + 1"
                , asMap("obj",new TestMethodCall()))).stripTrailingZeros().toString(), "9.5");
        assertEquals(((BigDecimal)evaluateMine("obj.testNumber9(3.3,5.2) + 1"
                , asMap("obj",new TestMethodCall()))).stripTrailingZeros().toString(), "9.5");
        assertEquals(((BigDecimal)evaluateMine("obj.testNumber10(3.3,5.2) + 1"
                , asMap("obj",new TestMethodCall()))).stripTrailingZeros().toString(), "9");
        assertEquals(((BigDecimal)evaluateMine("obj.testNumber11(3.3,5.2) + 1"
                , asMap("obj",new TestMethodCall()))).stripTrailingZeros().toString(), "9");
        assertEquals(((BigDecimal)evaluateMine("obj.testNumber12(3.3,5.2) + 1"
                , asMap("obj",new TestMethodCall()))).stripTrailingZeros().toString(), "9");
        assertEquals(((BigDecimal)evaluateMine("obj.testNumber13(3.3,5.2) + 1"
                , asMap("obj",new TestMethodCall()))).stripTrailingZeros().toString(), "9");
        assertEquals(((BigDecimal)evaluateMine("obj.testNumber15(3.3,5.2) + 1"
                , asMap("obj",new TestMethodCall()))).stripTrailingZeros().toString(), "9.5");
        assertEquals(((BigDecimal)evaluateMine("obj.testNumber16(3.3,5.2) + 1"
                , asMap("obj",new TestMethodCall()))).stripTrailingZeros().toString(), "9");
        //test case for null
        testEvaluation("obj.testNULL(null, 3+4/2) && true", asMap("obj",new TestMethodCall()));
        //call protected methods
        testEvaluation("obj.testProtected(null, 3+4/2) && true", asMap("obj",new TestMethodCall()));
        //test case for string
        testEvaluation("obj.testString('abcdef','a'+'b'+'c') + 11", asMap("obj",new TestMethodCall()));
        //method call as an operand
        testEvaluation("obj.asOperand('abcdef'.length(),'a'.length()+3.456) + 11", asMap("obj",new TestMethodCall()));
        //parameter is another method call
        assertEquals(evaluateMine("obj.anotherMethodCall('abcdef'.length(),'a'.length()+3.456)"
                , asMap("obj",new TestMethodCall())).toString(), "4");
        //method call with no parameter
        assertEquals(evaluateMine("obj.noParameter()", asMap("obj",new TestMethodCall())).toString(), "0");
        //parameter is another expression
        assertEquals(evaluateMine("obj.multipleParameters(1+3,2*4/5+6-7^3,'345'+str1+map1['t'+'tt'],false)"
                , asMap("obj",new TestMethodCall(),"str1","aaa","map1",asMap("ttt","111"))).toString(), "2");
        //method call with one parameter
        assertEquals(evaluateMine("obj.oneParameter(3)", asMap("obj",new TestMethodCall())).toString(), "1");
        //method call with multiple parameters
        assertEquals(evaluateMine("obj.multipleParameters(1,2,'3456',false)", asMap("obj",new TestMethodCall())).toString(), "2");
        //parameter is another () expression
        assertEquals(evaluateMine("obj.nestingParentheses((1+333),(2*4)+5,'3456',((false))|true&((false)))"
                , asMap("obj",new TestMethodCall())).toString(), "3");
    }
    
    @Test
    public void testBV1() throws Exception{
        //method overloading
        //1.BigDecimal over double
        assertEquals(evaluateMine("obj.testOverloading1(-3.4) + 1", asMap("obj",new TestMethodCall())).toString(), "2");
        //2.double over Double
        assertEquals(evaluateMine("obj.testOverloading2(-3.4) + 1", asMap("obj",new TestMethodCall())).toString(), "2");
        //3.Double over float
        assertEquals(evaluateMine("obj.testOverloading3(-3.4) + 1", asMap("obj",new TestMethodCall())).toString(), "2");
        //4.float over Float
        assertEquals(evaluateMine("obj.testOverloading4(-3.4) + 1", asMap("obj",new TestMethodCall())).toString(), "2");
        //5.Float over BigInteger
        assertEquals(evaluateMine("obj.testOverloading5(-3.4) + 1", asMap("obj",new TestMethodCall())).toString(), "2");
        //6.BigInteger over long
        assertEquals(evaluateMine("obj.testOverloading6(-3.4) + 1", asMap("obj",new TestMethodCall())).toString(), "2");
        //7.long over Long
        assertEquals(evaluateMine("obj.testOverloading7(-3.4) + 1", asMap("obj",new TestMethodCall())).toString(), "2");
        //8.Long over int
        assertEquals(evaluateMine("obj.testOverloading8(-3.4) + 1", asMap("obj",new TestMethodCall())).toString(), "2");
        //9.int over Integer
        assertEquals(evaluateMine("obj.testOverloading9(-3.4) + 1", asMap("obj",new TestMethodCall())).toString(), "2");
        //10.Integer over short
        assertEquals(evaluateMine("obj.testOverloading10(-3.4) + 1", asMap("obj",new TestMethodCall())).toString(), "2");
        //11.short over Short
        assertEquals(evaluateMine("obj.testOverloading11(-3.4) + 1", asMap("obj",new TestMethodCall())).toString(), "2");
        //12.Short over byte
        assertEquals(evaluateMine("obj.testOverloading12(-3.4) + 1", asMap("obj",new TestMethodCall())).toString(), "2");
        //13.byte over Byte
        assertEquals(evaluateMine("obj.testOverloading13(-3.4) + 1", asMap("obj",new TestMethodCall())).toString(), "2");
        
        //choosing between multiple methods
        //find the max specific
        assertEquals(evaluateMine("obj.maxSpecific1(-3,-4) + 1", asMap("obj",new TestMethodCall())).toString(), "7");
        
        //ambiguous call
        assertEvaluationException("obj.maxSpecific2(-3,-4) + 1", asMap("obj",new TestMethodCall()), Identifier.class, 11);
    }
    
    @Test
    public void testBV2() throws Exception{
        //calling method on null reference
        assertEvaluationException("a=null;a.length()"
                , asMap("obj",new TestMethodCall())
                , Identifier.class, 9);
        
        //mismatch
        assertEvaluationException("obj.nullMismatch(null)"
                , asMap("obj",new TestMethodCall())
                , Identifier.class, 12);
        assertEvaluationException("obj.stringMismatch('3')"
                , asMap("obj",new TestMethodCall())
                , Identifier.class, 12);
        assertEvaluationException("obj.boolMismatch(false)"
                , asMap("obj",new TestMethodCall())
                , Identifier.class, 12);
        assertEvaluationException("obj.numberMismatch(3)"
                , asMap("obj",new TestMethodCall())
                , Identifier.class, 12);
        
        //private method excluded
        assertEvaluationException("obj.privateMethod(3)"
                , asMap("obj",new TestMethodCall())
                , Identifier.class, 12);
        
        //method itself throws exception
        assertEvaluationException("obj.exception('3')"
                , asMap("obj",new TestMethodCall())
                , Identifier.class, 10);
    }
    
    @Test
    public void testBV3() throws Exception{
        //call static method
        assertEvaluationException("obj.static1(3)", asMap("obj",new TestMethodCall()), Identifier.class, 12);
        assertEquals(evaluateMine("io.github.zhtmf.script.test.test1.TestMethodCall.static1(3)", asMap("obj",new TestMethodCall())).toString(), "6");
        assertEquals(evaluateMine("java.lang.Math.floor(3.456)", asMap("obj",new TestMethodCall())).toString(), "3");
        
        //lookup static methods when calling method of a class object,
        //lookup instance methods when calling method of an non-class object
        assertEvaluationException("obj.static2('3')"
                , asMap("obj",new TestMethodCall())
                , Identifier.class, 12);
        assertEvaluationException("io.github.zhtmf.script.test.test1.TestMethodCall.static2(3)"
                , asMap("obj",new TestMethodCall())
                , Identifier.class, 12);
        assertEvaluationException("obj.static2(3)"
                , asMap("obj",TestMethodCall.class)
                , Identifier.class, 12);
    }
    
    private void testEvaluation2(String script, Map<String,Object> context) {
        Object mine = null;
        Object nashornResult;
        Exception ex = null;
        try {
            mine = evaluateMine(script,context);
        } catch (Exception e) {
            ex = e;
        }
        try {
            nashornResult = evaluateNashorn("function fff(){"+script+"}fff();", context);
        } catch (ScriptException e) {
            throw new Error(e);
        }
        compareScriptResults(ex, script, mine, nashornResult);
    }
    
    private void testEvaluation(String script, Map<String,Object> context, Map<String,Object> context2) {
        Object mine = null;
        Object nashornResult;
        Exception ex = null;
        try {
            mine = evaluateMine(script,context);
        } catch (Exception e) {
            ex = e;
        }
        try {
            nashornResult = evaluateNashorn(script, context2);
        } catch (ScriptException e) {
            throw new Error(e);
        }
        compareScriptResults(ex, script, mine, nashornResult);
    }
    
    private void testEvaluation(String script, Map<String,Object> context) {
        testEvaluation(script, context, context);
    }
    
    private void testEvaluationConcurrently(String script, Map<String,Object> context, int thread, int count) throws ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(thread);
        class Task implements Callable<Void>{
            @Override
            public Void call() {
                for(int i=0;i<count;++i) {
                    testEvaluation(script, new HashMap<>(context), new HashMap<>(context));
                }
                return null;
            }
        }
        List<Task> tasks = new ArrayList<Task>();
        for(int i=0;i<thread;++i) {
            tasks.add(new Task());
        }
        try {
            List<Future<Void>> results = executor.invokeAll(tasks);
            for(Future<Void> future : results) {
                future.get();
            }
        } catch (InterruptedException e) {
        }
    }
    
    private void compareScriptResults(Exception ex, String script, Object mine, Object nashornResult) {
        if(ex!=null) {
            System.out.println("nashorn result: "+nashornResult);
            System.out.println("mine throws exception \n" + printMessage(ex));
            Assert.fail("for script "+script+" error: \n"+printMessage(ex));
        }
        if(nashornResult instanceof Number) {
            nashornResult = new BigDecimal(nashornResult.toString()).stripTrailingZeros().toPlainString();
        }
        if(mine instanceof Number) {
            mine = new BigDecimal(mine.toString()).stripTrailingZeros().toPlainString();
        }
        Assert.assertEquals("for script "+script+" error: \n",nashornResult, mine);
    }
    
    private static Object evaluateMine(String script, Map<String,Object> context) throws Exception {
        return mine.eval(script, new SimpleBindings(context));
    }
    
    private static Object evaluateNashorn(String script, Map<String,Object> context) throws ScriptException {
        return nashorn.eval(script, new SimpleBindings(context));
    }
    
    private static void assertEvaluationException(String script, Map<String,Object> context,Class<?> site, int ordinal) {
        try {
            mine.eval(script, new SimpleBindings(context));
            fail();
        } catch (Exception e) {
            testException(e, site, ordinal);
        }
    }
    
    private static void assertScriptException(ScriptAssertion script) {
        try {
            script
            
            .tokenize()
            
            .reorder()
            .flatten()
            ;
        } catch (Exception e) {
            Assert.fail(script.print()+"\n"+printMessage(e));
        }
    }
    
    private static void assertScriptException(ScriptAssertion script,Class<?> site, int ordinal) {
        try {
            script
            
            .tokenize()
            
            .reorder()
            .flatten()
            ;
            Assert.fail(script.print());
        } catch (Exception e) {
            testException(e, site ,ordinal);
        }
    }
    
    private static void testException(Throwable t,Class<?> site, int ordinal) {
        Throwable ex = t;
        while(ex != null) {
            if(ex instanceof ParsingException) {
                break;
            }
            ex = ex.getCause();
        }
        if(ex == null) {
            t.printStackTrace();
            Assert.fail("not a exception of type ParsingException");
        }
        Assert.assertTrue(printMessage(ex), ex instanceof ParsingException);
        Assert.assertEquals(printMessage(ex),((ParsingException)ex).getSite(), site);
        Assert.assertEquals(printMessage(ex),((ParsingException)ex).getOrdinal(), ordinal);
    }
    
    private static String printMessage(Throwable ex) {
        StringWriter sw = new StringWriter();
        PrintWriter ret = new PrintWriter(sw);
        ex.printStackTrace(ret);
        return sw.toString();
    }
    
    private static Map<String,Object> asMap(Object... args){
        Map<String,Object> ret = new HashMap<String, Object>();
        if(args.length % 2 !=0) {
            throw new IllegalArgumentException();
        }
        for(int i=0;i<args.length;i+=2) {
            Object key = args[i];
            Object value = args[i+1];
            ret.put(Objects.toString(key), value);
        }
        return ret;
    }
    
    public static <T> List<T> asList(@SuppressWarnings("unchecked") T...args){
        return new ArrayList<T>(Arrays.asList(args));
    }
}
