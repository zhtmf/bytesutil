package test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.Length;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Variant;
import org.dzh.bytesutil.converters.auxiliary.EntityHandler;
import org.junit.Assert;
import org.junit.Test;

import test.entity.ListEntity;

public class TestVariantForList {

	public static class Entity extends DataPacket{
		
		public int temp = 5;
		
		@Order(0)
		@Length
		public List<ListEntity> withoutVariant;
		
		@Order(1)
		@Length
		@Variant(VariantHandler.class)
		public List<ListEntity> withVariant;
	}
	
	public static class VariantHandler extends EntityHandler{
		@Override
		public DataPacket handle0(String fieldName, Object entity, InputStream is) throws IOException {
			Entity parent = (Entity)entity;
			ListEntity ret = new ListEntity();
			ret.temp = parent.temp;
			return ret;
		}
	}
	
	@Test
	public void test() throws Exception {
		Entity entity = new Entity();
		ListEntity sub1 = new ListEntity();
		sub1.a = 121;
		ListEntity sub2 = new ListEntity();
		sub2.a = 122;
		entity.withoutVariant = Arrays.asList(sub1);
		entity.withVariant = Arrays.asList(sub2);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		entity.serialize(baos);
		
		Entity dest = new Entity();
		dest.deserialize(new ByteArrayInputStream(baos.toByteArray()));
		
		Assert.assertEquals(dest.withoutVariant.size(), 1);
		Assert.assertEquals(dest.withVariant.size(), 1);
		
		Assert.assertEquals(dest.withoutVariant.get(0).a, 121);
		Assert.assertEquals(dest.withVariant.get(0).a, 122);
		
		Assert.assertEquals(dest.withoutVariant.get(0).temp, ListEntity.DEFAULT_TEMP);
		Assert.assertEquals(dest.withVariant.get(0).temp, entity.temp);
	}
	
	public static void main(String[] args) throws Exception {
		new TestVariantForList().test();
	}
}
