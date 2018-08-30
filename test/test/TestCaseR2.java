package test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.dzh.bytesutil.ConversionException;
import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.Length;
import org.dzh.bytesutil.annotations.modifiers.ListLength;
import org.dzh.bytesutil.annotations.modifiers.LittleEndian;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Unsigned;
import org.dzh.bytesutil.annotations.types.CHAR;
import org.dzh.bytesutil.annotations.types.INT;
import org.dzh.bytesutil.converters.auxiliary.DataType;
import org.dzh.bytesutil.converters.auxiliary.ModifierHandler;
import org.junit.Assert;
import org.junit.Test;

public class TestCaseR2 {
	
	@LittleEndian
	@Unsigned
	public static class Entity extends DataPacket{
		//list with write-ahead length
		@Order(1)
		@Length(type=DataType.SHORT)
		@CHAR(5)
		public List<String> str;
		//list with definite length
		@Order(2)
		@Length(3)
		@CHAR(1)
		public List<String> str2;
		//list with write-ahead length, but null
		@Order(3)
		@Length
		@CHAR(1)
		public List<String> nullList;
		//list with handler
		@Order(4)
		@Length(handler=Handler1.class)
		@CHAR(1)
		public List<String> str3;
		
		//same list, but marked with ListLength
		
		//list with write-ahead length
		@Order(5)
		@ListLength(type=DataType.SHORT)
		@INT
		public List<Integer> integer1;
		//list with definite length
		@Order(6)
		@ListLength(3)
		@INT
		public List<Integer> integer2;
		//list with write-ahead length, but null
		@Order(7)
		@ListLength
		@INT
		public List<Integer> nullList2;
		//list with handler
		@Order(8)
		@ListLength(handler=Handler1.class)
		@INT
		public List<Integer> integer3;
		
		public static final class Handler1 extends ModifierHandler<Integer>{
			@Override
			public Integer handleDeserialize0(String fieldName, Object entity, InputStream is) {
				return 2;
			}

			@Override
			public Integer handleSerialize0(String fieldName, Object entity) {
				return 2;
			}
		}
	}
	
	@Test
	public void testOrder() throws ConversionException {
		Entity ent = new Entity();
		ent.str = Arrays.asList("abcde","ttttt");
		ent.str2 = Arrays.asList("a","b","c");
		ent.str3 = Arrays.asList("d","f");
		ent.integer1 = Arrays.asList(123,444,555,77777);
		ent.integer2 = Arrays.asList(111,222,333);
		ent.integer3 = Arrays.asList(1,2);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ent.serialize(baos);
		Assert.assertEquals(baos.toByteArray().length, ent.length());
	}
}
