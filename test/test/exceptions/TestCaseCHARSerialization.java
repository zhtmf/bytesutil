package test.exceptions;

import org.junit.Assert;
import org.junit.Test;

import io.github.zhtmf.ConversionException;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.EndsWith;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.types.CHAR;
import io.github.zhtmf.converters.ClassInfo;
import io.github.zhtmf.converters.FieldInfo;
import test.TestUtils;

public class TestCaseCHARSerialization {
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
}