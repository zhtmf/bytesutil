package test.exceptions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.dzh.bytesutil.ConversionException;
import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.Length;
import org.dzh.bytesutil.annotations.modifiers.ListLength;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.types.BCD;
import org.dzh.bytesutil.annotations.types.CHAR;
import org.dzh.bytesutil.annotations.types.INT;
import org.dzh.bytesutil.converters.auxiliary.FieldInfo;
import org.junit.Assert;
import org.junit.Test;

import test.TestUtils;

public class TestCaseInitialParsing {
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
}