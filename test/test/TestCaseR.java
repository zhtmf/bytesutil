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
import org.dzh.bytesutil.annotations.types.INT;
import org.dzh.bytesutil.annotations.types.SHORT;
import org.dzh.bytesutil.converters.auxiliary.EntityHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import test.entity.Utils;

/*
 * test nested marking for MarkableInputStream
 */
public class TestCaseR {
	
	private Entity2 entity = new Entity2();
	private Inner inner = new Inner();
	private Inner2 inner2 = new Inner2();
	
	@Before
	public void setValues() {
		entity.b1 = 0x55DF;
		entity.b2 = 0x66FF;
		entity.b3 = 0xFFDE;
		entity.b4 = 0xFFAA;
		entity.b5 = 0xFFBA;
		inner.b1 = 0x55DF;
		inner.b2 = 0x66FF;
		inner.b3 = 0xFFDE;
		inner2.i1 = 0xFFDA;
		inner2.i2 = 0x0FFFDABA;
		inner2.i3 = 30;
		inner2.i4 = 111130;
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
	public static final class Inner2 extends DataPacket{
		@Order(0)
		@SHORT
		public int i1;
		@Order(1)
		@INT
		public long i2;
		@Order(2)
		@INT
		public int i3;
		@Order(3)
		@INT
		public int i4;
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
		public DataPacket body;
		@Order(4)
		@SHORT
		public int b4;
		@Order(5)
		@SHORT
		public int b5;
		
		public static final class handler extends EntityHandler{

			@Override
			public DataPacket handle0(String fieldName, Object entity, InputStream is) throws IOException {
				int b = is.read();
				int b2 = is.read();
				int n = b2<<8 | b;
				if(n == 0x55DF) {
					return new Inner();
				}else if(n==0xFFDA) {
					return new Inner2();
				}else {
					throw new Error();
				}
			}
		};
	}
	
	@Test
	public void testOrder() throws ConversionException {
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			entity.body = inner;
			entity.serialize(baos);
			byte[] arr = baos.toByteArray();
			Entity2 e2 = new Entity2();
			e2.deserialize(new ByteArrayInputStream(arr));
			Assert.assertTrue(Utils.equals(entity, e2));
		}
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			entity.body = inner;
			entity.serialize(baos);
			entity.body = inner2;
			entity.serialize(baos);
			byte[] arr = baos.toByteArray();
			ByteArrayInputStream bais = new ByteArrayInputStream(arr);
			{
				Entity2 e2 = new Entity2();
				e2.deserialize(bais);
				entity.body = inner;
				Assert.assertTrue(Utils.equals(entity, e2));
			}
			{
				Entity2 e2 = new Entity2();
				e2.deserialize(bais);
				entity.body = inner2;
				Assert.assertTrue(Utils.equals(entity, e2));
			}
		}
	}
}
