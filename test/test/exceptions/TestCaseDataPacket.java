package test.exceptions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import org.dzh.bytesutil.ConversionException;
import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.Length;
import org.dzh.bytesutil.annotations.modifiers.ListLength;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.types.BYTE;
import org.dzh.bytesutil.annotations.types.CHAR;
import org.dzh.bytesutil.annotations.types.INT;
import org.dzh.bytesutil.annotations.types.SHORT;
import org.junit.Assert;
import org.junit.Test;

import test.exceptions.TestCaseDataPacket.Entity11.SubEntity11;


public class TestCaseDataPacket {
	
	public static class Entity0 extends DataPacket{
		@Order(0)
		public SubEntity0 b;
		public static class SubEntity0 extends DataPacket{
			@Order(0)
			@INT
			public int a;
		}
	}
	@Test
	public void test0() throws ConversionException {
		Entity0 entity = new Entity0();
		try {
			entity.serialize(ExTestUtils.newByteArrayOutputStream());
		} catch (Exception e) {
			ExTestUtils.assertExactException(e, DataPacket.class, 0);
			return;
		}
		Assert.fail();
	}
	
	public static class Entity extends DataPacket{
		@Order(0)
		public int b;
	}
	@Test
	public void test1() throws ConversionException {
		Entity entity = new Entity();
		try {
			entity.serialize(null);
		} catch (Exception e) {
			ExTestUtils.assertException(e,NullPointerException.class);
			return;
		}
		Assert.fail();
	}
	public static class Entity2 extends DataPacket{
		@Order(0)
		@BYTE
		@Length
		public List<Byte> bytes;
	}
	@Test
	public void test2() throws ConversionException {
		Entity2 entity = new Entity2();
		entity.bytes = Arrays.asList((byte)1);
		try {
			entity.serialize(ExTestUtils.newThrowOnlyOutputStream());
		} catch (Exception e) {
			ExTestUtils.assertExactException(e, DataPacket.class, 1);
			return;
		}
		Assert.fail();
	}
	public static class Entity3 extends DataPacket{
		@Order(0)
		@BYTE
		@Length(2)
		public List<Byte> bytes;
	}
	@Test
	public void test3() throws ConversionException {
		Entity3 entity = new Entity3();
		entity.bytes = Arrays.asList((byte)1);
		try {
			entity.serialize(new ByteArrayOutputStream());
		} catch (Exception e) {
			ExTestUtils.assertExactException(e, DataPacket.class, 2);
			return;
		}
		Assert.fail();
	}
	public static class Entity4 extends DataPacket{
		@Order(0)
		@INT
		@Length(1)
		public List<Byte> bytes;
	}
	@Test
	public void test4() throws ConversionException {
		Entity4 entity = new Entity4();
		entity.bytes = Arrays.asList((byte)1);
		try {
			entity.serialize(new ByteArrayOutputStream());
		} catch (Exception e) {
			ExTestUtils.assertExactException(e, DataPacket.class, 3);
			return;
		}
		Assert.fail();
	}
	public static class Entity5 extends DataPacket{
		@Order(0)
		@BYTE
		@Length(1)
		public List<Byte> bytes;
	}
	@Test
	public void test5() throws ConversionException {
		Entity5 entity = new Entity5();
		entity.bytes = Arrays.asList((byte)1);
		try {
			entity.serialize(ExTestUtils.newThrowOnlyOutputStream());
		} catch (Exception e) {
			ExTestUtils.assertExactException(e, DataPacket.class, 4);
			return;
		}
		Assert.fail();
	}
	public static class Entity6 extends DataPacket{
		@Order(0)
		@BYTE
		@Length(1)
		public List<Timestamp> bytes;
	}
	@Test
	public void test6() throws ConversionException {
		Entity6 entity = new Entity6();
		entity.bytes = Arrays.asList(new Timestamp(0));
		try {
			entity.serialize(ExTestUtils.newThrowOnlyOutputStream());
		} catch (Exception e) {
			ExTestUtils.assertExactException(e, DataPacket.class, 5);
			return;
		}
		Assert.fail();
	}
	public static class Entity7 extends DataPacket{
		@Order(0)
		@BYTE
		public Timestamp bytes;
	}
	@Test
	public void test7() throws ConversionException {
		Entity7 entity = new Entity7();
		entity.bytes = new Timestamp(0);
		try {
			entity.serialize(ExTestUtils.newThrowOnlyOutputStream());
		} catch (Exception e) {
			ExTestUtils.assertExactException(e, DataPacket.class, 7);
			return;
		}
		Assert.fail();
	}
	public static class Entity8 extends DataPacket{
		@Order(0)
		@INT	
		public Byte abyte;
	}
	@Test
	public void test8() throws ConversionException {
		Entity8 entity = new Entity8();
		entity.abyte = (byte)1;
		try {
			entity.serialize(ExTestUtils.newThrowOnlyOutputStream());
		} catch (Exception e) {
			ExTestUtils.assertExactException(e, DataPacket.class, 8);
			return;
		}
		Assert.fail();
	}
	public static class Entity9 extends DataPacket{
		@Order(0)
		@BYTE	
		public Byte abyte;
	}
	@Test
	public void test9() throws ConversionException {
		Entity9 entity = new Entity9();
		entity.abyte = (byte)1;
		try {
			entity.serialize(ExTestUtils.newThrowOnlyOutputStream());
		} catch (Exception e) {
			ExTestUtils.assertExactException(e, DataPacket.class, 9);
			return;
		}
		Assert.fail();
	}
	public static class Entity10 extends DataPacket{
		@Order(0)
		@BYTE	
		public Byte abyte;
	}
	@Test
	public void test10() throws ConversionException {
		Entity10 entity = new Entity10();
		entity.abyte = (byte)1;
		ByteArrayOutputStream baos = ExTestUtils.newByteArrayOutputStream();
		entity.serialize(baos);
		try {
			entity.deserialize(null);
		} catch (Exception e) {
			ExTestUtils.assertException(e,NullPointerException.class);
			return;
		}
		Assert.fail();
	}
	
	
	public static class Entity11 extends DataPacket{
		@Order(0)
		public SubEntity11 field1;
		public static final class SubEntity11 extends DataPacket{
			@Order(0)
			@SHORT
			private int i1;
			private static boolean throwEx = false;
			public SubEntity11() {
				if(throwEx) {
					throw new IllegalStateException();
				}
				throwEx = true;
			}
		}
	}
	@Test
	public void test11() throws ConversionException {
		Entity11 entity = new Entity11();
		entity.field1 = new SubEntity11();
		ByteArrayOutputStream baos = ExTestUtils.newByteArrayOutputStream();
		entity.serialize(baos);
		try {
			new Entity11().deserialize(new ByteArrayInputStream(baos.toByteArray()));
		} catch (Exception e) {
			ExTestUtils.assertExactException(e, DataPacket.class, 11);
			return;
		}
		Assert.fail();
	}
	
