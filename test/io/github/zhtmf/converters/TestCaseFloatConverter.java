package io.github.zhtmf.converters;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.junit.Test;

import io.github.zhtmf.ConversionException;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.BigEndian;
import io.github.zhtmf.annotations.modifiers.LittleEndian;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Signed;
import io.github.zhtmf.annotations.types.FLOAT;

public class TestCaseFloatConverter{
	
    @Test
    public void test0() throws ConversionException {
    	{
    		@Signed
            @BigEndian
        	class Entity extends DataPacket{
            	@Order(0)
            	@FLOAT
            	public float field1;
            	@Order(1)
            	@FLOAT
            	public Float field2;
            }
            
            
            {
            	ByteBuffer byteBuffer = ByteBuffer.allocate(Float.BYTES * 2);
                byteBuffer.order(ByteOrder.BIG_ENDIAN);
                byteBuffer.putFloat(131111.445234234f);
                byteBuffer.putFloat(-101.00000000345f);
                
                byte[] data = byteBuffer.array();
            	Entity entity = new Entity();
            	
                entity.deserialize(TestUtils.newInputStream(data));
                assertEquals(entity.field1, byteBuffer.getFloat(0), 0);
                assertEquals(entity.field2, byteBuffer.getFloat(Float.BYTES), 0);
                
                byte[] data2 = TestUtils.serializeAndGetBytes(entity);
                assertArrayEquals(data, data2);
            }
    	}
        
    	{

    		@Signed
            @LittleEndian
        	class Entity extends DataPacket{
            	@Order(0)
            	@FLOAT
            	public float field1;
            	@Order(1)
            	@FLOAT
            	public Float field2;
            }
            
            
            {
            	ByteBuffer byteBuffer = ByteBuffer.allocate(Float.BYTES * 2);
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                byteBuffer.putFloat(131111.445234234f);
                byteBuffer.putFloat(-101111234.00000000345f);
                
                byte[] data = byteBuffer.array();
            	Entity entity = new Entity();
            	
                entity.deserialize(TestUtils.newInputStream(data));
                assertEquals(entity.field1, byteBuffer.getFloat(0), 0);
                assertEquals(entity.field2, byteBuffer.getFloat(Float.BYTES), 0);
                
                byte[] data2 = TestUtils.serializeAndGetBytes(entity);
                assertArrayEquals(data, data2);
            }
    	
    	}
        
    }
    
    @Test
    public void test01() throws ConversionException {
    	{
    		@Signed
            @BigEndian
        	class Entity extends DataPacket{
            	@Order(0)
            	@FLOAT
            	public double field1;
            	@Order(1)
            	@FLOAT
            	public Double field2;
            }
            
            
            {
            	ByteBuffer byteBuffer = ByteBuffer.allocate(Float.BYTES * 2);
                byteBuffer.order(ByteOrder.BIG_ENDIAN);
                byteBuffer.putFloat(131111.445234234f);
                byteBuffer.putFloat(-101.0003f);
                
                byte[] data = byteBuffer.array();
            	Entity entity = new Entity();
            	
                entity.deserialize(TestUtils.newInputStream(data));
                assertEquals(entity.field1, byteBuffer.getFloat(0), 0);
                assertEquals(entity.field2, byteBuffer.getFloat(Float.BYTES), 0);
                
                byte[] data2 = TestUtils.serializeAndGetBytes(entity);
                assertArrayEquals(data, data2);
            }
    	}
        
    	{

    		@Signed
            @LittleEndian
        	class Entity extends DataPacket{
            	@Order(0)
            	@FLOAT
            	public double field1;
            	@Order(1)
            	@FLOAT
            	public Double field2;
            }
            
            
            {
            	ByteBuffer byteBuffer = ByteBuffer.allocate(Float.BYTES * 2);
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                byteBuffer.putFloat(131111.445234234f);
                byteBuffer.putFloat(-101111234.00000000345f);
                
                byte[] data = byteBuffer.array();
            	Entity entity = new Entity();
            	
                entity.deserialize(TestUtils.newInputStream(data));
                assertEquals(entity.field1, byteBuffer.getFloat(0), 0);
                assertEquals(entity.field2, byteBuffer.getFloat(Float.BYTES), 0);
                
                byte[] data2 = TestUtils.serializeAndGetBytes(entity);
                assertArrayEquals(data, data2);
            }
    	
    	}
        
    }
    @Test
    public void testExceptions() throws ConversionException {
    	{
    		@Signed
            @BigEndian
        	class Entity extends DataPacket{
            	@Order(0)
            	@FLOAT
            	public double field1;
            }
    		/**
    		 * floating numbers are approximates, 
    		 * so numbers with too few digits cannot make difference in the final result
    		 */
            {
            	Entity entity = new Entity();
            	entity.field1 = (double)Float.MAX_VALUE + 3000000000000000000000000000d;
            	
                try {
					TestUtils.serializeAndGetBytes(entity);
					fail();
				} catch (Exception e) {
					TestUtils.assertExactException(e, DoubleConverter.class, 1);
				}
            }
            {
            	Entity entity = new Entity();
            	entity.field1 = (double)-Float.MAX_VALUE - 1000000000000000000000000000d;
                try {
					TestUtils.serializeAndGetBytes(entity);
					fail();
				} catch (Exception e) {
					TestUtils.assertExactException(e, DoubleConverter.class, 1);
				}
            }
            {
            	Entity entity = new Entity();
            	entity.field1 = (double)Float.MIN_VALUE - 0.0000000000000000000000000000000000000000000000000005d;
                try {
					TestUtils.serializeAndGetBytes(entity);
					fail();
				} catch (Exception e) {
					TestUtils.assertExactException(e, DoubleConverter.class, 1);
				}
            }
    	}
    }
    
