package io.github.zhtmf.converters;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.github.zhtmf.ConversionException;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.TypeConverter;
import io.github.zhtmf.annotations.modifiers.CHARSET;
import io.github.zhtmf.annotations.modifiers.Conditional;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.ListLength;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Script;
import io.github.zhtmf.annotations.modifiers.Signed;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.modifiers.Variant;
import io.github.zhtmf.annotations.types.BCD;
import io.github.zhtmf.annotations.types.BYTE;
import io.github.zhtmf.annotations.types.CHAR;
import io.github.zhtmf.annotations.types.INT;
import io.github.zhtmf.annotations.types.INT3;
import io.github.zhtmf.annotations.types.INT5;
import io.github.zhtmf.annotations.types.INT6;
import io.github.zhtmf.annotations.types.INT7;
import io.github.zhtmf.annotations.types.LONG;
import io.github.zhtmf.annotations.types.RAW;
import io.github.zhtmf.annotations.types.SHORT;
import io.github.zhtmf.annotations.types.UserDefined;
import io.github.zhtmf.converters.TestCaseFieldInfo.Entity6.CharsetHandler;
import io.github.zhtmf.converters.auxiliary.DataType;
import io.github.zhtmf.converters.auxiliary.EntityHandler;
import io.github.zhtmf.converters.auxiliary.ModifierHandler;

public class TestCaseFieldInfo {
    
    @Test
    public void test0() throws ConversionException {
        try {
            class Entity extends DataPacket{@Order(0)@BYTE public Timestamp ts;}
            new Entity().serialize(new ByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, FieldInfo.class, 1);
        }
        try {
            class Entity extends DataPacket{@Order(0)@SHORT public Timestamp ts;}
            new Entity().serialize(new ByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, FieldInfo.class, 1);
        }
        try {
            class Entity extends DataPacket{@Order(0)@INT public Timestamp ts;}
            new Entity().serialize(new ByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, FieldInfo.class, 1);
        }
        try {
            class Entity extends DataPacket{@Order(0)@CHAR public Timestamp ts;}
            new Entity().serialize(new ByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, FieldInfo.class, 1);
        }
        try {
            class Entity extends DataPacket{@Order(0)@RAW public Timestamp ts;}
            new Entity().serialize(new ByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, FieldInfo.class, 1);
        }
        try {
            class Entity extends DataPacket{@Order(0)@BCD(1) public Timestamp ts;}
            new Entity().serialize(new ByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, FieldInfo.class, 1);
        }
        try {
            class Entity extends DataPacket{@Order(0)@LONG public Timestamp ts;}
            new Entity().serialize(new ByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, FieldInfo.class, 1);
        }
        try {
            class MyConverter extends TypeConverter<Timestamp>{
                public void serialize(Timestamp obj, Output output) throws IOException {
                }
                public Timestamp deserialize(Input input) throws IOException {
                    return null;
                }}
            class Entity extends DataPacket{@Order(0)@UserDefined(MyConverter.class)@Length public Timestamp ts;}
            new Entity().serialize(new ByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, FieldInfo.class, 23);
        }
        try {
            class MyConverter extends TypeConverter<Timestamp>{
                @SuppressWarnings("unused")
                public MyConverter() {throw new IllegalStateException();}
                public void serialize(Timestamp obj, Output output) throws IOException {
                }
                public Timestamp deserialize(Input input) throws IOException {
                    return null;
                }}
            class Entity extends DataPacket{@Order(0)@UserDefined(MyConverter.class)@Length(8) public Timestamp ts;}
            new Entity().serialize(new ByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, FieldInfo.class, 24);
        }
    }
    
    public static class Entity4 extends DataPacket {
        @Order(0)
        @INT
        @Length(1)
        public List<Byte> bytes;
    }

    @Test
    public void test4() throws ConversionException {
        Entity4 entity = new Entity4();
        entity.bytes = Arrays.asList((byte) 1);
        try {
            entity.serialize(new ByteArrayOutputStream());
        } catch (Exception e) {
            TestUtils.assertExactException(e, FieldInfo.class, 0);
            return;
        }
        Assert.fail();
    }

    public static class Entity8 extends DataPacket {
        @Order(0)
        @INT
        public Byte abyte;
    }

    @Test
    public void test8() throws ConversionException {
        Entity8 entity = new Entity8();
        entity.abyte = (byte) 1;
        try {
            entity.serialize(TestUtils.newThrowOnlyOutputStream());
        } catch (Exception e) {
            TestUtils.assertExactException(e, FieldInfo.class, 1);
            return;
        }
        Assert.fail();
    }
    
    public static class Entity16 extends DataPacket{
        @Order(0)
        @CHAR
        @Length(3)
        @ListLength(3)
        public List<Timestamp> b;
    }
    