	public static class Entity12 extends DataPacket{
		@Order(0)
		@Length
		@CHAR(10)
		public List<String> chars;
	}
	
	@Test
	public void test12() throws ConversionException {
		Entity12 entity = new Entity12();
		try {
			entity.deserialize(new ByteArrayInputStream(new byte[0]));
		} catch (Exception e) {
			ExTestUtils.assertExactException(e, DataPacket.class, 12);
			return;
		}
		Assert.fail();
	}
	
	public static class Entity13 extends DataPacket{
		@Order(0)
		@INT
		@Length(3)
		public List<Byte> b;
	}
	
	@Test
	public void test13() throws ConversionException {
		Entity13 entity = new Entity13();
		try {
			entity.deserialize(new ByteArrayInputStream(new byte[] {0x0,0x1,0x2}));
			Assert.fail();
		} catch (Exception e) {
			ExTestUtils.assertExactException(e, DataPacket.class, 13);
		}
		
	}
	
	public static class Entity14 extends DataPacket{
		@Order(0)
		@BYTE
		@Length(3)
		public List<Byte> b;
	}
	
	@Test
	public void test14() throws ConversionException {
		Entity14 entity = new Entity14();
		try {
			entity.deserialize(ExTestUtils.newZeroLengthInputStream());
			Assert.fail();
		} catch (Exception e) {
			ExTestUtils.assertExactException(e, DataPacket.class, 14);
		}
	}
	
	public static class Entity15 extends DataPacket{
		@Order(0)
		@Length(3)
		public List<SubEntity15> b;
		public static final class SubEntity15 extends DataPacket{
			@Order(0)
			@SHORT
			private int i1;
			public SubEntity15() {
				throw new IllegalStateException();
			}
		}
	}
	
	@Test
	public void test15() throws ConversionException {
		Entity15 entity = new Entity15();
		try {
			entity.deserialize(new ByteArrayInputStream(new byte[] {0x0,0x1,0x2}));
			Assert.fail();
		} catch (Exception e) {
			ExTestUtils.assertExactException(e, DataPacket.class, 15);
		}
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
			ExTestUtils.assertExactException(e, DataPacket.class, 16);
		}
	}
	
	public static class Entity17 extends DataPacket{
		@Order(0)
		@INT
		public Timestamp b;
	}
	
	@Test
	public void test17() throws ConversionException {
		Entity17 entity = new Entity17();
		try {
			entity.deserialize(new ByteArrayInputStream(new byte[] {0x0,0x1,0x2}));
			Assert.fail();
		} catch (Exception e) {
			ExTestUtils.assertExactException(e, DataPacket.class, 17);
		}
	}
	
	public static class Entity18 extends DataPacket{
		@Order(0)
		@INT
		public byte b;
	}
	
	@Test
	public void test18() throws ConversionException {
		Entity18 entity = new Entity18();
		try {
			entity.deserialize(new ByteArrayInputStream(new byte[] {0x0,0x1,0x2}));
			Assert.fail();
		} catch (Exception e) {
			ExTestUtils.assertExactException(e, DataPacket.class, 18);
		}
	}
	
	public static class Entity19 extends DataPacket{
		@Order(0)
		@BYTE
		public byte b;
	}
	
	@Test
	public void test19() throws ConversionException {
		Entity19 entity = new Entity19();
		try {
			entity.deserialize(ExTestUtils.newZeroLengthInputStream());
			Assert.fail();
		} catch (Exception e) {
			ExTestUtils.assertExactException(e, DataPacket.class, 19);
		}
	}
	
	public static class Entity20 extends DataPacket{
		@Order(0)
		@BYTE
		public SubEntity20 entity;
		public static final class SubEntity20 extends DataPacket{
			@Order(0)
			@SHORT
			private int i1;
		}
	}
	
	@Test
	public void test20() throws ConversionException {
		Entity20 entity = new Entity20();
		try {
			entity.length();
			Assert.fail();
		} catch (Exception e) {
			ExTestUtils.assertExactException(e, DataPacket.class, 20);
		}
	}
}