package io.github.zhtmf.converters;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.github.zhtmf.ConversionException;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.TypeConverter;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.types.BCD;
import io.github.zhtmf.annotations.types.BYTE;
import io.github.zhtmf.annotations.types.CHAR;
import io.github.zhtmf.annotations.types.INT;
import io.github.zhtmf.annotations.types.RAW;
import io.github.zhtmf.annotations.types.UserDefined;
import io.github.zhtmf.converters.ClassInfo;
import test.TestUtils;

public class TestCaseClassInfo {
    @Test
    public void test0() throws ConversionException {
        try {
            new ClassInfo(null);
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, ClassInfo.class, 0);
        }
        try {
            new ClassInfo(Object.class);
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, ClassInfo.class, 0);
        }
    }
    @Test
    public void test() throws ConversionException {
        class Entity extends DataPacket{
            @Order(0)
            @INT
            @RAW
            public byte b;
        }
        try {
            new Entity().serialize(new ByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, ClassInfo.class, 1);
        }
    }
    @Test
    public void test1() throws ConversionException {
        class Entity extends DataPacket{
            @Order(0)
            public byte b;
        }
        try {
            new Entity().serialize(new ByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, ClassInfo.class, 2);
        }
    }
    @Test
    public void test1_1() throws ConversionException {
        class Entity extends DataPacket{
            @Order(0)
            public List<Timestamp> ts;
        }
        try {
            new Entity().serialize(new ByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, ClassInfo.class, 2);
        }
    }
    @Test
    public void test1_2() throws ConversionException {
        class Entity extends DataPacket{
            @SuppressWarnings("rawtypes")
            @Order(0)
            public List ts;
        }
        try {
            new Entity().serialize(new ByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, ClassInfo.class, 2);
        }
    }
    @Test
    public void test2() throws ConversionException {
        class Entity extends DataPacket{
            @Order(0)
            @BCD(-1)
            public byte b;
        }
        try {
            new Entity().serialize(new ByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, ClassInfo.class, 3);
        }
    }
    @Test
    public void test3() throws ConversionException {
        class Entity extends DataPacket{
            @Order(0)
            @BYTE
            public List<Byte> bytes;
        }
        try {
            new Entity().serialize(new ByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, ClassInfo.class, 4);
        }
    }
    @Test
    public void test4() throws ConversionException {
        class Entity extends DataPacket{
            @Order(0)
            @CHAR
            @Length(3)
            public List<String> bytes;
        }
        try {
            new Entity().serialize(new ByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, ClassInfo.class, 5);
        }
    }
    @Test
    public void test5() throws ConversionException {
        class Entity extends DataPacket{
            @Order(0)
            @CHAR
            public String bytes;
        }
        try {
            new Entity().serialize(new ByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, ClassInfo.class, 6);
        }
    }
    @Test
    public void test6() throws ConversionException {
        class Entity extends DataPacket{
            @Order(0)
            @RAW
            public byte[] bytes;
        }
        try {
            new Entity().serialize(new ByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, ClassInfo.class, 7);
        }
    }
    @Test
    public void test7() throws ConversionException {
        class Entity extends DataPacket{
            @Order(0)
            @RAW(2)
            public byte[] bytes;
            @Order(0)
            @RAW(2)
            public byte[] bytes1;
        }
        try {
            new Entity().serialize(new ByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, ClassInfo.class, 8);
        }
    }
    public static class MySub2 extends TypeConverter<Timestamp>{
        @Override
        public void serialize(Timestamp obj, Output output) throws IOException {
        }
        
        @Override
        public Timestamp deserialize(Input input) throws IOException {
            return null;
        }
    }
    @Test
    public void test8() throws ConversionException {
        class Entity extends DataPacket{
            @Order(0)
            @UserDefined(MySub2.class)
            public Timestamp ts;
        }
        try {
            new Entity().serialize(new ByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, ClassInfo.class, 9);
        }
    }
}
