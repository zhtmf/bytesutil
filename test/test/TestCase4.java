package test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.dzh.bytesutil.ConversionException;
import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.BigEndian;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Signed;
import org.dzh.bytesutil.annotations.types.BCD;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import test.entity.Utils;

public class TestCase4{
	
	private Entity2 entity = new Entity2();
	
	@Signed
	@BigEndian
	public static final class Entity2 extends DataPacket{
		@Order(-1)
		@BCD(5)
		public long n1;
	}
	
	@Before
	public void setValues() {
		entity.n1 = 1234567070L;
	}
	
	
	@Test
	public void testLength() throws ConversionException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		entity.serialize(baos);
		byte[] arr = baos.toByteArray();
		Entity2 e2 = new Entity2();
		e2.deserialize(new ByteArrayInputStream(arr));
		Assert.assertTrue(Utils.equals(entity, e2));
	}
	
	public static void main(String[] args) throws ConversionException {
		TestCase4 tc3 = new TestCase4();
		tc3.setValues();
		tc3.testLength();
	}
}
