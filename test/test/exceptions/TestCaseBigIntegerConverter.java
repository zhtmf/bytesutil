package test.exceptions;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.math.BigInteger;

import org.dzh.bytesutil.ConversionException;
import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Unsigned;
import org.dzh.bytesutil.annotations.types.CHAR;
import org.dzh.bytesutil.annotations.types.LONG;
import org.dzh.bytesutil.converters.BigIntegerConverter;
import org.dzh.bytesutil.converters.auxiliary.Utils;
import org.junit.Assert;
import org.junit.Test;

import test.TestUtils;

public class TestCaseBigIntegerConverter{
	@Test
	public void test0() throws ConversionException {
		class Entity0 extends DataPacket{
			@Order(0)
			@LONG
			@Unsigned
			public BigInteger bi = BigInteger.valueOf(-1);
		}
		Entity0 entity = new Entity0();
		try {
			entity.serialize(TestUtils.newByteArrayOutputStream());
			Assert.fail();
		} catch (Exception e) {
			TestUtils.assertExactException(e, BigIntegerConverter.class, 1);
		}
	}
	@Test
	public void test1() throws ConversionException {
		class Entity0 extends DataPacket{
			@Order(0)
			@LONG
			@Unsigned
			public BigInteger bi;
		}
		Entity0 entity = new Entity0();
		try {
			ByteArrayOutputStream baos = TestUtils.newByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeLong(-1);
			entity.deserialize(TestUtils.newInputStream(baos.toByteArray()));
			Assert.fail();
		} catch (Exception e) {
			TestUtils.assertExactException(e, BigIntegerConverter.class, 2);
		}
	}
	@Test
	public void test2() throws ConversionException {
		class Entity0 extends DataPacket{
			@Order(0)
			@CHAR(2)
			public BigInteger bi = BigInteger.valueOf(-1);
		}
		Entity0 entity = new Entity0();
		try {
			entity.serialize(TestUtils.newByteArrayOutputStream());
			Assert.fail();
		} catch (Exception e) {
			TestUtils.assertExactException(e, Utils.class, 8);
		}
	}
	@Test
	public void test3() throws ConversionException {
		class Entity0 extends DataPacket{
			@Order(0)
			@CHAR(2)
			public BigInteger bi;
		}
		Entity0 entity = new Entity0();
		try {
			entity.deserialize(TestUtils.newInputStream(new byte[] {(byte)'-',(byte)'1'}));
			Assert.fail();
		} catch (Exception e) {
			TestUtils.assertExactException(e, Utils.class, 9);
		}
		try {
			entity.deserialize(TestUtils.newInputStream(new byte[] {(byte)' ',(byte)'1'}));
			Assert.fail();
		} catch (Exception e) {
			TestUtils.assertExactException(e, Utils.class, 9);
		}
		try {
			entity.deserialize(TestUtils.newInputStream(new byte[] {(byte)'a',(byte)'f'}));
			Assert.fail();
		} catch (Exception e) {
			TestUtils.assertExactException(e, Utils.class, 9);
		}
	}
}
