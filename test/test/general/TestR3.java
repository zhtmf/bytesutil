package test.general;

import java.io.ByteArrayOutputStream;

import org.dzh.bytesutil.ConversionException;
import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Unsigned;
import org.dzh.bytesutil.annotations.types.RAW;
import org.junit.Assert;
import org.junit.Test;

import test.TestUtils;

public class TestR3 {

	public class EntityR extends DataPacket{
		@Order(0)
		@RAW(2)
		@Unsigned
		public int[] arr;
	}
	
	@Test
	public void testR2() throws ConversionException {
		byte[] src = new byte[] {0,(byte) 255};
		EntityR entity = new EntityR();
		entity.deserialize(TestUtils.newInputStream(src));
		ByteArrayOutputStream baos = TestUtils.newByteArrayOutputStream();
		entity.serialize(baos);
		Assert.assertArrayEquals(src, baos.toByteArray());
	}
}
