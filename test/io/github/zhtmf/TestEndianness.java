package io.github.zhtmf;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;

import org.junit.Assert;
import org.junit.Test;

import io.github.zhtmf.ConversionException;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.LittleEndian;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Signed;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.INT;
import io.github.zhtmf.annotations.types.LONG;
import io.github.zhtmf.annotations.types.SHORT;
import io.github.zhtmf.converters.TestUtils;

public class TestEndianness {
    
    @Test
    public void testShort() throws ConversionException, IOException {
        class Entity extends DataPacket{@LittleEndian @SHORT @Order(0)public short s1 = Short.MIN_VALUE;}
        byte[] b1;
        byte[] b2;
        Entity entity = new Entity();
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            entity.serialize(baos);
            b1 = baos.toByteArray();
        }
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeShort(Short.reverseBytes(entity.s1));
            b2 = baos.toByteArray();
        }
        Assert.assertArrayEquals(b1, b2);
    }
    
    @Test
    public void testInt() throws ConversionException, IOException {
        class Entity extends DataPacket{@LittleEndian @INT @Order(0)public int s1 = Integer.MIN_VALUE;}
        byte[] b1;
        byte[] b2;
        Entity entity = new Entity();
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            entity.serialize(baos);
            b1 = baos.toByteArray();
        }
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeInt(Integer.reverseBytes(entity.s1));
            b2 = baos.toByteArray();
        }
        Assert.assertArrayEquals(b1, b2);
    }
    
    @Test
    public void testLong() throws ConversionException, IOException {
        class Entity extends DataPacket{@LittleEndian @LONG @Order(0)public long s1 = Long.MIN_VALUE;}
        byte[] b1;
        byte[] b2;
        Entity entity = new Entity();
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            entity.serialize(baos);
            b1 = baos.toByteArray();
        }
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeLong(Long.reverseBytes(entity.s1));
            b2 = baos.toByteArray();
        }
        Assert.assertArrayEquals(b1, b2);
    }
    
    @Test
    public void testBigInteger() throws ConversionException, IOException {
        class Entity extends DataPacket{
            @LittleEndian @Signed @LONG @Order(0)public BigInteger s1 = BigInteger.valueOf(Long.MIN_VALUE);}
        byte[] b1;
        byte[] b2;
        Entity entity = new Entity();
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            entity.serialize(baos);
            b1 = baos.toByteArray();
        }
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeLong(Long.reverseBytes(entity.s1.longValue()));
            b2 = baos.toByteArray();
        }
        Assert.assertArrayEquals(b1, b2);
    }
    
    @Test
    public void testBigInteger2() throws Exception {
        class Entity extends DataPacket{
            @LittleEndian @Unsigned @LONG @Order(0)
            public BigInteger s1 = BigInteger.valueOf(Long.MAX_VALUE).multiply(new BigInteger("2"));}
        byte[] b1;
        byte[] b2;
        Entity entity = new Entity();
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            entity.serialize(baos);
            b1 = baos.toByteArray();
        }
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeLong(Long.reverseBytes(entity.s1.longValue()));
            b2 = baos.toByteArray();
        }
        Assert.assertArrayEquals(b1, b2);
        Entity restored = new Entity();
        restored.s1 = null;
        restored.deserialize(TestUtils.newInputStream(b2));
        Assert.assertTrue(TestUtils.equalsOrderFields(entity, restored));
    }
}
