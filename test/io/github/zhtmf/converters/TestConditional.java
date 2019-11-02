package io.github.zhtmf.converters;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Conditional;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.types.CHAR;
import io.github.zhtmf.converters.auxiliary.ModifierHandler;

public class TestConditional {
    public static class ThrowingConditonal extends ModifierHandler<Boolean>{
        public ThrowingConditonal() throws Exception {
            throw new Exception();
        }
        @Override
        public Boolean handleDeserialize0(String fieldName, Object entity, InputStream is) throws IOException {
            return null;
        }
        @Override
        public Boolean handleSerialize0(String fieldName, Object entity) {
            return null;
        }
    }
    @Test
    public void testException() throws Exception {
        class Test4Entity extends DataPacket{
            @Order(0)
            @CHAR(10)
            @Conditional(ThrowingConditonal.class)
            public String str1;
        }
        Test4Entity entity = new Test4Entity();
        try {
            entity.serialize(TestUtils.newByteArrayOutputStream());
            Assert.fail();
        } catch (Throwable e) {
            TestUtils.assertExactExceptionInHierarchy(e, FieldInfo.class, 25);
        }
    }
}
