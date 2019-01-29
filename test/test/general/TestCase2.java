package test.general;

import java.io.ByteArrayOutputStream;

import org.dzh.bytesutil.ConversionException;
import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.BigEndian;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Signed;
import org.dzh.bytesutil.annotations.types.BYTE;
import org.junit.Before;
import org.junit.Test;

import org.junit.Assert;

public class TestCase2 {
	
	private Entity2 entity = new Entity2();
	
	@Before
	public void setValues() {
		entity.b1 = 1;
		entity.b2 = 2;
		entity.b3 = 3;
	}
	
	@Signed
	@BigEndian
	public static final class Entity2 extends DataPacket{
		@Order(0)
		@BYTE
		public byte b1;
		@Order(1)
		@BYTE
		public byte b2;
		@Order(2)
		@BYTE
		public byte b3;
	}
	
	@Test
	public void testOrder() throws ConversionException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		entity.serialize(baos);
		byte[] arr = baos.toByteArray();
		Assert.assertEquals(arr[0], 1);
		Assert.assertEquals(arr[1], 2);
		Assert.assertEquals(arr[2], 3);
	}
}
