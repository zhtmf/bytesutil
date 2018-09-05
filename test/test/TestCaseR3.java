package test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.dzh.bytesutil.ConversionException;
import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.EOF;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Unsigned;
import org.dzh.bytesutil.annotations.types.CHAR;
import org.dzh.bytesutil.annotations.types.SHORT;
import org.junit.Assert;
import org.junit.Test;

import test.entity.Utils;

public class TestCaseR3 {
	
	@Unsigned
	public static class Entity extends DataPacket{
		@Order(0)
		@SHORT
		public int header;
		@Order(1)
		@SHORT
		public int serial;
		@Order(2)
		@CHAR
		@EOF
		public String data;
	}
	
	@Unsigned
	public static class Entity2 extends DataPacket{
		public int header;
		public int serial;
		public String data;
	}
	
	@Test
	public void testOrder() throws ConversionException {
		Entity ent = new Entity();
		ent.header = 1;
		ent.serial = 65533;
		ent.data = "sdfkjlasjkdlfjklasjdklf";
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ent.serialize(baos);
		Assert.assertEquals(baos.toByteArray().length, ent.length());
		Entity ent2 = new Entity();
		ent2.deserialize(new ByteArrayInputStream(baos.toByteArray()));
		Assert.assertTrue(Utils.equals(ent, ent2));
	}
}
