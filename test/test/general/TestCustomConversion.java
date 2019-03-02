package test.general;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.TypeConverter;
import org.dzh.bytesutil.annotations.modifiers.BigEndian;
import org.dzh.bytesutil.annotations.modifiers.CHARSET;
import org.dzh.bytesutil.annotations.modifiers.DatePattern;
import org.dzh.bytesutil.annotations.modifiers.Length;
import org.dzh.bytesutil.annotations.modifiers.ListLength;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Signed;
import org.dzh.bytesutil.annotations.types.UserDefined;
import org.dzh.bytesutil.converters.auxiliary.ModifierHandler;
import org.junit.Assert;
import org.junit.Test;

import test.TestUtils;

public class TestCustomConversion {
	public static class Converter1 extends TypeConverter{
		@Override
		public void serialize(Object obj, TypeConverter.Output context) throws IOException {
			context.writeLong(((Timestamp)obj).getTime());
		}
		@Override
		public Object deserialize(Input input) throws IOException{
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
	
	@Signed
	@BigEndian
	@CHARSET("UTF-8")
	public static final class Entity2 extends DataPacket{
		@Order(0)
		@UserDefined(Converter2.class)
		@DatePattern("yyyy-MM-dd")
		@Length(8)
		public Timestamp ts;
	}
	
	public static class Converter2 extends TypeConverter{
		@Override
		public void serialize(Object obj, TypeConverter.Output context) throws IOException {
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
			Assert.assertEquals(context.written(), 0);
			context.writeLong(((Timestamp)obj).getTime());
			Assert.assertEquals(context.written(), 8);
		}
		@Override
		public Object deserialize(Input context) throws IOException{
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
	@Test
	public void test2() throws Exception {
		Entity2 entity = new Entity2();
		Timestamp ts = Timestamp.valueOf("2011-1-1 23:00:59.333");
		entity.ts = ts;
		TestUtils.serializeMultipleTimesAndRestore(entity);
	}
	
	@Signed
	@BigEndian
	@CHARSET("UTF-8")
	public static final class Entity3 extends DataPacket{
		@Order(0)
		@UserDefined(Converter3.class)
		@Length(1+1+2+2+4+4+8+8+3)
		public byte[] customData;
	}
	
	public static class Converter3 extends TypeConverter{
		@Override
		public void serialize(Object obj, TypeConverter.Output context) throws IOException {
			byte[] orig = (byte[])obj;
			DataInputStream dis = new DataInputStream(new ByteArrayInputStream(orig));
			context.writeByte(dis.readByte());
			context.writeByte(dis.readByte());
			context.writeShort(dis.readShort());
			context.writeShort(dis.readShort());
			context.writeInt(dis.readInt());
			context.writeInt(dis.readInt());
			context.writeLong(dis.readLong());
			context.writeLong(dis.readLong());
			context.writeBytes(Arrays.copyOfRange(orig, orig.length-3, orig.length));
			Assert.assertEquals(context.written(), context.length());
		}
		@Override
		public Object deserialize(Input context) throws IOException{
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
}
