package test.exceptions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

import org.dzh.bytesutil.ConversionException;
import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.Length;
import org.dzh.bytesutil.annotations.modifiers.ListLength;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.types.BYTE;
import org.dzh.bytesutil.annotations.types.CHAR;
import org.dzh.bytesutil.annotations.types.INT;
import org.dzh.bytesutil.annotations.types.RAW;
import org.dzh.bytesutil.annotations.types.SHORT;
import org.dzh.bytesutil.converters.CharConverter;
import org.dzh.bytesutil.converters.DataPacketConverter;
import org.dzh.bytesutil.converters.IntArrayConverter;
import org.dzh.bytesutil.converters.ListConverter;
import org.dzh.bytesutil.converters.auxiliary.AbstractListConverter;
import org.junit.Assert;
import org.junit.Test;

import test.TestUtils;
import test.exceptions.TestCaseDataPacket.Entity11.SubEntity11;

public class TestCaseDataPacket {
    
    public static class Entity0 extends DataPacket{
        @Order(0)
        public SubEntity0 b;
        public static class SubEntity0 extends DataPacket{
            @Order(0)
            @INT
            public int a;
        }
    }
    @Test
    public void test0() throws ConversionException {
        Entity0 entity = new Entity0();
        try {
            entity.serialize(TestUtils.newByteArrayOutputStream());
        } catch (Exception e) {
            TestUtils.assertExactException(e, DataPacket.class, 0);
            return;
        }
        Assert.fail();
    }
    
    public static class Entity extends DataPacket{
        @Order(0)
        public int b;
    }
    @Test
    public void test1() throws ConversionException {
        Entity entity = new Entity();
        try {
            entity.serialize(null);
        } catch (Exception e) {
            TestUtils.assertException(e,NullPointerException.class);
            return;
        }
        Assert.fail();
    }
    public static class Entity2 extends DataPacket{
        @Order(0)
        @BYTE
        @Length
        public List<Byte> bytes;
    }
    @Test
    public void test2() throws ConversionException {
        Entity2 entity = new Entity2();
        entity.bytes = Arrays.asList((byte)1);
        try {
            entity.serialize(TestUtils.newThrowOnlyOutputStream());
        } catch (Exception e) {
            TestUtils.assertExactException(e, AbstractListConverter.class, 1);
            return;
        }
        Assert.fail();
    }
    public static class Entity3 extends DataPacket{
        @Order(0)
        @BYTE
        @Length(2)
        public List<Byte> bytes;
    }
    @Test
    public void test3() throws ConversionException {
        Entity3 entity = new Entity3();
        entity.bytes = Arrays.asList((byte)1);
        try {
            entity.serialize(new ByteArrayOutputStream());
        } catch (Exception e) {
            TestUtils.assertExactException(e, AbstractListConverter.class, 2);
            return;
        }
        Assert.fail();
    }

    public static class Entity5 extends DataPacket{
        @Order(0)
        @BYTE
        @Length(1)
        public List<Byte> bytes;
    }
    @Test
    public void test5() throws ConversionException {
        Entity5 entity = new Entity5();
        entity.bytes = Arrays.asList((byte)1);
        try {
            entity.serialize(TestUtils.newThrowOnlyOutputStream());
        } catch (Exception e) {
            TestUtils.assertExactException(e, ListConverter.class, 4);
            return;
        }
        Assert.fail();
    }

