package test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.dzh.bytesutil.ConversionException;
import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.LittleEndian;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Unsigned;
import org.dzh.bytesutil.annotations.modifiers.Variant;
import org.dzh.bytesutil.annotations.types.SHORT;
import org.dzh.bytesutil.converters.auxiliary.EntityHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/*
 * test nested marking for MarkableInputStream
 */
public class TestCaseR {
	
	private Entity2 entity = new Entity2();
	
	@Before
	public void setValues() {
		entity.b1 = 0x55DF;
		entity.b2 = 0x66FF;
		entity.b3 = 0xFFDE;
		entity.inner = new Inner();
		entity.inner.b1 = 0x55DF;
		entity.inner.b2 = 0x66FF;
		entity.inner.b3 = 0xFFDE;
	}
	
	@Unsigned
	@LittleEndian
	public static final class Inner extends DataPacket{
		@Order(0)
		@SHORT
		public int b1;
		@Order(1)
		@SHORT
		public int b2;
		@Order(2)
		@SHORT
		public int b3;
	}
	
	@Unsigned
	@LittleEndian
	public static final class Entity2 extends DataPacket{
		@Order(0)
		@SHORT
		public int b1;
		@Order(1)
		@SHORT
		public int b2;
		@Order(2)
		@SHORT
		public int b3;
		@Order(3)
		@Variant(handler.class)
		public Inner inner;
		
		public static final class handler extends EntityHandler{

			@Override
			public DataPacket handle0(String fieldName, Object entity, InputStream is) throws IOException {
				int b = is.read();
				int b2 = is.read();
				Assert.assertEquals((b2<<8 | b), 0x55DF);
				return new Inner();
			}
		};
	}
	
	@Test
	public void testOrder() throws ConversionException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		entity.serialize(baos);
		byte[] arr = baos.toByteArray();
		Entity2 e2 = new Entity2();
		e2.deserialize(new ByteArrayInputStream(arr));
		Assert.assertEquals(entity.b1, e2.b1);
		Assert.assertEquals(entity.b2, e2.b2);
		Assert.assertEquals(entity.b3, e2.b3);
		Assert.assertEquals(entity.inner.b1, e2.inner.b1);
		Assert.assertEquals(entity.inner.b2, e2.inner.b2);
		Assert.assertEquals(entity.inner.b3, e2.inner.b3);
	}
}
