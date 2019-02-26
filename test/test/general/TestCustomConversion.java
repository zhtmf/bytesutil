package test.general;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.TypeConverter;
import org.dzh.bytesutil.annotations.modifiers.BigEndian;
import org.dzh.bytesutil.annotations.modifiers.CHARSET;
import org.dzh.bytesutil.annotations.modifiers.Length;
import org.dzh.bytesutil.annotations.modifiers.ListLength;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Signed;
import org.dzh.bytesutil.annotations.types.UserDefined;
import org.junit.Assert;
import org.junit.Test;

import test.TestUtils;

public class TestCustomConversion {
	
	@Signed
	@BigEndian
	@CHARSET("UTF-8")
	public static final class Entity extends DataPacket{
		@Order(0)
		@UserDefined(Converter1.class)
		@Length(8)
		public Timestamp ts;
		@Order(1)
		@UserDefined(length=8,value=Converter1.class)
		public Timestamp ts2;
		@Order(2)
		@UserDefined(Converter1.class)
		@Length(8)
		@ListLength(2)
		public List<Timestamp> tsList;
		@Order(3)
		@UserDefined(length=8,value=Converter1.class)
		@ListLength(2)
		public List<Timestamp> tsList2;
	}
	
	@Test
	public void test() throws Exception {
		Entity entity = new Entity();
		Timestamp ts = Timestamp.valueOf("2011-1-1 23:00:59.333");
		entity.ts = ts;
		entity.ts2 = ts;
		entity.tsList = Arrays.asList(ts,ts);
		entity.tsList2 = Arrays.asList(ts,ts);
		TestUtils.serializeMultipleTimesAndRestore(entity);
		Assert.assertEquals(entity.length(), TestUtils.serializeAndGetBytes(entity).length);
	}
	
	public static class Converter1 extends TypeConverter{

		@Override
		public byte[] serialize(Object obj, Context context) {
			long n = ((Timestamp)obj).getTime();
			byte[] bytes = new byte[8];
			bytes[0] = (byte) (n>>56 & 0xFF);
			bytes[1] = (byte) (n>>48 & 0xFF);
			bytes[2] = (byte) (n>>40 & 0xFF);
			bytes[3] = (byte) (n>>32 & 0xFF);
			bytes[4] = (byte) (n>>24 & 0xFF);
			bytes[5] = (byte) (n>>16 & 0xFF);
			bytes[6] = (byte) (n>>8 & 0xFF);
			bytes[7] = (byte) (n & 0xFF);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			try {
				dos.writeLong(n);
				byte[] temp = baos.toByteArray();
				System.out.println(temp);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return bytes;
		}

		@Override
		public Object deserialize(byte[] data, Context context) {
			long ret = 0;
			ret |= (((long)data[0] & 0xFF) << 56);
			ret |= (((long)data[1] & 0xFF) << 48);
			ret |= (((long)data[2] & 0xFF) << 40);
			ret |= (((long)data[3] & 0xFF) << 32);
			ret |= (((long)data[4] & 0xFF) << 24);
			ret |= (((long)data[5] & 0xFF) << 16);
			ret |= (((long)data[6] & 0xFF) << 8);
			ret |= ((long)data[7] & 0xFF);
			return new Timestamp(ret);
		}
		
	}
}
