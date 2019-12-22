package io.github.zhtmf.converters;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.BigEndian;
import io.github.zhtmf.annotations.modifiers.LittleEndian;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.NUMBER;

public class TestNUMBER {
    
    @Unsigned
    public static class ExampleEntity extends DataPacket{
        @Order(0)
        @NUMBER(4)
        @BigEndian
        public BigInteger integer1;
        @Order(1)
        @NUMBER(8)
        @LittleEndian
        public BigInteger integer2;
    }
    
    @Test
    public void test1() throws Exception{
        byte[] arr = {0,1,2,3,4,5,6};
        Assert.assertArrayEquals(new byte[]{1,0,2,3,4,5,6}, StreamUtils.reverse(Arrays.copyOf(arr, arr.length),0,2));
        Assert.assertArrayEquals(new byte[]{6,5,4,3,2,1,0}, StreamUtils.reverse(Arrays.copyOf(arr, arr.length)));
        Assert.assertArrayEquals(new byte[]{6,5,4,3,2,1,0}, StreamUtils.reverse(Arrays.copyOf(arr, arr.length),0, arr.length));
        Assert.assertArrayEquals(new byte[]{0,2,1,3,4,5,6}, StreamUtils.reverse(Arrays.copyOf(arr, arr.length),1,3));
        Assert.assertArrayEquals(new byte[]{0,4,3,2,1,5,6}, StreamUtils.reverse(Arrays.copyOf(arr, arr.length),1,5));
        Assert.assertArrayEquals(new byte[]{0,1,2,3,4,5,6}, StreamUtils.reverse(Arrays.copyOf(arr, arr.length),3,3));
    }

    @Test
    public void test0() throws Exception {
        ByteArrayOutputStream total = new ByteArrayOutputStream();
        ExampleEntity entity = new ExampleEntity();
        for(int i= 0; i< 100000; i+=2) {
            entity.integer1 = BigInteger.valueOf(i);
            entity.integer2 = BigInteger.valueOf(i+1);
            try {
                TestUtils.serializeAndRestore(entity);
            } catch (Throwable e) {
                e.printStackTrace();
                Assert.fail("at "+i+" ");
            }
            entity.serialize(total);
        }
        InputStream mis = TestUtils.newInputStream(total.toByteArray());
        for(int i= 0; i< 100000; i+=2) {
            entity.deserialize(mis);
            Assert.assertEquals("at "+i, i, entity.integer1.intValue());
            Assert.assertEquals("at "+i, i+1, entity.integer2.intValue());
        }
    }
}
