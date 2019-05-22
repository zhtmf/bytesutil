package test.exceptions;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.github.zhtmf.ConversionException;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.EndsWith;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.ListLength;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.types.CHAR;
import io.github.zhtmf.converters.StringConverter;
import io.github.zhtmf.converters.auxiliary.ClassInfo;
import io.github.zhtmf.converters.auxiliary.FieldInfo;
import io.github.zhtmf.converters.auxiliary.Utils;
import test.TestUtils;

public class TestCaseCHARSerialization {
    public static class Entity0 extends DataPacket{
        @Order(0)
        @CHAR
        @Length
        public byte b;
    }
    @Test
    public void test0() throws ConversionException {
        Entity0 entity = new Entity0();
        try {
            entity.b = -3;
            entity.serialize(TestUtils.newByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, Utils.class, 0);
            return;
        }
    }
    public static class Entity1 extends DataPacket{
        @Order(0)
        @CHAR(2)
        public byte b;
    }
    @Test
    public void test1() throws ConversionException {
        Entity1 entity = new Entity1();
        try {
            entity.b = 120;
            entity.serialize(TestUtils.newByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, Utils.class, 2);
            return;
        }
    }
    public static class Entity2 extends DataPacket{
        @Order(0)
        @CHAR(2)
        public byte b;
    }
    public static class Entity3 extends DataPacket{
        @Order(0)
        @CHAR(20)
        public byte b;
    }
    @Test
    public void test2() throws ConversionException {
        Entity2 entity = new Entity2();
        try {
            entity.deserialize(TestUtils.newInputStream(new byte[] {(byte)'0',(byte)'9'}));
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, Utils.class, 10);
        }
        try {
            entity.deserialize(TestUtils.newInputStream(new byte[] {(byte)'1',(byte)'/'}));
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, Utils.class, 3);
        }
        try {
            entity.deserialize(TestUtils.newInputStream(new byte[] {(byte)'1',(byte)';'}));
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, Utils.class, 3);
        }
        try {
            new Entity3().deserialize(TestUtils.newInputStream(new byte[] {'9','9','9','9','9','9','9','9','9','9','9','9','9','9','9','9','9','9',
                                                                    '9','9','9','9','9','9','9','9','9','9','9','9','9','9','9','9','9','9',
                                                                    '9','9','9','9','9','9','9','9','9','9','9','9','9','9','9','9','9','9',}));
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, Utils.class, 3);
        }
    }
    @Test
    public void test3() throws ConversionException {
        class EntityX extends DataPacket{
            @CHAR(3)
            @Order(0)
            @Length(3)
            public String str;
        }
        try {
            new EntityX().serialize(TestUtils.newByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, ClassInfo.class, 11);
            return;
        }
    }
    @Test
    public void test4() throws ConversionException {
        class EntityX extends DataPacket{
            @CHAR(3)
            @Order(0)
            @EndsWith({0x0})
            public String str;
        }
        try {
            new EntityX().serialize(TestUtils.newByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, ClassInfo.class, 10);
            return;
        }
    }
    @Test
    public void test5() throws ConversionException {
        class EntityX extends DataPacket{
            @CHAR
            @Order(0)
            @Length(3)
            @EndsWith({0x0})
            public String str;
        }
        try {
            new EntityX().serialize(TestUtils.newByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, ClassInfo.class, 10);
            return;
        }
    }
    @Test
    public void test6() throws ConversionException {
        class EntityX extends DataPacket{
            @CHAR
            @Order(0)
            @EndsWith({})
            public String str;
        }
        try {
            new EntityX().serialize(TestUtils.newByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, FieldInfo.class, 11);
            return;
        }
    }
    @Test
    public void test7() throws ConversionException {
        class EntityX extends DataPacket{
            @CHAR
            @Order(0)
            @EndsWith({0x0})
            public String str;
        }
        try {
            new EntityX().deserialize(TestUtils.newInputStream(new byte[] {0x20,0x30,0x40}));
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, StringConverter.class, 1);
            return;
        }
    }
    @Test
    public void test8() throws ConversionException {
        class EntityX extends DataPacket{
            @CHAR
            @Order(0)
            @ListLength(3)
            @EndsWith({0x0})
            public List<String> strs;
        }
        EntityX x = new EntityX();
        try {
            x.deserialize(TestUtils.newInputStream(new byte[] {0x20,0x0,0x20,0x30,0x0,0x40,0x50}));
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, StringConverter.class, 1);
            return;
        }
    }
}