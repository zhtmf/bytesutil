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
import org.dzh.bytesutil.annotations.types.INT;
import org.dzh.bytesutil.annotations.types.RAW;
import org.dzh.bytesutil.annotations.types.SHORT;
import org.dzh.bytesutil.converters.auxiliary.Utils;
import org.junit.Assert;
import org.junit.Test;

import test.TestUtils;

public class TestIntegerRange {

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
	
	@Unsigned
	public static class EntityIntArray extends DataPacket{
		@Order(0)
		@RAW(5)
		public int[] arr;
	}
	
	@Signed
	public static class EntityIntArray2 extends DataPacket{
		@Order(0)
		@RAW(5)
		public int[] arr;
	}
	
	@Test
	public void testEntityIntArray() throws Exception {
		{
			EntityIntArray entity = new EntityIntArray();
			entity.arr = new int[] {255,200,127,0,1};
			TestUtils.serializeAndRestore(entity);
		}
		{
			EntityIntArray2 entity = new EntityIntArray2();
			entity.arr = new int[] {-1,0,1,127,-128};
			TestUtils.serializeAndRestore(entity);
		}
		{
			EntityIntArray entity = new EntityIntArray();
			entity.arr = new int[] {-1,256,127,0,1};
			try {
				entity.serialize(TestUtils.newByteArrayOutputStream());
				Assert.fail();
			} catch (Exception e) {
				TestUtils.assertExactException(e, Utils.class, 1);
			}
		}
		{
			EntityIntArray entity = new EntityIntArray();
			entity.arr = new int[] {256,-1,127,0,1};
			try {
				entity.serialize(TestUtils.newByteArrayOutputStream());
				Assert.fail();
			} catch (Exception e) {
				TestUtils.assertExactException(e, Utils.class, 1);
			}
		}
		{
			EntityIntArray2 entity = new EntityIntArray2();
			entity.arr = new int[] {-129,0,0,0,0};
			try {
				entity.serialize(TestUtils.newByteArrayOutputStream());
				Assert.fail();
			} catch (Exception e) {
				TestUtils.assertExactException(e, Utils.class, 1);
			}
		}
		{
			EntityIntArray2 entity = new EntityIntArray2();
			entity.arr = new int[] {128,0,0,0,0};
			try {
				entity.serialize(TestUtils.newByteArrayOutputStream());
				Assert.fail();
			} catch (Exception e) {
				TestUtils.assertExactException(e, Utils.class, 1);
			}
		}
	}
	
	@Test
	public void testRange() throws Exception {
		try {
			class Entity extends DataPacket{@Order(0)@SHORT@Unsigned public long sh = -1;}
			new Entity().serialize(new ByteArrayOutputStream());
			Assert.fail();
		} catch (Exception e) {
			TestUtils.assertExactException(e, Utils.class, 1);
		}
		try {
			class Entity extends DataPacket{@Order(0)@SHORT@Unsigned public long sh = 70000;}
			new Entity().serialize(new ByteArrayOutputStream());
			Assert.fail();
		} catch (Exception e) {
			TestUtils.assertExactException(e, Utils.class, 1);
		}
		try {
			class Entity extends DataPacket{@Order(0)@SHORT@Signed public long sh = 65535;}
			new Entity().serialize(new ByteArrayOutputStream());
			Assert.fail();
		} catch (Exception e) {
			TestUtils.assertExactException(e, Utils.class, 1);
		}
		try {
			class Entity extends DataPacket{@Order(0)@SHORT@Signed public long sh = -65535;}
			new Entity().serialize(new ByteArrayOutputStream());
			Assert.fail();
		} catch (Exception e) {
			TestUtils.assertExactException(e, Utils.class, 1);
		}
		try {
			class Entity extends DataPacket{@Order(0)@INT@Signed public long in = ((long)Integer.MAX_VALUE)+1;}
			new Entity().serialize(new ByteArrayOutputStream());
			Assert.fail();
		} catch (Exception e) {
			TestUtils.assertExactException(e, Utils.class, 1);
		}
		try {
			class Entity extends DataPacket{@Order(0)@INT@Signed public long in = ((long)Integer.MIN_VALUE)-1;}
			new Entity().serialize(new ByteArrayOutputStream());
			Assert.fail();
		} catch (Exception e) {
			TestUtils.assertExactException(e, Utils.class, 1);
		}
		try {
			class Entity extends DataPacket{@Order(0)@INT@Unsigned public long in = ((long)Integer.MAX_VALUE)*3+1;}
			new Entity().serialize(new ByteArrayOutputStream());
			Assert.fail();
		} catch (Exception e) {
			TestUtils.assertExactException(e, Utils.class, 1);
		}
		try {
			class Entity extends DataPacket{@Order(0)@INT@Unsigned public long in = ((long)Integer.MIN_VALUE)*3-1;}
			new Entity().serialize(new ByteArrayOutputStream());
			Assert.fail();
		} catch (Exception e) {
			TestUtils.assertExactException(e, Utils.class, 1);
		}
	}
}