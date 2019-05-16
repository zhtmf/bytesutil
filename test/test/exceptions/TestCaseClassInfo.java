package test.exceptions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.List;

import org.dzh.bytesutil.ConversionException;
import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.TypeConverter;
import org.dzh.bytesutil.annotations.modifiers.Length;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.types.BCD;
import org.dzh.bytesutil.annotations.types.BYTE;
import org.dzh.bytesutil.annotations.types.CHAR;
import org.dzh.bytesutil.annotations.types.INT;
import org.dzh.bytesutil.annotations.types.RAW;
import org.dzh.bytesutil.annotations.types.UserDefined;
import org.dzh.bytesutil.converters.Converters;
import org.dzh.bytesutil.converters.auxiliary.ClassInfo;
import org.dzh.bytesutil.converters.auxiliary.DataType;
import org.dzh.bytesutil.converters.auxiliary.EntityHandler;
import org.dzh.bytesutil.converters.auxiliary.PlaceHolderHandler;
import org.dzh.bytesutil.converters.auxiliary.StreamUtils;
import org.dzh.bytesutil.converters.auxiliary.Utils;
import org.junit.Assert;
import org.junit.Test;

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
    
    @SuppressWarnings("rawtypes")
    @Test
    public void testMakeJacocoHappy() throws Exception {
        {
            Constructor c = Utils.class.getDeclaredConstructor();
            c.setAccessible(true);
            c.newInstance();
        }
        {
            Constructor c = StreamUtils.class.getDeclaredConstructor();
            c.setAccessible(true);
            c.newInstance();
        }
        {
            try {
                new PlaceHolderHandler().handleDeserialize0(null, null, null);
            } catch (Exception e) {
                TestUtils.assertException(e, UnsupportedOperationException.class);
            }
            try {
                new PlaceHolderHandler().handleSerialize0(null, null);
            } catch (Exception e) {
                TestUtils.assertException(e, UnsupportedOperationException.class);
            }
            new PlaceHolderHandler.DefaultCharsetHandler();
            new PlaceHolderHandler.DefaultLengthHandler();
        }
        {
            class MySub extends EntityHandler{
                
                @Override
                public DataPacket handle0(String fieldName, Object entity, InputStream is) throws IOException {
                    return null;
                }
            }
            try {
                new MySub().handleSerialize0(null,null);
            } catch (Exception e) {
                TestUtils.assertException(e, UnsupportedOperationException.class);
            }
        }
        {
            try {
                Method mtd = DataType.class.getDeclaredMethod("mappedEnumFieldClass");
                mtd.setAccessible(true);
                mtd.invoke(DataType.USER_DEFINED);
            } catch (Exception e) {
                TestUtils.assertException(e, UnsupportedOperationException.class);
            }
            try {
                Method mtd = DataType.class.getDeclaredMethod("size");
                mtd.setAccessible(true);
                mtd.invoke(DataType.USER_DEFINED);
            } catch (Exception e) {
                TestUtils.assertException(e, UnsupportedOperationException.class);
            }
            try {
                Method mtd = DataType.class.getDeclaredMethod("checkRange",long.class,boolean.class);
                mtd.setAccessible(true);
                mtd.invoke(DataType.USER_DEFINED,0L,Boolean.FALSE);
            } catch (Exception e) {
                TestUtils.assertException(e, UnsupportedOperationException.class);
            }
            try {
                Method mtd = DataType.class.getDeclaredMethod("checkRange",BigInteger.class,boolean.class);
                mtd.setAccessible(true);
                mtd.invoke(DataType.USER_DEFINED,BigInteger.ZERO,Boolean.TRUE);
            } catch (Exception e) {
                TestUtils.assertException(e, UnsupportedOperationException.class);
            }
            {
                class Entity extends DataPacket{@Order(0)@INT int field1;}
                Entity obj = new Entity();
                obj.serialize(TestUtils.newByteArrayOutputStream());
                Method mtd = DataPacket.class.getDeclaredMethod("getClassInfo");
                mtd.setAccessible(true);
                ClassInfo ci = (ClassInfo) mtd.invoke(obj);
                ci.fieldInfoList().get(0).toString();
            }
            {
                Constructor c = Converters.class.getDeclaredConstructor();
                c.setAccessible(true);
                c.newInstance();
            }
        }
    }
}
