package io.github.zhtmf.converters;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class TestBITOS {

    @Test
    public void test() throws IllegalStateException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BitOutputStream btos = new BitOutputStream(baos);
        btos.write(127);
        btos.writeBits(false);
        btos.writeBits(true);
        btos.writeBits((byte)0b101,3);
        btos.writeBits((byte)0b01,2);
        btos.writeBits(false);
        btos.writeBits((byte)0b1101010,7);
        btos.writeBits(false);
        btos.write(33);
        btos.close();
        byte[] array = baos.toByteArray();
        Assert.assertEquals(array.length, 4);
        Assert.assertEquals(array[0], 127);
        Assert.assertEquals(array[1], (byte)0b01101010);
        Assert.assertEquals(array[2], (byte)0b11010100);
        Assert.assertEquals(array[3], 33);
    }
}
