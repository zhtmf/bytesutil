package test.general;

import java.io.ByteArrayInputStream;
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
import org.dzh.bytesutil.annotations.modifiers.Unsigned;
import org.dzh.bytesutil.annotations.types.BCD;
import org.dzh.bytesutil.annotations.types.BYTE;
import org.dzh.bytesutil.annotations.types.CHAR;
import org.dzh.bytesutil.annotations.types.INT;
import org.dzh.bytesutil.annotations.types.RAW;
import org.dzh.bytesutil.annotations.types.SHORT;
import org.junit.Assert;
import org.junit.Test;

import test.TestUtils;

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
		@Order(6)
		@RAW
		@Length
		public byte[] tail2;
		@Order(7)
		@CHAR
		@Length
		@ListLength(2)
		public List<Byte> bytes;
		@Order(8)
		@BCD(2)
		public byte bcd;
		@Order(9)
		@BYTE
		@Signed
		public byte byteSigned;
		@Order(10)
		@BYTE
		@Unsigned
		public byte byteUnsigned;
		@Order(11)
		@CHAR
		@Length
		public short short1;
		@Order(12)
		@CHAR
		@Length
		public Short short2;
		@Order(13)
		@BCD(2)
		public short short3;
		@Order(14)
		@BYTE
		@Signed
		public short short4;
		@Order(15)
		@BYTE
		@Unsigned
		public short short5;
		@Order(16)
		@BYTE
		@Signed
		public long long1 = 120L;
		@Order(17)
		@BYTE
		@Unsigned
		public Long long2 = 240L;
		@Order(18)
		@SHORT
		@Signed
		public long long3 = Short.MAX_VALUE;
		@Order(19)
		@SHORT
		@Unsigned
		public Long long4 = (long) (((int)Short.MAX_VALUE)*2);
		@Order(20)
		@INT
		@Signed
		public long long5 = Integer.MAX_VALUE;
		@Order(21)
		@INT
		@Unsigned
		public Long long6 = ((long)Integer.MAX_VALUE)*2;
	}
	
	
	@Test
	public void test() throws ConversionException {
		Entity entity = new Entity();
		entity.i = 5;
		entity.str = "ccc";
		entity.str2 = "呵呵呵";
		entity.strList1 = Arrays.asList("ttt","qwert","哈哈哈");
		entity.byteArrayList = Arrays.asList(new byte[] {1,2,3},new byte[] {0,1,2});
		entity.tail2 = new byte[] {1,2,3};
		entity.bytes = Arrays.asList(Byte.valueOf((byte)13),Byte.valueOf((byte)14));
		entity.bcd = 100;
		entity.byteSigned = 120;
		entity.byteUnsigned = 119;
		entity.short2 = 32344;
		entity.short3 = 1919;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		entity.serialize(baos);
		Assert.assertEquals(baos.size(), entity.length());
		Entity restored = new Entity();
		restored.deserialize(new ByteArrayInputStream(baos.toByteArray()));
		Assert.assertTrue(TestUtils.equalsOrderFields(entity, restored));
	}
}
