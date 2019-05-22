package test.exceptions;

import org.junit.Assert;
import org.junit.Test;

import io.github.zhtmf.ConversionException;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.types.BYTE;
import io.github.zhtmf.converters.BooleanConverter;
import test.TestUtils;

public class TestCaseBooleanConverter{
    @Test
    public void test() throws ConversionException {
        class Entity0 extends DataPacket{
            @Order(0)
            @BYTE
            public boolean bool1;
        }
        Entity0 entity = new Entity0();
        try {
            entity.deserialize(TestUtils.newInputStream(new byte[] {(byte)-1}));
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, BooleanConverter.class, 0);
        }
        try {
            entity.deserialize(TestUtils.newInputStream(new byte[] {(byte)2}));
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, BooleanConverter.class, 0);
        }
    }
}
