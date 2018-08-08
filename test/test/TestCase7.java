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
import org.dzh.bytesutil.annotations.modifiers.ListLength;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Signed;
import org.dzh.bytesutil.annotations.types.BCD;
import org.dzh.bytesutil.annotations.types.CHAR;
import org.dzh.bytesutil.annotations.types.INT;
import org.junit.Assert;
import org.junit.Test;

import test.entity.Utils;

public class TestCase7{
	
	private Entity entity = new Entity();
	
	@Signed
	@BigEndian
	@CHARSET("UTF32")
	public static final class Entity extends DataPacket{
		@Order(-1)
		@BCD(5)
		public long n1;
		
		@Order(7)
		@CHAR
		@EndsWith("呜呜呜")
		@ListLength(3)
		public List<String> strWithEndMarks;
		
		@Order(8)
		@EOF
		public List<Sub> subWithEndMarks;
	}
	
	@Signed
	@BigEndian
	@CHARSET("UTF-8")
	public static final class Sub extends DataPacket{
		@Order(0)
		@INT
		public long n2;
		@Order(1)
		@CHAR
		@EndsWith(",")
		public String s2;
		@Order(2)
		@CHAR
		@EndsWith(",")
		public String s3;
		@Order(3)
		@CHAR
		@EndsWith("\r\n")
		public String s4;
		public Sub(long n2, String s2, String s3, String s4) {
			super();
			this.n2 = n2;
			this.s2 = s2;
			this.s3 = s3;
			this.s4 = s4;
		}
		public Sub() {
		}
	}
	
	
	@Test
	public void testLength() throws ConversionException {
		entity.n1 = 123423234L;
		entity.strWithEndMarks = Arrays.asList("啊啊啊","呵呵呵","sadfsdf深刻搭街坊零零sadfasdf");
		entity.subWithEndMarks = Arrays.asList(new Sub(333L, "我我我", "asdfasdf", "啊ccc啊"),new Sub(444L, "ninini", "ttttttt", "圣诞快乐fjk"));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
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
		baos = new ByteArrayOutputStream();
		for(int i=0;i<10;++i) {
			entity.serialize(baos);
		}
		for(int i=0;i<10;++i) {
			Entity e2 = new Entity();
			e2.deserialize(new ByteArrayInputStream(arr));
			Assert.assertTrue(Utils.equals(entity, e2));
		}
	}
	
	public static void main(String[] args) throws ConversionException {
		TestCase7 tc3 = new TestCase7();
		tc3.testLength();
	}
}
