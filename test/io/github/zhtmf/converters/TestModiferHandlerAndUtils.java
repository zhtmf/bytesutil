package io.github.zhtmf.converters;

import java.io.ByteArrayOutputStream;

import org.junit.Assert;
import org.junit.Test;

import io.github.zhtmf.ConversionException;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.types.BCD;

public class TestModiferHandlerAndUtils {
    @Test
    public void test2() throws ConversionException {
        try {
            class Entity extends DataPacket{@Order(0)@BCD(2) public int ts = -1;}
            new Entity().serialize(new ByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactExceptionInHierarchy(e, StreamUtils.class, 15);
        }
        try {
            class Entity extends DataPacket{@Order(0)@BCD(2) public int ts = 133456;}
            new Entity().serialize(new ByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactExceptionInHierarchy(e, StreamUtils.class, 16);
        }
        try {
            class Entity extends DataPacket{@Order(0)@BCD(10) public int ts = 133456;}
            new Entity().serialize(new ByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactExceptionInHierarchy(e, StreamUtils.class, 16);
        }
        try {
            class Entity extends DataPacket{@Order(0)@BCD(7) public String ts = "133456";}
            new Entity().serialize(new ByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactExceptionInHierarchy(e, StreamUtils.class, 17);
        }
    }
}
