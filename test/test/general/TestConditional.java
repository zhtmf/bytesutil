package test.general;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.CHARSET;
import io.github.zhtmf.annotations.modifiers.Conditional;
import io.github.zhtmf.annotations.modifiers.ListLength;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Signed;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.BYTE;
import io.github.zhtmf.annotations.types.INT;
import io.github.zhtmf.converters.auxiliary.ModifierHandler;
import test.TestUtils;
import test.TestUtils.Provider;

public class TestConditional {
    @CHARSET("UTF-8")
    @Signed
    public static class Test1 extends DataPacket implements Cloneable{
        public int condition;
        @INT
        @Order(0)
        @Conditional(Condition.class)
        public int int1 = 3;
        @BYTE
        @Order(1)
        public byte b1 = 0;
        @INT
        @Order(2)
        @Conditional(Condition.class)
        public int int2;
        @BYTE
        @Order(3)
        public byte b2 = 1;
        @INT
        @Order(4)
        @ListLength(3)
        @Conditional(Condition.class)
        public List<Integer> integerList = Arrays.asList(30,40,50);
        @BYTE
        @Order(5)
        public byte b3 = 2;
        @Order(6)
        @Conditional(Condition.class)
        public Test2 entity1 = new Test2();
        @BYTE
        @Order(7)
        public byte b4 = 3;
        @Order(8)
        @ListLength(1)
        @Conditional(Condition.class)
        public List<Test2> entity2 = Arrays.asList(new Test2());
        @BYTE
        @Order(9)
        public byte b5 = 4;
        @Override
        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }
    @Unsigned
    public static class Test2 extends DataPacket implements Cloneable{
        @INT
        @Order(0)
        public int int1 = 10;
        @INT
        @Order(1)
        public int int2 = 11;
        @Override
        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }
    public static class Condition extends ModifierHandler<Boolean>{
        @Override
        public Boolean handleDeserialize0(String fieldName, Object entity, InputStream is) throws IOException {
            Test1 obj = (Test1)entity;
            return obj.condition<5;
        }
        @Override
        public Boolean handleSerialize0(String fieldName, Object entity) {
            Test1 obj = (Test1)entity;
            return obj.condition<5;
        }
    }
    
    @Test
    public void test() throws Exception {
        Test1 obj1 = new Test1();
        obj1.condition = 0;
        Assert.assertTrue(obj1.length()!=5);
        TestUtils.serializeAndRestore(obj1);
    }
    
    @Test
    public void test2() throws Exception {
        Test1 obj1 = new Test1();
        obj1.condition = 5;
        Assert.assertEquals(obj1.length(), 5);
        TestUtils.serializeMultipleTimesAndRestore(obj1,5,new Provider<Test1>() {

            @Override
            public Test1 newInstance() {
                Test1 ret = new Test1();
                ret.condition = 5;
                return ret;
            }
        });
    }
    
    @CHARSET("UTF-8")
    @Signed
    public static class Test3 extends DataPacket implements Cloneable{
        public int condition;
        @Order(0)
        @INT
        @Conditional(ConditionTest3.class)
        public int int1 = 3;
        @Order(1)
        @BYTE
        @Conditional(value=ConditionTest3.class,negative=true)
        public byte b1 = 0;
    }
    
    public static class ConditionTest3 extends ModifierHandler<Boolean>{
        @Override
        public Boolean handleDeserialize0(String fieldName, Object entity, InputStream is) throws IOException {
            Test3 obj = (Test3)entity;
            return obj.condition<5;
        }
        @Override
        public Boolean handleSerialize0(String fieldName, Object entity) {
            Test3 obj = (Test3)entity;
            return obj.condition<5;
        }
    }
    
    @Test
    public void test3() throws Exception {
        Test3 obj1 = new Test3();
        obj1.condition = 5;
        Assert.assertEquals(obj1.length(), 1);
        obj1.condition = 4;
        Assert.assertEquals(obj1.length(), 4);
    }
}
