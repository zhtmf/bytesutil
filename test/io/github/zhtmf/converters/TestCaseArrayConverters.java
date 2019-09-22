package io.github.zhtmf.converters;

import org.junit.Assert;
import org.junit.Test;

import io.github.zhtmf.ConversionException;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.types.RAW;
import io.github.zhtmf.converters.ByteArrayConverter;
import io.github.zhtmf.converters.IntArrayConverter;
import test.TestUtils;

public class TestCaseArrayConverters {
    public static class Entity0 extends DataPacket{
        @Order(0)
        @RAW(3)
        public int[] arr;
        @Order(1)
        @RAW(2)
        public byte[] arr2;
    }
    @Test
    public void test0() throws ConversionException {
        Entity0 entity = new Entity0();
        try {
            entity.arr = new int[2];
            entity.serialize(TestUtils.newByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            ConversionException ce = (ConversionException)e;
            Assert.assertEquals(ce.getEntityClassName(), Entity0.class.getName());
            Assert.assertEquals(ce.getFieldName(), "arr");
            TestUtils.assertExactException(e, IntArrayConverter.class, 1);
        }
        try {
            entity.arr = new int[3];
            entity.arr2 = new byte[1];
            entity.serialize(TestUtils.newByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            ConversionException ce = (ConversionException)e;
            Assert.assertEquals(ce.getEntityClassName(), Entity0.class.getName());
            Assert.assertEquals(ce.getFieldName(), "arr2");
            TestUtils.assertExactException(e, ByteArrayConverter.class, 1);
        }
    }
}