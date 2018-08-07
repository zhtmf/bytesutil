package test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

import org.dzh.bytesutil.ConversionException;
import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.BigEndian;
import org.dzh.bytesutil.annotations.modifiers.CHARSET;
import org.dzh.bytesutil.annotations.modifiers.EOF;
import org.dzh.bytesutil.annotations.modifiers.EndsWith;
import org.dzh.bytesutil.annotations.modifiers.Length;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Signed;
import org.dzh.bytesutil.annotations.types.BCD;
import org.dzh.bytesutil.annotations.types.CHAR;
import org.dzh.bytesutil.annotations.types.RAW;
import org.junit.Assert;
import org.junit.Test;

import test.entity.Utils;

public class TestCase6{
	
	private Entity entity = new Entity();
	
	@Signed
	@BigEndian
	@CHARSET("UTF-8")
	public static final class Entity extends DataPacket{
		@Order(-1)
		@BCD(5)
		public long n1;
		
		@Order(1)
		@CHAR
		@EndsWith(",")
		public String char1;
		
		@Order(2)
		@CHAR
		@EndsWith("哦")
		public String char2;
		
		@Order(6)
		@RAW
		@Length
		public int[] ints;
		
		@Order(7)
		@CHAR
		@EndsWith("\r\n")
		@EOF
		public List<String> strWithEndMarks;
	}
	
	
	@Test
	public void testLength() throws ConversionException {
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			entity.n1 = 1234567070L;
			entity.char1 = "abcdef";
			entity.char2 = "啊啊啊";
			entity.ints = new int[] {120,55,-32,-1};
			entity.strWithEndMarks = Arrays.asList("abcsssssssssssss","def","ghi");
			entity.serialize(baos);
			byte[] arr = baos.toByteArray();
			{
				Entity e2 = new Entity();
				e2.deserialize(new ByteArrayInputStream(arr));
				Assert.assertTrue(Utils.equals(entity, e2));
			}
			{
				Entity e2 = new Entity();
				e2.deserialize(new ByteArrayInputStream(arr));
				Assert.assertTrue(Utils.equals(entity, e2));
			}
		}
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			entity.n1 = 1234567070L;
			entity.char1 = "asdfasd啊啊啊fx啊zc啊v";
			entity.char2 = "啊啊啊ywwuyi呵呵呵";
			entity.ints = new int[] {1,1,1,127};
			entity.strWithEndMarks = Arrays.asList("abcpppppppppppppp","de        f","ghjjjjjjjjjjjji");
			entity.serialize(baos);
			byte[] arr = baos.toByteArray();
			{
				Entity e2 = new Entity();
				e2.deserialize(new ByteArrayInputStream(arr));
				Assert.assertTrue(Utils.equals(entity, e2));
			}
			{
				Entity e2 = new Entity();
				e2.deserialize(new ByteArrayInputStream(arr));
				Assert.assertTrue(Utils.equals(entity, e2));
			}
		}
	}
	
	public static void main(String[] args) throws ConversionException {
		TestCase6 tc3 = new TestCase6();
		tc3.testLength();
	}
}
