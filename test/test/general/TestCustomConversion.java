package test.general;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.TypeConverter;
import io.github.zhtmf.annotations.modifiers.BigEndian;
import io.github.zhtmf.annotations.modifiers.CHARSET;
import io.github.zhtmf.annotations.modifiers.DatePattern;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.ListLength;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Signed;
import io.github.zhtmf.annotations.types.UserDefined;
import io.github.zhtmf.converters.auxiliary.ModifierHandler;
import test.TestUtils;

public class TestCustomConversion {
    public static class Converter1 extends TypeConverter<Timestamp>{
        @Override
        public void serialize(Timestamp obj, TypeConverter.Output context) throws IOException {
            context.writeLong(((Timestamp)obj).getTime());
        }
        @Override
        public Timestamp deserialize(Input input) throws IOException{
            return new Timestamp(input.readLong());
        }
    }
    public static class LengthHandler extends ModifierHandler<Integer>{
        @Override
        public Integer handleDeserialize0(String fieldName, Object entity, InputStream is) throws IOException {
            return 8;
        }
        @Override
        public Integer handleSerialize0(String fieldName, Object entity) {
            return 8;
        }
    }

    @Signed
    @BigEndian
    @CHARSET("UTF-8")
    public static final class Entity extends DataPacket{
        @Order(0)
        @UserDefined(Converter1.class)
        @Length(8)
        public Timestamp ts;
        @Order(1)
        @UserDefined(length=8,value=Converter1.class)
        public Timestamp ts2;
        @Order(2)
        @UserDefined(Converter1.class)
        @Length(8)
        @ListLength(2)
        public List<Timestamp> tsList;
        @Order(3)
        @UserDefined(length=8,value=Converter1.class)
        @ListLength(2)
        public List<Timestamp> tsList2;
        @Order(4)
        @UserDefined(Converter1.class)
        @Length(handler=LengthHandler.class)
        @ListLength(2)
        public List<Timestamp> tsList3;
    }
    @Test
    public void test() throws Exception {
        Entity entity = new Entity();
        Timestamp ts = Timestamp.valueOf("2011-1-1 23:00:59.333");
        entity.ts = ts;
        entity.ts2 = ts;
        entity.tsList = Arrays.asList(ts,ts);
        entity.tsList2 = Arrays.asList(ts,ts);
        entity.tsList3 = Arrays.asList(ts,ts);
        TestUtils.serializeMultipleTimesAndRestore(entity);
        Assert.assertEquals(entity.length(), TestUtils.serializeAndGetBytes(entity).length);
    }
    
    private static enum MyEnum333{
        A,B;
    }
    
    @Signed
    @BigEndian
    @CHARSET("UTF-8")
    public static final class Entity2 extends DataPacket{
        @Order(0)
        @UserDefined(Converter2.class)
        @DatePattern("yyyy-MM-dd")
        @Length(8)
        public Timestamp ts;
        @Order(1)
        @UserDefined(ConverterX.class)
        @Length(1)
        public MyEnum333 enum1;
    }
    
    public static class Converter2 extends TypeConverter<Timestamp>{
        @Override
        public void serialize(Timestamp obj, TypeConverter.Output context) throws IOException {
            Assert.assertEquals(context.getName(), "ts");
            Assert.assertEquals(context.isUnsigned(), false);
            Assert.assertEquals(context.isSigned(), true);
            Assert.assertEquals(context.isBigEndian(), true);
            Assert.assertEquals(context.isLittleEndian(), false);
            Assert.assertEquals(context.getDatePattern(), "yyyy-MM-dd");
            Assert.assertEquals(context.getFieldClass(), Timestamp.class);
            Assert.assertEquals(context.getFieldClass(), Timestamp.class);
            Assert.assertEquals(context.getEntityClass(), Entity2.class);
            Assert.assertEquals(context.getCharset(), Charset.forName("UTF-8"));
            Assert.assertEquals(context.length(), 8);
            Assert.assertEquals(context.written(), 0);
            context.writeLong(((Timestamp)obj).getTime());
            Assert.assertEquals(context.written(), 8);
        }
        @Override
        public Timestamp deserialize(Input context) throws IOException{
            Assert.assertEquals(context.getName(), "ts");
            Assert.assertEquals(context.isUnsigned(), false);
            Assert.assertEquals(context.isSigned(), true);
            Assert.assertEquals(context.isBigEndian(), true);
            Assert.assertEquals(context.isLittleEndian(), false);
            Assert.assertEquals(context.getDatePattern(), "yyyy-MM-dd");
            Assert.assertEquals(context.getFieldClass(), Timestamp.class);
            Assert.assertEquals(context.getEntityClass(), Entity2.class);
            Assert.assertEquals(context.getCharset(), Charset.forName("UTF-8"));
            Assert.assertEquals(context.length(), 8);
            Assert.assertEquals(context.available(), 8);
            Timestamp ret = new Timestamp(context.readLong());
            Assert.assertEquals(context.available(), 0);
            return ret;
        }
    }
    
    public static class ConverterX extends TypeConverter<MyEnum333>{

