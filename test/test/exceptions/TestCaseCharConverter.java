package test.exceptions;

import org.dzh.bytesutil.ConversionException;
import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.CHARSET;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.types.CHAR;
import org.dzh.bytesutil.converters.CharConverter;
import org.junit.Assert;
import org.junit.Test;

import test.TestUtils;

public class TestCaseCharConverter{
	public static class Entity0 extends DataPacket{
		@Order(0)
		@CHAR(2)
		@CHARSET("ISO-8859-1")
		public Character ch;
	}
	@Test
	public void test0() throws ConversionException {
		Entity0 entity = new Entity0();
		try {
			entity.deserialize(TestUtils.newInputStream(new byte[] {'a','b'}));
			Assert.fail();
		} catch (Exception e) {
			TestUtils.assertExactException(e, CharConverter.class, 1);
		}
	}
}
