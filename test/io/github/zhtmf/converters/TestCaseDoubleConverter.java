package io.github.zhtmf.converters;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.junit.Test;

import io.github.zhtmf.ConversionException;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.BigEndian;
import io.github.zhtmf.annotations.modifiers.LittleEndian;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Signed;
import io.github.zhtmf.annotations.types.DOUBLE;

public class TestCaseDoubleConverter{
	
    @Test
    public void test0() throws ConversionException {
    	{
    		@Signed
            @BigEndian
        	class Entity extends DataPacket{
            	@Order(0)
            	@DOUBLE
            	public double field1;
            	@Order(1)
            	@DOUBLE
            	public Double field2;
            }
            
            
            {
            	ByteBuffer byteBuffer = ByteBuffer.allocate(Double.BYTES * 2);
                byteBuffer.order(ByteOrder.BIG_ENDIAN);
                byteBuffer.putDouble(131111.445234234);
                byteBuffer.putDouble(-10.00000000345);
                
                byte[] data = byteBuffer.array();
            	Entity entity = new Entity();
            	
                entity.deserialize(TestUtils.newInputStream(data));
                assertEquals(entity.field1, byteBuffer.getDouble(0), 0);
                assertEquals(entity.field2, byteBuffer.getDouble(8), 0);
                
                byte[] data2 = TestUtils.serializeAndGetBytes(entity);
                assertArrayEquals(data, data2);
            }
    	}
        
    	{

    		@Signed
            @LittleEndian
        	class Entity extends DataPacket{
            	@Order(0)
            	@DOUBLE
            	public double field1;
            	@Order(1)
            	@DOUBLE
            	public Double field2;
            }
            
            
            {
            	ByteBuffer byteBuffer = ByteBuffer.allocate(Double.BYTES * 2);
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                byteBuffer.putDouble(131111.445234234);
                byteBuffer.putDouble(-10.00000000345);
                
                byte[] data = byteBuffer.array();
            	Entity entity = new Entity();
            	
                entity.deserialize(TestUtils.newInputStream(data));
                assertEquals(entity.field1, byteBuffer.getDouble(0), 0);
                assertEquals(entity.field2, byteBuffer.getDouble(8), 0);
                
                byte[] data2 = TestUtils.serializeAndGetBytes(entity);
                assertArrayEquals(data, data2);
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
            	@DOUBLE
            	public double field1;
            	@Order(1)
            	@DOUBLE
            	public Double field2;
            	@Order(2)
            	@DOUBLE
            	public double field3;
            }
            
            
            {
            	ByteBuffer byteBuffer = ByteBuffer.allocate(Double.BYTES * 3);
                byteBuffer.order(ByteOrder.BIG_ENDIAN);
                byteBuffer.putDouble(Double.NaN);
                byteBuffer.putDouble(Double.POSITIVE_INFINITY);
                byteBuffer.putDouble(Double.NEGATIVE_INFINITY);
                
                byte[] data = byteBuffer.array();
            	Entity entity = new Entity();
            	
                entity.deserialize(TestUtils.newInputStream(data));
                assertTrue(Double.isNaN(entity.field1));
                assertEquals(entity.field1, byteBuffer.getDouble(0), 0);
                assertEquals(entity.field2, byteBuffer.getDouble(8), 0);
                assertEquals(entity.field3, byteBuffer.getDouble(16), 0);
                
                byte[] data2 = TestUtils.serializeAndGetBytes(entity);
                assertArrayEquals(data, data2);
            }
    	}
        
    	{
    		@Signed
            @LittleEndian
        	class Entity extends DataPacket{
            	@Order(0)
            	@DOUBLE
            	public double field1;
            	@Order(1)
            	@DOUBLE
            	public Double field2;
            	@Order(2)
            	@DOUBLE
            	public double field3;
            }
            
            
            {
            	ByteBuffer byteBuffer = ByteBuffer.allocate(Double.BYTES * 3);
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                byteBuffer.putDouble(Double.NaN);
                byteBuffer.putDouble(Double.POSITIVE_INFINITY);
                byteBuffer.putDouble(Double.NEGATIVE_INFINITY);
                
                byte[] data = byteBuffer.array();
            	Entity entity = new Entity();
            	
                entity.deserialize(TestUtils.newInputStream(data));
                assertTrue(Double.isNaN(entity.field1));
                assertEquals(entity.field1, byteBuffer.getDouble(0), 0);
                assertEquals(entity.field2, byteBuffer.getDouble(8), 0);
                assertEquals(entity.field3, byteBuffer.getDouble(16), 0);
                
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
        	@DOUBLE
        	public String field1;
        }
        {
        	ByteBuffer byteBuffer = ByteBuffer.allocate(Double.BYTES);
            byteBuffer.order(ByteOrder.BIG_ENDIAN);
            byteBuffer.putDouble(Double.NEGATIVE_INFINITY);
            
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
