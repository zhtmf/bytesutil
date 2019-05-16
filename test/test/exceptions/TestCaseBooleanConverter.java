package test.exceptions;

import org.dzh.bytesutil.ConversionException;
import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.types.BYTE;
import org.dzh.bytesutil.converters.BooleanConverter;
import org.junit.Assert;
import org.junit.Test;

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
