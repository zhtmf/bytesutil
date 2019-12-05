package io.github.zhtmf.converters;

import org.junit.Assert;
import org.junit.Test;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.LittleEndian;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Signed;
import io.github.zhtmf.annotations.types.Bit;
import io.github.zhtmf.annotations.types.INT;
import io.github.zhtmf.annotations.types.SHORT;
import io.github.zhtmf.converters.TestUtils.Provider;

public class TestBIT {

    @Test
    public void test0() {
        reverseBits((byte)0b1,1,(byte)0b1);
        reverseBits((byte)0b10,2,(byte)0b1);
        reverseBits((byte)0b011,3,(byte)0b110);
        reverseBits((byte)0b1011,4,(byte)0b1101);
        reverseBits((byte)0b10111,5,(byte)0b11101);
        reverseBits((byte)0b100011,6,(byte)0b110001);
        reverseBits((byte)0b0100011,7,(byte)0b1100010);
        reverseBits((byte)0b00100011,8,(byte)0b11000100);
    }
    
    private void reverseBits(byte src, int count, byte expected) {
        byte ret = StreamUtils.reverseNBits(src,count);
        Assert.assertEquals(Integer.toBinaryString(ret), ret, expected);
    }
    
    /*
     * TODO: Exceptions
     * TODO: List of Boolean/Byte
     * TODO: BIT to Numeric Enums
     * FIXME: map numeric enums according to their ordinal number
     */
    
    //General BIT fields conversion
    //more than one group of BIT fields in the same class
    @LittleEndian
    public static class Entity extends DataPacket{
        @INT
        @Order(0)
        public int num1;
        @Bit
        @Order(1)
        public boolean b1;
        @Bit
        @Order(2)
        public boolean b2;
        @Bit(4)
        @Order(3)
        public byte num2;
        @Bit
        @Order(4)
        public boolean b3;
        @Bit
        @Order(5)
        public boolean b4;
        @INT
        @Order(6)
        public int num3;
    }
    
    @Test
    public void testBit() throws Exception {
        Entity entity = new Entity();
        entity.deserialize(TestUtils.newInputStream(new byte[] {
                15,0,0,0,(byte) 0b11011101,127,0,0,0
        }));
        Assert.assertEquals(entity.num1, 15);
        Assert.assertEquals(entity.b1, true);
        Assert.assertEquals(entity.b2, true);
        Assert.assertEquals(entity.num2, 0b1110);
        Assert.assertEquals(entity.b3, false);
        Assert.assertEquals(entity.b4, true);
        Assert.assertEquals(entity.num3, 127);
        
        TestUtils.serializeMultipleTimesAndRestore(entity,10);
    }
    
    //BIT fields at the beginning or at the end of a class 
    @Test
    public void testBit2() throws Exception {
        
        @Signed
        class Entity2 extends DataPacket{
            @Bit
            @Order(1)
            public boolean b1 = false;
            @Bit
            @Order(2)
            public boolean b2 = true;
            @Bit(6)
            @Order(3)
            public byte num2 = 7;
            @SHORT
            @Order(4)
            public short sht = 12000;
            @Bit
            @Order(5)
            public boolean b3 = true;
            @Bit
            @Order(6)
            public boolean b4 = false;
            @Bit(6)
            @Order(7)
            public byte num3 = 58;
        }
        TestUtils.serializeMultipleTimesAndRestore(new Entity2(), 10, new Provider<Entity2>() {

            @Override
            public Entity2 newInstance() {
                return new Entity2();
            }
        });
    }
    
    //BIT fields with value of BIT from 1-7
    //BIT fields with value of BIT greater than 1 converted from little-endian streams
    @Test
    @SuppressWarnings("hiding")
    public void testBit3() throws Exception {
        
        @LittleEndian
        class Entity extends DataPacket{
            @Bit
            @Order(1)
            public boolean b1 = false;
            @Bit(2)
            @Order(2)
            public byte num1 = 3;
            @Bit(3)
            @Order(3)
            public byte num2 = 7;
            @Bit(2)
            @Order(4)
            public byte num3 = 2; //01111101
            
            @Bit(4)
            @Order(5)
            public byte num4 = 14;
            @Bit(4)
            @Order(6)
            public byte num5 = 10;
            
            @Bit(5)
            @Order(7)
            public byte num6 = 31;
            @Bit(2)
            @Order(8)
            public byte num7 = 3;
            @Bit(1)
            @Order(9)
            public boolean b2 = true;
            
            @Bit(6)
            @Order(10)
            public byte num8 = 63;
            @Bit(2)
            @Order(11)
            public byte num9 = 3;
            
            @Bit(7)
            @Order(12)
            public byte num10 = 127;
            @Bit
            @Order(13)
            public boolean b3 = false;
            
        }
        TestUtils.serializeMultipleTimesAndRestore(new Entity(), 10, new Provider<Entity>() {

            @Override
            public Entity newInstance() {
                return new Entity();
            }
        });
        
        byte[] bytes = TestUtils.serializeAndGetBytes(new Entity());
        Assert.assertEquals(bytes[0], 0b01111101);
        Assert.assertEquals(bytes[1], 0b01110101);
        Assert.assertEquals(bytes[2], (byte)0b11111111);
        Assert.assertEquals(bytes[3], (byte)0b11111111);
        Assert.assertEquals(bytes[4], (byte)0b11111110);
    }
}