    public static class Entity9 extends DataPacket{
        @Order(0)
        @BYTE    
        public Byte abyte;
    }
    @Test
    public void test9() throws ConversionException {
        Entity9 entity = new Entity9();
        entity.abyte = (byte)1;
        try {
            entity.serialize(TestUtils.newThrowOnlyOutputStream());
        } catch (Exception e) {
            TestUtils.assertExactException(e, DataPacket.class, 4);
            return;
        }
        Assert.fail();
    }
    public static class Entity10 extends DataPacket{
        @Order(0)
        @BYTE    
        public Byte abyte;
    }
    @Test
    public void test10() throws ConversionException {
        Entity10 entity = new Entity10();
        entity.abyte = (byte)1;
        ByteArrayOutputStream baos = TestUtils.newByteArrayOutputStream();
        entity.serialize(baos);
        try {
            entity.deserialize(null);
        } catch (Exception e) {
            TestUtils.assertException(e,NullPointerException.class);
            return;
        }
        Assert.fail();
    }
    
    
    public static class Entity11 extends DataPacket{
        @Order(0)
        public SubEntity11 field1;
        public static final class SubEntity11 extends DataPacket{
            @Order(0)
            @SHORT
            private int i1;
            private static boolean throwEx = false;
            public SubEntity11() {
                if(throwEx) {
                    throw new IllegalStateException();
                }
                throwEx = true;
            }
        }
    }
    @Test
    public void test11() throws ConversionException {
        Entity11 entity = new Entity11();
        entity.field1 = new SubEntity11();
        ByteArrayOutputStream baos = TestUtils.newByteArrayOutputStream();
        entity.serialize(baos);
        try {
            new Entity11().deserialize(new ByteArrayInputStream(baos.toByteArray()));
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, DataPacketConverter.class, 11);
            return;
        }
    }
    
    public static class Entity11_1 extends DataPacket{
        @Order(0)
        public SubEntity11_1 field1;
        public static abstract class SubEntity11_1 extends DataPacket{
            @Order(0)
            @SHORT
            private int i1;
        }
    }
    @Test
    public void test11_1() throws ConversionException {
        Entity11_1 entity = new Entity11_1();
        try {
            entity.deserialize(TestUtils.newZeroLengthInputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, DataPacketConverter.class, 11);
            return;
        }
    }
    
    public static class Entity12 extends DataPacket{
        @Order(0)
        @Length
        @CHAR(10)
        public List<String> chars;
    }
    
    @Test
    public void test12() throws ConversionException {
        Entity12 entity = new Entity12();
        try {
            entity.deserialize(new ByteArrayInputStream(new byte[0]));
        } catch (Exception e) {
            TestUtils.assertExactException(e, AbstractListConverter.class, 12);
            return;
        }
        Assert.fail();
    }
    
    public static class Entity14 extends DataPacket{
        @Order(0)
        @BYTE
        @Length(3)
        public List<Byte> b;
    }
    
    @Test
    public void test14() throws ConversionException {
        Entity14 entity = new Entity14();
        try {
            entity.deserialize(TestUtils.newZeroLengthInputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, ListConverter.class, 14);
        }
    }
    
    public static class Entity15 extends DataPacket{
        @Order(0)
        @Length(3)
        public List<SubEntity15> b;
        public static final class SubEntity15 extends DataPacket{
            @Order(0)
            @SHORT
            private int i1;
            public SubEntity15() {
                throw new IllegalStateException();
            }
        }
    }
    
    @Test
    public void test15() throws ConversionException {
        Entity15 entity = new Entity15();
        try {
            entity.deserialize(new ByteArrayInputStream(new byte[] {0x0,0x1,0x2}));
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, DataPacketConverter.class, 11);
        }
    }
    
    public static class Entity19 extends DataPacket{
        @Order(0)
        @BYTE
        public byte b;
    }
    
    @Test
    public void test19() throws ConversionException {
        Entity19 entity = new Entity19();
        try {
            entity.deserialize(TestUtils.newZeroLengthInputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, DataPacket.class, 14);
        }
    }
    
    public static class Entity20 extends DataPacket{
        @Order(0)
        @BYTE
        public SubEntity20 entity;
        public static final class SubEntity20 extends DataPacket{
            @Order(0)
            @SHORT
            private int i1;
        }
    }
    
    @Test
    public void test20() throws ConversionException {
        Entity20 entity = new Entity20();
        try {
            entity.length();
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, DataPacket.class, 20);
        }
    }
    
    public static class Entity$1 extends DataPacket{
        @Order(0)
        @BYTE
        @Length
        public List<Byte> b;
        @Order(1)
        @Length
        public List<Entity$1> b2;
    }
    
    @Test
    public void test$1() throws ConversionException {
        Entity$1 entity = new Entity$1();
        entity.b = Arrays.asList((Byte)null);
        try {
            entity.serialize(TestUtils.newByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, ListConverter.class, -1);
        }
        entity.b = Arrays.asList((Byte)(byte)3);
        entity.b2 = Arrays.asList((Entity$1)null);
        try {
            entity.serialize(TestUtils.newByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, ListConverter.class, -1);
        }
    }
    
    public static class EntityList extends DataPacket{
        @Order(0)
        @RAW(2)
        @Length
        public List<int[]> b;
    }
    
    @Test
    public void testList() throws ConversionException {
        EntityList entity = new EntityList();
        entity.b = Arrays.asList(new int[3]);
        try {
            entity.serialize(TestUtils.newByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, IntArrayConverter.class, 1);
        }
    }
    
    public static class EntityList2 extends DataPacket{
        @Order(0)
        @CHAR(2)
        @ListLength
        public List<Character> array;
    }
    
    @Test
    public void testList2() throws ConversionException {
        EntityList2 entity = new EntityList2();
        try {
            entity.deserialize(TestUtils.newInputStream(new byte[] {1,0x30,0x31}));
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, CharConverter.class, 1);
        }
    }
}