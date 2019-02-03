package test.exceptions;

import org.dzh.bytesutil.ConversionException;
import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.types.RAW;
import org.dzh.bytesutil.converters.ByteArrayConverter;
import org.dzh.bytesutil.converters.IntArrayConverter;
import org.junit.Assert;
import org.junit.Test;

import test.TestUtils;

public class TestCaseArrayConverters {
	public static class Entity0 extends DataPacket{
		@Order(0)
		@RAW(3)
		public int[] arr;
		@Order(1)
		@RAW(2)
		public byte[] arr2;
	}
	@Test
	public void test0() throws ConversionException {
		Entity0 entity = new Entity0();
		try {
			entity.arr = new int[2];
			entity.serialize(TestUtils.newByteArrayOutputStream());
			Assert.fail();
		} catch (Exception e) {
			TestUtils.assertExactException(e, IntArrayConverter.class, 1);
		}
		try {
			entity.arr = new int[3];
			entity.arr2 = new byte[1];
			entity.serialize(TestUtils.newByteArrayOutputStream());
			Assert.fail();
		} catch (Exception e) {
			TestUtils.assertExactException(e, ByteArrayConverter.class, 1);
		}
	}
}