        @Override
        public void serialize(MyEnum333 obj, Output output) throws IOException {
            output.getFieldClass();//make jacoco happy
            output.getFieldClass();
            output.writeByte((byte) 1);
        }

        @Override
        public MyEnum333 deserialize(Input input) throws IOException {
            input.getFieldClass();
            input.getFieldClass();
            input.readByte();
            return MyEnum333.A;
        }
        
    }
    @Test
    public void test2() throws Exception {
        Entity2 entity = new Entity2();
        Timestamp ts = Timestamp.valueOf("2011-1-1 23:00:59.333");
        entity.ts = ts;
        entity.enum1 = MyEnum333.A;
        TestUtils.serializeMultipleTimesAndRestore(entity);
    }
    
    @Signed
    @BigEndian
    @CHARSET("UTF-8")
    public static final class Entity3 extends DataPacket{
        @Order(0)
        @UserDefined(Converter3.class)
        @Length(1+1+2+2+4+4+8+8+3)
        @CHARSET(handler=CharsetHandler.class)
        public byte[] customData;
    }
    
    public static final class CharsetHandler extends ModifierHandler<Charset>{

        @Override
        public Charset handleDeserialize0(String fieldName, Object entity, InputStream is) throws IOException {
            return StandardCharsets.UTF_16;
        }

        @Override
        public Charset handleSerialize0(String fieldName, Object entity) {
            return StandardCharsets.UTF_16;
        }
        
    }
    
    public static class Converter3 extends TypeConverter<byte[]>{
        @Override
        public void serialize(byte[] obj, TypeConverter.Output context) throws IOException {
            Assert.assertEquals(context.getCharset(), StandardCharsets.UTF_16);
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(obj));
            context.writeByte(dis.readByte());
            context.writeByte(dis.readByte());
            context.writeShort(dis.readShort());
            context.writeShort(dis.readShort());
            context.writeInt(dis.readInt());
            context.writeInt(dis.readInt());
            context.writeLong(dis.readLong());
            context.writeLong(dis.readLong());
            context.writeBytes(Arrays.copyOfRange(obj, obj.length-3, obj.length));
            Assert.assertEquals(context.written(), context.length());
        }
        @Override
        public byte[] deserialize(Input context) throws IOException{
            Assert.assertEquals(context.getCharset(), StandardCharsets.UTF_16);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeByte(context.readUnsignedByte());
            dos.writeByte(context.readByte());
            dos.writeShort(context.readUnsignedShort());
            dos.writeShort(context.readShort());
            dos.writeInt((int) context.readUnsignedInt());
            dos.writeInt(context.readInt());
            dos.writeLong(context.readUnsignedLong().longValue());
            dos.writeLong(context.readLong());
            dos.write(context.readBytes(3));
            Assert.assertEquals(context.available(), 0);
            return baos.toByteArray();
        }
    }
    @Test
    public void test3() throws Exception {
        Entity3 entity = new Entity3();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeByte(Byte.MIN_VALUE);
        dos.writeByte(Byte.MAX_VALUE);
        dos.writeShort(Short.MIN_VALUE);
        dos.writeShort(Short.MAX_VALUE);
        dos.writeInt(Integer.MIN_VALUE);
        dos.writeInt(Integer.MAX_VALUE);
        dos.writeLong(Long.MIN_VALUE);
        dos.writeLong(Long.MAX_VALUE);
        dos.write("abc".getBytes());
        entity.customData = baos.toByteArray();
        TestUtils.serializeMultipleTimesAndRestore(entity);
    }
    
    @Signed
    @BigEndian
    public static final class Entity4 extends DataPacket{
        @Order(0)
        @UserDefined(Converter4.class)
        @CHARSET("UTF-8")
        @Length(2)
        public byte[] customData;
        @Order(1)
        @UserDefined(Converter4.class)
        @CHARSET("GBK")
        @Length(8)
        public Timestamp customData2;
    }
    
    public static class Converter4 extends TypeConverter<Object>{
        @Override
        public void serialize(Object obj, TypeConverter.Output context) throws IOException {
            String name = context.getName();
            if("customData".equals(name)) {
                Assert.assertEquals(context.getCharset(), Charset.forName("UTF-8"));
                context.writeBytes((byte[])obj);
            }else {
                Assert.assertEquals(context.getCharset(), Charset.forName("GBK"));
                context.writeLong(((Timestamp)obj).getTime());
            }
        }
        @Override
        public Object deserialize(Input context) throws IOException{
            String name = context.getName();
            if("customData".equals(name)) {
                Assert.assertEquals(context.getCharset(), Charset.forName("UTF-8"));
                return context.readBytes(context.length());
            }else {
                Assert.assertEquals(context.getCharset(), Charset.forName("GBK"));
                return new Timestamp(context.readLong());
            }
        }
    }
    
    @Test
    public void test4() throws Exception {
        Entity4 entity4 = new Entity4();
        entity4.customData = new byte[] {1,2};
        entity4.customData2 = new Timestamp(System.currentTimeMillis());
        TestUtils.serializeMultipleTimesAndRestoreConcurrently(entity4, 1500);
    }
}
