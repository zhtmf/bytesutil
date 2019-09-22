package io.github.zhtmf.converters;

import org.junit.Assert;
import org.junit.Test;

import io.github.zhtmf.ConversionException;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.CHARSET;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.types.CHAR;
import io.github.zhtmf.converters.CharConverter;
import test.TestUtils;

public class TestCaseCharConverter{
    public static class Entity0 extends DataPacket{
        @Order(0)
        @CHAR(2)
        @CHARSET("ISO-8859-1")
        public Character ch;
    }
    @Test
    public void test0() throws ConversionException {
        Entity0 entity = new Entity0();
        try {
            entity.deserialize(TestUtils.newInputStream(new byte[] {'a','b'}));
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, CharConverter.class, 1);
        }
    }
}
