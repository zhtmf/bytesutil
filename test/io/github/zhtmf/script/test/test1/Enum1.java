package io.github.zhtmf.script.test.test1;

public enum Enum1 {
    A(1), B(2), C(3);

    final public int value;

    private Enum1(int value) {
        this.value = value;
    }
    
    public int getValue() {
        return value;
    }
    
    public String getStr() {
        return "abc";
    }
    
    public static int staticMethod1() {
        return 12345;
    }
}