    @Test
    public void test16() throws ConversionException {
        Entity16 entity = new Entity16();
        try {
            entity.deserialize(new ByteArrayInputStream(new byte[] {0x0,0x1,0x2}));
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, FieldInfo.class, 0);
        }
    }
    
    static enum Enum17{
        a,b;
    }
    @Test
    public void test17() throws ConversionException {
        class Entity17 extends DataPacket{
            @Order(0)
            @INT3
            public Enum17 enum17;
        }
        class Entity18 extends DataPacket{
            @Order(0)
            @INT5
            public Enum17 enum17;
        }
        class Entity19 extends DataPacket{
            @Order(0)
            @INT6
            public Enum17 enum17;
        }
        class Entity20 extends DataPacket{
            @Order(0)
            @INT7
            public Enum17 enum17;
        }
        try {
            new Entity17().deserialize(new ByteArrayInputStream(new byte[] {0x0,0x1,0x2}));
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, FieldInfo.class, 1);
        }
        try {
            new Entity18().deserialize(new ByteArrayInputStream(new byte[] {0x0,0x1,0x2}));
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, FieldInfo.class, 1);
        }
        try {
            new Entity19().deserialize(new ByteArrayInputStream(new byte[] {0x0,0x1,0x2}));
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, FieldInfo.class, 1);
        }
        try {
            new Entity20().deserialize(new ByteArrayInputStream(new byte[] {0x0,0x1,0x2}));
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, FieldInfo.class, 1);
        }
    }
    
    public static class Entity2 extends DataPacket{
        @Order(0)
        @BCD(2)
        public Date b;
    }
    
    @Test
    public void test2() throws ConversionException {
        Entity2 entity = new Entity2();
        try {
            entity.deserialize(TestUtils.newZeroLengthInputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, FieldInfo.class, 2);
        }
    }
    
    public static class Entity3 extends DataPacket{
        @SuppressWarnings("rawtypes")
        @Order(0)
        @RAW(2)
        @ListLength
        public List list;
    }
    
    @Test
    public void test3() throws ConversionException {
        Entity3 entity = new Entity3();
        try {
            entity.deserialize(TestUtils.newZeroLengthInputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, FieldInfo.class, -1);
        }
    }
    
    public static class Entity5 extends DataPacket{
        @Order(0)
        @Variant(VariantHandler.class)
        public DataPacket list;
        
        public static class VariantHandler extends EntityHandler{
            public VariantHandler() {throw new IllegalArgumentException();}
            @Override
            public DataPacket handle0(String fieldName, Object entity, InputStream is) throws IOException {
                return null;
            }
            
        }
    }
    
    @Test
    public void test5() throws ConversionException {
        Entity5 entity = new Entity5();
        try {
            entity.deserialize(TestUtils.newZeroLengthInputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, FieldInfo.class, 5);
        }
    }
    
    @CHARSET(handler=CharsetHandler.class)
    public static class Entity6 extends DataPacket{
        
        @Order(0)
        @CHAR(1)
        public char ch;
        
        public static class CharsetHandler extends ModifierHandler<Charset>{
            public CharsetHandler() {throw new IllegalArgumentException();}
            @Override
            public Charset handleDeserialize0(String fieldName, Object entity, InputStream is) throws IOException {
                return null;
            }
            @Override
            public Charset handleSerialize0(String fieldName, Object entity) {
                return null;
            }
        }
    }
    
    @Test
    public void test6() throws ConversionException {
        Entity6 entity = new Entity6();
        try {
            entity.deserialize(TestUtils.newZeroLengthInputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, FieldInfo.class, 6);
        }
    }
    
    @CHARSET("abc")
    public static class Entity7 extends DataPacket{
        
        @Order(0)
        @CHAR(1)
        public char ch;
    }
    
    @Test
    public void test7() throws ConversionException {
        Entity7 entity = new Entity7();
        try {
            entity.deserialize(TestUtils.newZeroLengthInputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, FieldInfo.class, 6);
        }
    }
    
    public static class LengthHandler extends ModifierHandler<Integer>{
        public LengthHandler() {throw new IllegalArgumentException();}
        @Override
        public Integer handleDeserialize0(String fieldName, Object entity, InputStream is) throws IOException {
            return null;
        }
        @Override
        public Integer handleSerialize0(String fieldName, Object entity) {
            return null;
        }
    } 
    
    public static class Entity9 extends DataPacket{
        @Order(0)
        @CHAR
        @Length(handler=LengthHandler.class)
        public char ch;
    }
    
    @Test
    public void test9() throws ConversionException {
        Entity9 entity = new Entity9();
        try {
            entity.deserialize(TestUtils.newZeroLengthInputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, FieldInfo.class, 9);
        }
    }
    
    public static class Entity10 extends DataPacket{
        @Order(0)
        @CHAR
        @Length(type=DataType.BCD)
        public char ch;
    }
    
    @Test
    public void test10() throws ConversionException {
        Entity10 entity = new Entity10();
        try {
            entity.deserialize(TestUtils.newZeroLengthInputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, FieldInfo.class, 10);
        }
    }
    
    public static class Entity11 extends DataPacket{
        @Order(0)
        @CHAR(2)
        @ListLength(handler=LengthHandler.class)
        public List<Byte> ch;
    }
    
    @Test
    public void test11() throws ConversionException {
        Entity11 entity = new Entity11();
        try {
            entity.deserialize(TestUtils.newZeroLengthInputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, FieldInfo.class, 11);
        }
    }
    
    public static class Entity21 extends DataPacket{
        @Order(0)
        @CHAR(2)
        @ListLength(2)
        @Unsigned
        @Signed
        public List<Byte> ch;
    }
    
    @Test
    public void test21() throws ConversionException {
        Entity21 entity = new Entity21();
        try {
            entity.deserialize(TestUtils.newZeroLengthInputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, FieldInfo.class, 21);
        }
    }
    
    @Unsigned
    @Signed
    public static class Entity22 extends DataPacket{
        @Order(0)
        @CHAR(2)
        @ListLength(2)
        public List<Byte> ch;
    }
    
    @Test
    public void test22() throws ConversionException {
        Entity22 entity = new Entity22();
        try {
            entity.deserialize(TestUtils.newZeroLengthInputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, FieldInfo.class, 22);
        }
    }
    
    @Unsigned
    public static class Entity23 extends DataPacket{
        @BYTE
        @Order(0)
        public int len;
        @CHAR(3)
        @Conditional(scripts = @Script("return 3()*4;"))
        @CHARSET(scripts = @Script("return 3;"))
        @Order(1)
        public String str;
    }
    
    @Test
    public void test23() throws ConversionException {
        Entity23 entity = new Entity23();
        entity.len = 5;
        entity.str = "abc";
        try {
            entity.serialize(TestUtils.newByteArrayOutputStream());
            fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, FieldInfo.class, 12);
        }
    }
    
    @Unsigned
    public static class Entity24 extends DataPacket{
        @BYTE
        @Order(0)
        public int len;
        @CHAR(3)
        @Conditional
        @CHARSET(scripts = @Script("return 'gbk';"))
        @Order(1)
        public String str;
    }
    
    @Test
    public void test24() throws ConversionException {
        Entity24 entity = new Entity24();
        entity.len = 5;
        entity.str = "abc";
        try {
            entity.serialize(TestUtils.newByteArrayOutputStream());
            fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, FieldInfo.class, 26);
        }
    }
    
    @Unsigned
    public static class Entity25 extends DataPacket{
        @BYTE
        @Order(0)
        public int len;
        @Order(1)
        public Entity25_1 entity;
    }
    
    @Unsigned
    public static class Entity25_1 extends DataPacket{
    	@BYTE
        @Order(0)
        public int len;
    	
    	public Entity25_1(double d) {
    		
    	}
    }
    
    @Test
    public void test25() throws ConversionException {
        try {
            new Entity25().serialize(TestUtils.newByteArrayOutputStream());
            fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, FieldInfo.class, 33);
        }
    }
    
    @Unsigned
    public static class Entity26 extends DataPacket{
        @BYTE
        @Order(0)
        public int len = 10;
        @Order(1)
        public Entity26_1 entity = new Entity26_1(1);
    }
    
    @Unsigned
    public static class Entity26_1 extends DataPacket{
    	@BYTE
        @Order(0)
        public int len = 5;
    	
    	public Entity26_1(int i) {
    		
    	}
    	
    	public Entity26_1() {
    		throw new RuntimeException();
    	}
    }
    
    @Test
    public void test26() throws ConversionException {
        try {
        	TestUtils.serializeMultipleTimesAndRestore(new Entity26());
            fail();
        } catch (Exception e) {
            TestUtils.assertExactExceptionInHierarchy(e, FieldInfo.class, 34);
        }
    }
    
    /*
     * test constructors with access level other than 'public'
     */
    @Unsigned
    public static class Entity27 extends DataPacket{
        @BYTE
        @Order(0)
        public int len = 10;
        @Order(1)
        public Entity27_1 entity = new Entity27_1();
        @Order(2)
        public Entity27_2 entity2 = new Entity27_2();
        @Order(3)
        public Entity27_3 entity3 = new Entity27_3();
    }
    
    @Unsigned
    public static class Entity27_1 extends DataPacket{
    	@BYTE
        @Order(0)
        public int len = 5;
    	
    	Entity27_1() {
    	}
    }
    
    @Unsigned
    public static class Entity27_2 extends DataPacket{
    	@BYTE
        @Order(0)
        public int len = 5;
    	
    	private Entity27_2() {
    	}
    }
    
    @Unsigned
    public static class Entity27_3 extends DataPacket{
    	@BYTE
        @Order(0)
        public int len = 5;
    	
    	protected Entity27_3() {
    	}
    }
    
    @Test
    public void test27() throws Exception {
		TestUtils.serializeMultipleTimesAndRestore(new Entity27());
    }
}