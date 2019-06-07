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

public class TestConditional {
    @CHARSET("UTF-8")
    @Signed
    public static class Test1 extends DataPacket{
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
    }
    @Unsigned
    public static class Test2 extends DataPacket{
        @INT
        @Order(0)
        public int int1 = 10;
        @INT
        @Order(1)
        public int int2 = 11;
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
        obj1.condition = 5;
        Assert.assertEquals(obj1.length(), 5);
        TestUtils.serializeAndRestore(obj1);
    }
}
