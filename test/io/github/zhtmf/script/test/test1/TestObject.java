package io.github.zhtmf.script.test.test1;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.github.zhtmf.script.ScriptTest;

//nashorn引擎访问不了私有实例域
public class TestObject{
    public static BigDecimal static2 = BigDecimal.ZERO;
    public static final int static1 = 10;
    public static int static3 = 10;
    public static final int static4 = 10;
    public int length = 333;
    public int getProperty2333() {
        throw new UnsupportedOperationException();
    }
    public BigDecimal abc = new BigDecimal(3);
    public BigDecimal def = new BigDecimal(4);
    public String str1 = "abc123456";
    public String str2 = "abc123456";
    public List<Object> list2 = null;
    public List<Object> list = ScriptTest.asList("abc","def",
            ScriptTest.asList("abc","111",ScriptTest.asList(1,2,3,4,5)));
    public List<Object> list3 = ScriptTest.asList(1,2,3,4,5);
    public Object[] array = new Object[] {
            new Object[] {
                "abcdef",
                "defghi",
                new Object[] {
                         1
                        ,2
                        ,3
                        ,4
                        ,new Object[] {
                                9,
                                0,
                                11,
                                12
                        }
                        ,"ssssss"
                }
            },
            new Object()
    };
    
    private int propertyX;
    public int getPropertyX() {
        return propertyX;
    }
    public void setPropertyX(int propertyX) {
        throw new UnsupportedOperationException();
    }
    
    private int setterProperty1;
    public int getSetterProperty1() {
        return setterProperty1;
    }
    public void setSetterProperty1(int setterProperty1) {
        this.setterProperty1 = setterProperty1;
    }
    public void setSetterProperty1(int setterProperty1, int j) {
        throw new UnsupportedOperationException();
    }
    public Object setSetterProperty1(Object setterProperty1) {
        throw new UnsupportedOperationException();
    }
    
    public AtomicInteger numberImpl1;
    
    public TestObject2 object2 = new TestObject2();
}