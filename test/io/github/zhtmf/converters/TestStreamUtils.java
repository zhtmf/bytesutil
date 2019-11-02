package io.github.zhtmf.converters;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.github.zhtmf.ConversionException;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.ListLength;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.types.BCD;
import io.github.zhtmf.annotations.types.CHAR;
import io.github.zhtmf.annotations.types.RAW;
import io.github.zhtmf.annotations.types.SHORT;
import io.github.zhtmf.converters.StreamUtils;
import io.github.zhtmf.converters.auxiliary.DataType;

public class TestStreamUtils {
    @Test
    public void test0() throws ConversionException {
        try {
            class Entity extends DataPacket{
            @Order(0)@CHAR(2)@ListLength(type = DataType.BYTE) public List<String> strs = Arrays.asList(new String[300]);}
            new Entity().serialize(new ByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactExceptionInHierarchy(e, StreamUtils.class, 1);
        }
        try {
            class Entity extends DataPacket{
            @Order(0)@BCD(20) public Byte bcd;}
            new Entity().deserialize(TestUtils.newInputStream(new byte[] {'9','9','9','9','9','9','9','9','9','9',
                    '9','9','9','9','9','9','9','9','9','9',
                    '9','9','9','9','9','9','9','9','9','9',
                    '9','9','9','9','9','9','9','9','9','9',
                    '9','9','9','9','9','9','9','9','9','9',
                    '9','9','9','9','9','9','9','9','9','9',
                    '9','9','9','9','9','9','9','9','9','9',
                    '9','9','9','9','9','9','9','9','9','9',
                    '9','9','9','9','9','9','9','9','9','9',
                    '9','9','9','9','9','9','9','9','9','9',
                    '9','9','9','9','9','9','9','9','9','9',
                    '9','9','9','9','9','9','9','9','9','9',
                    '9','9','9','9','9','9','9','9','9','9',
                    '9','9','9','9','9','9','9','9','9','9',}));
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactExceptionInHierarchy(e, StreamUtils.class, 2);
        }
        try {
            class Entity extends DataPacket{
            @Order(0)@SHORT public int shrt;}
            new Entity().deserialize(TestUtils.newInputStream(new byte[] {'9'}));
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertException(e, EOFException.class);
        }
        try {
            class Entity extends DataPacket{
            @Order(0)@RAW(3) public byte[] array;}
            new Entity().deserialize(TestUtils.newInputStream(new byte[] {'9'}));
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertException(e, EOFException.class);
        }
        try {
            class Entity extends DataPacket{
            @Order(0)@RAW@Length(type=DataType.INT) public byte[] array;}
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeInt(Integer.MAX_VALUE*2+1);
            dos.flush();
            dos.close();
            new Entity().deserialize(TestUtils.newInputStream(baos.toByteArray()));
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactExceptionInHierarchy(e, StreamUtils.class, 3);
        }
    }
}
