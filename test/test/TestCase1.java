package test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.dzh.bytesutil.ConversionException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import test.entity.MyEntity;
import test.entity.SubEntity;
import test.entity.Utils;
import test.entity.WeirdEntity;
import test.hierarchy.Sub1;
import test.hierarchy.Sub2;

public class TestCase1 {
	
	private MyEntity entity = new MyEntity();
	
	@Before
	public void setValues() {
		entity.a = 120;
		entity.b = 110;
		entity.c = -1000;
		entity.d = 255;
		entity.e = 65535;
		entity.f = Integer.MIN_VALUE;
		entity.z = -100;
		entity.str = "abcdef";
		entity.str2 = "啊啊啊";
		entity.bcd = "20180909";
		entity.status = 'Y';
		entity.status2 = 'N';
		entity.sub = new SubEntity(30, "0123456789abcde");
		entity.strList = Arrays.asList("1234","2456","haha");
		entity.list3 = Arrays.asList("1234","abcd","defg","hijk","lmno");
		entity.subEntityList = Arrays.asList(new SubEntity(-3142, "0123456789abcde"),new SubEntity(5000,"0123456789fffff"));
		entity.unusedLength = 0;
		entity.entityList2 = new ArrayList<SubEntity>();
		entity.bytes = new byte[] {0x1,0x2};
		entity.byteList = Arrays.asList(new byte[] {0x1,0x2,0x5},new byte[]{0x3,0x4,0x6});
		entity.bytes2Len = 5;
		entity.anotherBytes = new byte[] {0x1,0x2,0x5,0x1,0x2};
		entity.date = new Date(0);
		entity.date2 = new Date(0); //milliseconds different?
		entity.veryLong = ((long)Integer.MAX_VALUE)*2;
		Sub2 s2 = new Sub2();
		s2.type = 2;
		s2.time = "19990101";
		s2.str1 = "123456";
		s2.str2 = "A";
		s2.str3 = "MF";
		s2.type2 = 1;
		s2.str4 = "hahahahaha";
		entity.variantEntity = s2;
		Sub1 s1 = new Sub1();
		s1.type = 1;
		s1.time = "20000202";
		s1.field1 = -350;
		s1.field2 = 30000;
		entity.anotherEntity = s1;
		
		WeirdEntity we = new WeirdEntity();
		we.char1 = "abcdef";
		we.char2 = "hahahahahaha";
		we.char3 = we.char1;
		entity.we = we;
	}
	
	@Test
	public void testPerformance() throws ConversionException {
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		{
			long st = System.currentTimeMillis();
			for(int i=0;i<100000;++i) {
				baos.reset();
				entity.serialize(baos);
			}
			long elapsed = System.currentTimeMillis() - st;
			System.out.println("time elapsed:"+elapsed);
			Assert.assertTrue("time elapsed:"+elapsed, elapsed<4000);
		}
		
		{
			long st = System.currentTimeMillis();
			byte[] bts = baos.toByteArray();
			ByteArrayInputStream bais = new ByteArrayInputStream(bts);
			bais.mark(Integer.MAX_VALUE);
			MyEntity entity2 = null;
			for(int i=0;i<100000;++i) {
				bais.reset();
				entity2 = new MyEntity();
				entity2.deserialize(bais);
			}
			long elapsed = System.currentTimeMillis() - st;
			System.out.println("time elapsed:"+elapsed);
			Assert.assertTrue(Utils.equals(entity, entity2));
			Assert.assertTrue("time elapsed:"+elapsed, elapsed<4000);
		}
	}

	@Test
	public void testEntity1() throws ConversionException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		entity.serialize(baos);
		MyEntity entity2 = new MyEntity();
		final byte[] bts = baos.toByteArray();
		entity2.deserialize(new ByteArrayInputStream(bts));
		Assert.assertTrue(Utils.equals(entity,entity2));
	}
}
