package test.general;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

import org.dzh.bytesutil.ConversionException;
import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.BigEndian;
import org.dzh.bytesutil.annotations.modifiers.CHARSET;
import org.dzh.bytesutil.annotations.modifiers.Length;
import org.dzh.bytesutil.annotations.modifiers.ListLength;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Signed;
import org.dzh.bytesutil.annotations.types.CHAR;
import org.dzh.bytesutil.annotations.types.INT;
import org.dzh.bytesutil.annotations.types.RAW;
import org.junit.Assert;
import org.junit.Test;

public class TestCase91{
	
	@Signed
	@BigEndian
	@CHARSET("UTF-8")
	public static final class Entity extends DataPacket{
		@Order(-2)
		@INT
		public int i;
		@Order(-1)
		@CHAR
		@Length
		public String str;
		@Order(1)
		@CHAR(9)
		public String str2;
		@Order(2)
		@CHAR
		@Length
		@ListLength(3)
		public List<String> strList1;
		@Order(3)
		@RAW
		@Length
		@ListLength(2)
		public List<byte[]> byteArrayList;
		@Order(4)
		@RAW(2)
		@Length(0)
		public List<byte[]> byteArrayList2;
		@Order(5)
		@RAW
		@Length
		public byte[] tail;
		@Order(6)
		@RAW
		@Length
		public byte[] tail2;
	}
	
	
	@Test
	public void testLength() throws ConversionException {
		Entity entity = new Entity();
		entity.i = 5;
		entity.str = null;
		entity.str2 = "呵呵呵";
		entity.strList1 = Arrays.asList(null,null,"哈哈哈");
		entity.byteArrayList = Arrays.asList(null,new byte[] {0,1,2});
		entity.tail2 = new byte[] {1,2,3};
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		entity.serialize(baos);
		Assert.assertEquals(baos.size(), entity.length());
	}
}
