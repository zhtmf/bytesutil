package test.exceptions;

import java.io.IOException;
import java.sql.Timestamp;

import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.TypeConverter;
import org.dzh.bytesutil.TypeConverter.Input;
import org.dzh.bytesutil.TypeConverter.Output;
import org.dzh.bytesutil.annotations.modifiers.BigEndian;
import org.dzh.bytesutil.annotations.modifiers.CHARSET;
import org.dzh.bytesutil.annotations.modifiers.DatePattern;
import org.dzh.bytesutil.annotations.modifiers.Length;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Signed;
import org.dzh.bytesutil.annotations.types.UserDefined;
import org.dzh.bytesutil.converters.UserDefinedTypeConverter;
import org.junit.Assert;
import org.junit.Test;

import test.TestUtils;

public class TestCaseUserDefined {
	@Signed
	@BigEndian
	@CHARSET("UTF-8")
	public static final class Entity extends DataPacket{
		@Order(0)
		@UserDefined(Converter.class)
		@DatePattern("yyyy-MM-dd")
		@Length(8)
		public Timestamp ts;
	}
	
	public static class Converter extends TypeConverter{
		@Override
		public void serialize(Object obj, TypeConverter.Output context) throws IOException {
			context.writeLong(Long.MAX_VALUE);
			Assert.assertEquals(context.written(), 8);
			context.writeLong(8);
		}
		@Override
		public Object deserialize(Input context) throws IOException{
			Assert.assertEquals(context.available(), 8);
			context.readLong();
			Assert.assertEquals(context.available(), 0);
			context.readLong();
			return null;
		}
	}
	@Test
	public void test1() throws Exception {
		Entity entity = new Entity();
		entity.ts = Timestamp.valueOf("2011-1-1 23:00:59.333");
		try {
			TestUtils.serializeMultipleTimesAndRestore(entity);
		} catch (Exception e) {
			TestUtils.assertExactExceptionInHierarchy(e, Output.class, 1);
		}
	}
	@Test
	public void test2() throws Exception {
		Entity entity = new Entity();
		try {
			entity.deserialize(TestUtils.newInputStream(new byte[] {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}));
		} catch (Exception e) {
			TestUtils.assertExactExceptionInHierarchy(e, Input.class, 1);
		}
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
			context.writeInt(3);
			Assert.assertEquals(context.written(), 4);
		}
		@Override
		public Object deserialize(Input context) throws IOException{
			Assert.assertEquals(context.available(), 8);
			return null;
		}
	}
	@Test
	public void test3() throws Exception {
		Entity2 entity = new Entity2();
		entity.ts = Timestamp.valueOf("2011-1-1 23:00:59.333");
		try {
			TestUtils.serializeMultipleTimesAndRestore(entity);
		} catch (Exception e) {
			TestUtils.assertExactExceptionInHierarchy(e, UserDefinedTypeConverter.class, 1);
		}
	}
	@Test
	public void test4() throws Exception {
		Entity2 entity = new Entity2();
		try {
			entity.deserialize(TestUtils.newZeroLengthInputStream());
		} catch (Exception e) {
			TestUtils.assertExactExceptionInHierarchy(e, UserDefinedTypeConverter.class, 2);
		}
	}
}
