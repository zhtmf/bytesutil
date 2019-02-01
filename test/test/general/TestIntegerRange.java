package test.general;

import java.io.ByteArrayOutputStream;

import org.dzh.bytesutil.ConversionException;
import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Signed;
import org.dzh.bytesutil.annotations.modifiers.Unsigned;
import org.dzh.bytesutil.annotations.types.BCD;
import org.dzh.bytesutil.annotations.types.BYTE;
import org.dzh.bytesutil.annotations.types.CHAR;
import org.dzh.bytesutil.annotations.types.SHORT;
import org.dzh.bytesutil.converters.auxiliary.Utils;
import org.junit.Assert;
import org.junit.Test;

import test.TestUtils;

public class TestIntegerRange {

	@Unsigned
	public static class EntityByte extends DataPacket{
		@Order(0)
		@BYTE
		public byte b;
	}
	
	@Test
	public void testByte() throws ConversionException {
		EntityByte entity = new EntityByte();
		try {
			entity.b = -1;
			entity.serialize(new ByteArrayOutputStream());
			Assert.fail();
		} catch (Exception e) {
			TestUtils.assertExactException(e, Utils.class, 1);
		}
		try {
			entity.b = -128;
			entity.serialize(new ByteArrayOutputStream());
			Assert.fail();
		} catch (Exception e) {
			TestUtils.assertExactException(e, Utils.class, 1);
		}
		try {
			entity.b = 1;
			entity.serialize(new ByteArrayOutputStream());
		} catch (Exception e) {
			throw e;
		}
		try {
			entity.b = 127;
			entity.serialize(new ByteArrayOutputStream());
		} catch (Exception e) {
			throw e;
		}
	}
	
	@Signed
	public static class EntityByte2 extends DataPacket{
		@Order(0)
		@CHAR(3)
		public byte b;
	}
	
	@Unsigned
	public static class EntityByte2_1 extends DataPacket{
		@Order(0)
		@CHAR(3)
		public byte b;
	}
	
	@Test
	public void testByte2() throws ConversionException {
		{
			EntityByte2 entity = new EntityByte2();
			try {
				entity.deserialize(TestUtils.newInputStream(new byte[] {(byte)'1',(byte)'2',(byte)'8'}));
				Assert.fail();
			} catch (Exception e) {
				TestUtils.assertExactException(e, Utils.class, 1);
			}
		}
		{
			EntityByte2_1 entity = new EntityByte2_1();
			try {
				entity.deserialize(TestUtils.newInputStream(new byte[] {(byte)'1',(byte)'2',(byte)'8'}));
				Assert.assertEquals(entity.b, (byte)128);
			} catch (Exception e) {
				throw e;
			}
		}
	}
	
	@Unsigned
	public static class EntityByte3 extends DataPacket{
		@Order(0)
		@BCD(2)
		public byte b;
	}
	@Signed
	public static class EntityByte3_1 extends DataPacket{
		@Order(0)
		@BCD(2)
		public byte b;
	}
	
	@Test
	public void testByte3() throws ConversionException {
		{
			EntityByte3 entity = new EntityByte3();
			try {
				entity.deserialize(TestUtils.newInputStream(new byte[] {(byte)0b00000001,(byte)0b10011001}));
				Assert.assertEquals(entity.b, (byte)199);
			} catch (Exception e) {
				throw e;
			}
		}
		{
			EntityByte3_1 entity = new EntityByte3_1();
			try {
				entity.deserialize(TestUtils.newInputStream(new byte[] {(byte)0b00000001,(byte)0b10011001}));
				Assert.fail();
			} catch (Exception e) {
				TestUtils.assertExactException(e, Utils.class, 1);
			}
		}
	}
	
	@Signed
	public static class EntityByte4 extends DataPacket{
		@Order(0)
		@BYTE
		public short b;
	}
	
	@Test
	public void testByte4() throws ConversionException {
		EntityByte4 entity = new EntityByte4();
		entity.b = 130;
		try {
			entity.serialize(TestUtils.newByteArrayOutputStream());
			Assert.fail();
		} catch (Exception e) {
			TestUtils.assertExactException(e, Utils.class, 1);
		}
		entity.b = 120;
		try {
			entity.serialize(TestUtils.newByteArrayOutputStream());
		} catch (Exception e) {
			throw e;
		}
	}
	
	@Unsigned
	public static class EntityByte5 extends DataPacket{
		@Order(0)
		@SHORT
		public short b;
	}
	
	@Test
	public void testByte5() throws ConversionException {
		EntityByte5 entity = new EntityByte5();
		entity.b = -1;
		try {
			entity.serialize(TestUtils.newByteArrayOutputStream());
			Assert.fail();
		} catch (Exception e) {
			TestUtils.assertExactException(e, Utils.class, 1);
		}
	}
	
	@Signed
	public static class EntityByte6 extends DataPacket{
		@Order(0)
		@SHORT
		public short b;
		@Order(1)
		@SHORT
		@Unsigned
		public short c;
	}
	
	@Test
	public void testByte6() throws Exception {
		EntityByte6 entity = new EntityByte6();
		entity.b = -1;
		entity.c = 23234;
		try {
			TestUtils.serializeAndRestore(entity);
		} catch (Exception e) {
			throw e;
		}
	}
}
