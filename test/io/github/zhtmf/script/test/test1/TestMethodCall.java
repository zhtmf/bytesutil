package io.github.zhtmf.script.test.test1;

import java.math.BigDecimal;
import java.math.BigInteger;

public class TestMethodCall {
    public int noParameter() {
        return 0;
    }
    public int oneParameter(int a) {
        return 1;
    }
    public int multipleParameters(BigDecimal one, double two, String third, Boolean fourth) {
        return 2;
    }
    public int nestingParentheses(BigDecimal one, double two, String third, Boolean fourth) {
        return 3;
    }
    public int anotherMethodCall(BigDecimal one, double two) {
        return 4;
    }
    public int asOperand(int one, double two) {
        return 5;
    }
    public String testString(String str1, String str2) {
        return str1+str2;
    }
    public double testNumber(double d1, double d2) {
        return d1+d2;
    }
    public double testNumber2(float d1, float d2) {
        return d1+d2;
    }
    public double testNumber3(long d1, long d2) {
        return d1+d2;
    }
    public double testNumber4(int d1, int d2) {
        return d1+d2;
    }
    public double testNumber5(short d1, short d2) {
        return d1+d2;
    }
    public double testNumber6(byte d1, byte d2) {
        return d1+d2;
    }
    public double testNumber8(Double d1, Double d2) {
        return d1+d2;
    }
    public double testNumber9(Float d1, Float d2) {
        return d1+d2;
    }
    public double testNumber10(Long d1, Long d2) {
        return d1+d2;
    }
    public double testNumber11(Integer d1, Integer d2) {
        return d1+d2;
    }
    public double testNumber12(Short d1, Short d2) {
        return d1+d2;
    }
    public double testNumber13(Byte d1, Byte d2) {
        return d1+d2;
    }
    public double testNumber15(BigDecimal d1, BigDecimal d2) {
        return d1.add(d2).doubleValue();
    }
    public double testNumber16(BigInteger d1, BigInteger d2) {
        return d1.add(d2).doubleValue();
    }
    public boolean testBoolean(boolean d1, boolean d2) {
        return d1 ^ d2;
    }
    public boolean testNULL(BigDecimal d1, Double d2) {
        return d1 != null && d2 == null;
    }
    
    public boolean testProtected(BigDecimal d1, Double d2) {
        return d1 != null && d2 == null;
    }

    public int testOverloading1(BigDecimal num) {
        return 1;
    }

    public int testOverloading1(double num) {
        return 2;
    }

    public int testOverloading2(double num) {
        return 1;
    }

    public int testOverloading2(Double num) {
        return 2;
    }

    public int testOverloading3(Double num) {
        return 1;
    }

    public int testOverloading3(float num) {
        return 2;
    }

    public int testOverloading4(float num) {
        return 1;
    }

    public int testOverloading4(Float num) {
        return 2;
    }

    public int testOverloading5(Float num) {
        return 1;
    }

    public int testOverloading5(BigInteger num) {
        return 2;
    }

    public int testOverloading6(BigInteger num) {
        return 1;
    }

    public int testOverloading6(long num) {
        return 2;
    }

    public int testOverloading7(long num) {
        return 1;
    }

    public int testOverloading7(Long num) {
        return 2;
    }

    public int testOverloading8(Long num) {
        return 1;
    }

    public int testOverloading8(int num) {
        return 2;
    }

    public int testOverloading9(int num) {
        return 1;
    }

    public int testOverloading9(Integer num) {
        return 2;
    }

    public int testOverloading10(Integer num) {
        return 1;
    }

    public int testOverloading10(short num) {
        return 2;
    }

    public int testOverloading11(short num) {
        return 1;
    }

    public int testOverloading11(Short num) {
        return 2;
    }

    public int testOverloading12(Short num) {
        return 1;
    }

    public int testOverloading12(byte num) {
        return 2;
    }

    public int testOverloading13(byte num) {
        return 1;
    }

    public int testOverloading13(Byte num) {
        return 2;
    }
    
    public int maxSpecific1(Byte num1, byte num2) {
        return 1;
    }
    public int maxSpecific1(short num1,Short num2) {
        return 2;
    }
    public int maxSpecific1(Integer num1,Integer num2) {
        return 3;
    }
    public int maxSpecific1(int num1,byte num2) {
        return 4;
    }
    public int maxSpecific1(long num1,double num2, boolean b) {
        return 5;
    }
    public int maxSpecific1(Double num1,Float num2) {
        return 6;
    }
    
    protected int maxSpecific2(int num1,long num2) {
        return 1;
    }
    public int maxSpecific2(double num1, byte num2) {
        return 2;
    }
    
    public int stringMismatch(double num1) {
        return 2;
    }
    public int boolMismatch(double num1) {
        return 2;
    }
    public int numberMismatch(String num1) {
        return 2;
    }
    protected int nullMismatch(int num1) {
        return 2;
    }
    
    public int exception(String num1) {
        throw new RuntimeException("exception within method");
    }
    
    @SuppressWarnings("unused")
    private int privateMethod(int num1) {
        return 2;
    }
    protected int privateMethod(String num1) {
        return 2;
    }
    
    public static long static1(int n) {
        return n*1;
    }
    public static long static1(long n) {
        return n*2;
    }
}