    @Test
    public void test1() throws ConversionException {
    	{
    		@Signed
            @BigEndian
        	class Entity extends DataPacket{
            	@Order(0)
            	@FLOAT
            	public float field1;
            	@Order(1)
            	@FLOAT
            	public Float field2;
            	@Order(2)
            	@FLOAT
            	public float field3;
            }
            
            
            {
            	ByteBuffer byteBuffer = ByteBuffer.allocate(Float.BYTES * 3);
                byteBuffer.order(ByteOrder.BIG_ENDIAN);
                byteBuffer.putFloat(Float.NaN);
                byteBuffer.putFloat(Float.POSITIVE_INFINITY);
                byteBuffer.putFloat(Float.NEGATIVE_INFINITY);
                
                byte[] data = byteBuffer.array();
            	Entity entity = new Entity();
            	
                entity.deserialize(TestUtils.newInputStream(data));
                assertTrue(Float.isNaN(entity.field1));
                assertEquals(entity.field1, byteBuffer.getFloat(0), 0);
                assertEquals(entity.field2, byteBuffer.getFloat(Float.BYTES), 0);
                assertEquals(entity.field3, byteBuffer.getFloat(Float.BYTES * 2), 0);
                
                byte[] data2 = TestUtils.serializeAndGetBytes(entity);
                assertArrayEquals(data, data2);
            }
    	}
        
    	{
    		@Signed
            @LittleEndian
        	class Entity extends DataPacket{
            	@Order(0)
            	@FLOAT
            	public float field1;
            	@Order(1)
            	@FLOAT
            	public Float field2;
            	@Order(2)
            	@FLOAT
            	public float field3;
            }
            
            
            {
            	ByteBuffer byteBuffer = ByteBuffer.allocate(Float.BYTES * 3);
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                byteBuffer.putFloat(Float.NaN);
                byteBuffer.putFloat(Float.POSITIVE_INFINITY);
                byteBuffer.putFloat(Float.NEGATIVE_INFINITY);
                
                byte[] data = byteBuffer.array();
            	Entity entity = new Entity();
            	
                entity.deserialize(TestUtils.newInputStream(data));
                assertTrue(Float.isNaN(entity.field1));
                assertEquals(entity.field1, byteBuffer.getFloat(0), 0);
                assertEquals(entity.field2, byteBuffer.getFloat(Float.BYTES), 0);
                assertEquals(entity.field3, byteBuffer.getFloat(Float.BYTES * 2), 0);
                
                byte[] data2 = TestUtils.serializeAndGetBytes(entity);
                assertArrayEquals(data, data2);
            }
    	}
        
    }
    
    @Test
    public void test2() throws ConversionException {
		@Signed
        @BigEndian
    	class Entity extends DataPacket{
        	@Order(0)
        	@FLOAT
        	public String field1;
        }
        {
        	ByteBuffer byteBuffer = ByteBuffer.allocate(Float.BYTES);
            byteBuffer.order(ByteOrder.BIG_ENDIAN);
            byteBuffer.putFloat(Float.NEGATIVE_INFINITY);
            
            byte[] data = byteBuffer.array();
        	Entity entity = new Entity();
        	
            try {
				entity.deserialize(TestUtils.newInputStream(data));
			} catch (Exception e) {
				TestUtils.assertExactException(e, FieldInfo.class, 1);
			}
        }
    }
}
