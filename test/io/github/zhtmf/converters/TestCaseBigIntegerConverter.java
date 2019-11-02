package io.github.zhtmf.converters;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.math.BigInteger;

import org.junit.Assert;
import org.junit.Test;

import io.github.zhtmf.ConversionException;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.CHAR;
import io.github.zhtmf.annotations.types.LONG;
import io.github.zhtmf.converters.BigIntegerConverter;

public class TestCaseBigIntegerConverter{
    @Test
    public void test0() throws ConversionException {
        class Entity0 extends DataPacket{
            @Order(0)
            @LONG
            @Unsigned
            public BigInteger bi = BigInteger.valueOf(-1);
        }
        Entity0 entity = new Entity0();
        try {
            entity.serialize(TestUtils.newByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, BigIntegerConverter.class, 1);
        }
    }
    @Test
    public void test1() throws ConversionException {
        class Entity0 extends DataPacket{
            @Order(0)
            @LONG
            @Unsigned
            public BigInteger bi;
        }
        Entity0 entity = new Entity0();
        try {
            ByteArrayOutputStream baos = TestUtils.newByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeLong(-1);
            entity.deserialize(TestUtils.newInputStream(baos.toByteArray()));
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, BigIntegerConverter.class, 2);
        }
    }
    @Test
    public void test2() throws ConversionException {
        class Entity0 extends DataPacket{
            @Order(0)
            @CHAR(2)
            public BigInteger bi = BigInteger.valueOf(-1);
        }
        Entity0 entity = new Entity0();
        try {
            entity.serialize(TestUtils.newByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, StreamUtils.class, 18);
        }
    }
    @Test
    public void test3() throws ConversionException {
        class Entity0 extends DataPacket{
            @Order(0)
            @CHAR(2)
            public BigInteger bi;
        }
        Entity0 entity = new Entity0();
        try {
            entity.deserialize(TestUtils.newInputStream(new byte[] {(byte)'-',(byte)'1'}));
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, StreamUtils.class, 19);
        }
        try {
            entity.deserialize(TestUtils.newInputStream(new byte[] {(byte)' ',(byte)'1'}));
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, StreamUtils.class, 19);
        }
        try {
            entity.deserialize(TestUtils.newInputStream(new byte[] {(byte)'a',(byte)'f'}));
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, StreamUtils.class, 19);
        }
    }
}
