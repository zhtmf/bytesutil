package io.github.zhtmf.converters;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.github.zhtmf.ConversionException;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.ListEndsWith;
import io.github.zhtmf.annotations.modifiers.ListLength;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.types.CHAR;
import io.github.zhtmf.converters.auxiliary.ListTerminationHandler;

public class TestListEndsWith {
    
    @Test
    public void test0() throws ConversionException {
        class Test0 extends DataPacket{
            @Order(0)
            @CHAR(3)
            @ListEndsWith
            public List<String> abc;
        }
        try {
            new Test0().deserialize(TestUtils.newZeroLengthInputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, FieldInfo.class, 31);
        }
    }
    
    public static final class Test1Handler extends ListTerminationHandler{
        public Test1Handler() {
            throw new UnsupportedOperationException();
        }
        @Override
        public boolean handleDeserialize0(String fieldName, Object entity, InputStream in, List<Object> list)
                throws IOException {
            return false;
        }
    }
    
    @Test
    public void test1() throws ConversionException {
        class Test1 extends DataPacket{
            @Order(0)
            @CHAR(3)
            @ListEndsWith(handler = Test1Handler.class)
            public List<String> abc;
        }
        try {
            new Test1().deserialize(TestUtils.newZeroLengthInputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, FieldInfo.class, 32);
        }
    }
    
    @Test
    public void test2() throws Exception {
        class Test2 extends DataPacket{
            @Order(0)
            @CHAR(3)
            @ListEndsWith({0x01,0x02,0x03})
            public List<String> abc;
        }
        Test2 entity = new Test2();
        entity.abc = Arrays.asList("123","456","789");
        TestUtils.serializeMultipleTimesAndRestore(entity, 10, ()->new Test2());
    }
    
    @Test
    public void test3() throws Exception {
        class Test3 extends DataPacket{
            @Order(0)
            @CHAR(3)
            @ListLength(3)
            @ListEndsWith({0x01,0x02,0x03})
            public List<String> abc;
        }
        try {
            new Test3().deserialize(TestUtils.newZeroLengthInputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, ClassInfo.class, 14);
        }
    }
}
