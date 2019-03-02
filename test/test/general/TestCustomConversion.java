package test.general;

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
		public void serialize(Object obj, TypeConverter.Output context) throws IOException {
			context.writeLong(((Timestamp)obj).getTime());
		}

		@Override
		public Object deserialize(Input input) throws IOException{
			return new Timestamp(input.readLong());
		}
		
	}
}
