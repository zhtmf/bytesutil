package test.exceptions;

import java.util.Date;

import org.dzh.bytesutil.ConversionException;
import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.DatePattern;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.types.BCD;
import org.dzh.bytesutil.annotations.types.CHAR;
import org.dzh.bytesutil.converters.DateConverter;
import org.dzh.bytesutil.converters.auxiliary.Utils;
import org.junit.Assert;
import org.junit.Test;

import test.TestUtils;

public class TestCaseDateConverter{
	public static class Entity0 extends DataPacket{
		@Order(0)
		@BCD(1)
		@DatePattern("G")
		public Date date = new Date();
	}
	@Test
	public void test0() throws ConversionException {
		Entity0 entity = new Entity0();
		try {
			entity.serialize(TestUtils.newByteArrayOutputStream());
			Assert.fail();
		} catch (Exception e) {
			TestUtils.assertExactException(e, Utils.class, 4);
		}
	}
	public static class Entity1 extends DataPacket{
		@Order(0)
		@BCD(1)
		@DatePattern("mm")
		public Date date = new Date();
	}
	@Test
	public void test1() throws ConversionException {
		Entity1 entity = new Entity1();
		try {
			entity.deserialize(TestUtils.newInputStream(new byte[] {0b01100001}));
			Assert.fail();
		} catch (Exception e) {
			TestUtils.assertExactException(e, DateConverter.class, 2);
		}
	}
	public static class Entity2 extends DataPacket{
		@Order(0)
		@CHAR(2)
		@DatePattern("mm")
		public Date date = new Date();
	}
	@Test
	public void test2() throws ConversionException {
		Entity2 entity = new Entity2();
		try {
			entity.deserialize(TestUtils.newInputStream(new byte[] {'6','1'}));
			Assert.fail();
		} catch (Exception e) {
			TestUtils.assertExactException(e, DateConverter.class, 2);
		}
	}
}
