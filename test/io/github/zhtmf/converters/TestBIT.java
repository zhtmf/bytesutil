package io.github.zhtmf.converters;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.BigEndian;
import io.github.zhtmf.annotations.modifiers.Conditional;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.ListLength;
import io.github.zhtmf.annotations.modifiers.LittleEndian;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Signed;
import io.github.zhtmf.annotations.modifiers.Variant;
import io.github.zhtmf.annotations.types.BYTE;
import io.github.zhtmf.annotations.types.Bit;
import io.github.zhtmf.annotations.types.INT;
import io.github.zhtmf.annotations.types.SHORT;
import io.github.zhtmf.converters.FieldInfo.EnumFieldInfo;
import io.github.zhtmf.converters.TestBIT.Entity1.Sub1;
import io.github.zhtmf.converters.TestUtils.Provider;
import io.github.zhtmf.converters.auxiliary.EntityHandler;
import io.github.zhtmf.converters.auxiliary.ModifierHandler;

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
        public Boolean b2;
        @Bit(4)
        @Order(3)
        public Byte num2;
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
        Assert.assertEquals((byte)entity.num2, 0b1110);
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
    
    //List of Boolean/Byte
    @Test
    public void testBit4() throws Exception {
        @SuppressWarnings("hiding")
        @BigEndian
        class Entity extends DataPacket{
            @Bit
            @Order(1)
            public boolean b1;
            @Bit
            @Order(2)
            @Length(1)
            @ListLength(7)
            public List<Boolean> flags;
            @Bit
            @Order(3)
            public boolean b2;
            @Bit
            @Order(4)
            @ListLength(4)
            public List<Boolean> flags2;
            @Bit(3)
            @Order(5)
            public byte byte1;
            @Bit(7)
            @Order(6)
            public byte byte2;
            @Bit
            @Order(7)
            @ListLength(1)
            public List<Boolean> flags3;
        }
        Entity entity = new Entity();
        entity.deserialize(TestUtils.newInputStream(new byte[] {(byte) 0b10000111,(byte) 0b01111001,(byte) 0b01111001}));
        TestUtils.serializeMultipleTimesAndRestore(entity, 10, new Provider<Entity>() {

            @Override
            public Entity newInstance() {
                return new Entity();
            }
        });
        Assert.assertTrue(entity.b1);
        Assert.assertTrue(!entity.flags.get(0));
        Assert.assertTrue(!entity.flags.get(1));
        Assert.assertTrue(!entity.flags.get(2));
        Assert.assertTrue(!entity.flags.get(3));
        Assert.assertTrue(entity.flags.get(4));
        Assert.assertTrue(entity.flags.get(5));
        Assert.assertTrue(entity.flags.get(6));
        Assert.assertTrue(!entity.b2);
        Assert.assertTrue(entity.flags2.get(0));
        Assert.assertTrue(entity.flags2.get(1));
        Assert.assertTrue(entity.flags2.get(2));
        Assert.assertEquals(entity.byte1,1);
        Assert.assertEquals(entity.byte2,0b0111100);
        Assert.assertTrue(entity.flags3.get(0));
    }
    
    public static enum TestEnum1{
        A,B,C,D,E,F,G,H
    }
    
    //BIT to Numeric Enums
    @Test
    public void testBit5() throws Exception {
        @SuppressWarnings("hiding")
        @BigEndian
        class Entity extends DataPacket{
            @Bit(3)
            @Order(1)
            public TestEnum1 e1;
            @Bit(3)
            @Order(2)
            public TestEnum1 e2;
            @Bit(1)
            @Order(3)
            @ListLength(2)
            public List<Boolean> flags1;
            @Bit(3)
            @Order(4)
            public TestEnum1 e3;
            @Bit(3)
            @Order(5)
            public TestEnum1 e4;
            @Bit
            @Length(1)
            @Order(6)
            @ListLength(2)
            public List<Boolean> flags2;
            @Bit(3)
            @Order(7)
            public TestEnum1 e5;
            @Bit(3)
            @Order(8)
            public TestEnum1 e6;
            @Bit(1)
            @Order(9)
            @ListLength(2)
            public List<Boolean> flags3;
            @Bit(3)
            @Order(10)
            public TestEnum1 e7;
            @Bit(3)
            @Order(11)
            public TestEnum1 e8;
            @Bit(1)
            @Order(12)
            @ListLength(2)
            public List<Boolean> flags4;
        }
        Entity entity = new Entity();
        entity.deserialize(TestUtils.newInputStream(new byte[] {
                (byte) 0b000_001_11,(byte) 0b010_011_01,(byte) 0b100_101_01,(byte)0b110_111_10}));
        Assert.assertEquals(entity.e1, TestEnum1.A);
        Assert.assertEquals(entity.e2, TestEnum1.B);
        Assert.assertEquals(entity.flags1, Arrays.asList(true,true));
        Assert.assertEquals(entity.e3, TestEnum1.C);
        Assert.assertEquals(entity.e4, TestEnum1.D);
        Assert.assertEquals(entity.flags2, Arrays.asList(false,true));
        Assert.assertEquals(entity.e5, TestEnum1.E);
        Assert.assertEquals(entity.e6, TestEnum1.F);
        Assert.assertEquals(entity.flags3, Arrays.asList(false,true));
        Assert.assertEquals(entity.e7, TestEnum1.G);
        Assert.assertEquals(entity.e8, TestEnum1.H);
        Assert.assertEquals(entity.flags4, Arrays.asList(true,false));
        TestUtils.serializeMultipleTimesAndRestore(entity, 10, new Provider<Entity>() {

            @Override
            public Entity newInstance() {
                return new Entity();
            }
        });
    }
    
    @Test
    public void testBit6() throws Exception {
        @SuppressWarnings("hiding")
        @BigEndian
        class Entity extends DataPacket{
            @Bit(3)
            @Order(1)
            public int e1;
        }
        try {
            Entity entity = new Entity();
            entity.deserialize(TestUtils.newInputStream(new byte[] {
                    (byte) 0b000_001_11,(byte) 0b010_011_01,(byte) 0b100_101_01,(byte)0b110_111_10}));
            Assert.fail();
        } catch (Throwable e) {
            TestUtils.assertExactExceptionInHierarchy(e, FieldInfo.class, 1);
        }
    }
    
    @Test
    public void testBit7() throws Exception {
        @SuppressWarnings("hiding")
        @BigEndian
        class Entity extends DataPacket{
            @Bit(1)
            @Order(1)
            public TestEnum1 e1;
            @Bit(1)
            @Order(2)
            @ListLength(7)
            public List<Boolean> flags;
        }
        try {
            Entity entity = new Entity();
            entity.deserialize(TestUtils.newInputStream(new byte[] {4}));
            Assert.fail();
        } catch (Throwable e) {
            TestUtils.assertExactExceptionInHierarchy(e, EnumFieldInfo.class, 8);
        }
    }
    
    public static enum TestEnum2{
        A{
            @Override
            public String toString() {
                return "-1";
            }
        },B{
            @Override
            public String toString() {
                return "-2";
            }
        }
    }
    
    @Test
    public void testBit8() throws Exception {
        @SuppressWarnings("hiding")
        @BigEndian
        class Entity extends DataPacket{
            @Bit(1)
            @Order(1)
            public TestEnum2 e1;
            @Bit(1)
            @Order(2)
            @ListLength(7)
            public List<Boolean> flags;
        }
        try {
            Entity entity = new Entity();
            entity.deserialize(TestUtils.newInputStream(new byte[] {4}));
            Assert.fail();
        } catch (Throwable e) {
            TestUtils.assertExactExceptionInHierarchy(e, EnumFieldInfo.class, 8);
        }
    }
    
    @Test
    public void testBit9() throws Exception {
        @SuppressWarnings("hiding")
        @BigEndian
        class Entity extends DataPacket{
            @Bit(8)
            @Order(1)
            public TestEnum2 e1;
        }
        try {
            Entity entity = new Entity();
            entity.deserialize(TestUtils.newInputStream(new byte[] {4}));
            Assert.fail();
        } catch (Throwable e) {
            TestUtils.assertExactExceptionInHierarchy(e, FieldInfo.class, 12);
        }
    }
    
    @Test
    public void testBit10() throws Exception {
        @SuppressWarnings("hiding")
        @BigEndian
        class Entity extends DataPacket{
            @Bit(12)
            @Order(1)
            public TestEnum2 e1;
        }
        try {
            Entity entity = new Entity();
            entity.deserialize(TestUtils.newInputStream(new byte[] {4}));
            Assert.fail();
        } catch (Throwable e) {
            TestUtils.assertExactExceptionInHierarchy(e, FieldInfo.class, 13);
        }
        @BigEndian
        class Entity2 extends DataPacket{
            @Bit(-2)
            @Order(1)
            public TestEnum2 e1;
        }
        try {
            Entity2 entity = new Entity2();
            entity.deserialize(TestUtils.newInputStream(new byte[] {4}));
            Assert.fail();
        } catch (Throwable e) {
            TestUtils.assertExactExceptionInHierarchy(e, FieldInfo.class, 13);
        }
    }
    
    @Test
    public void testBit11() throws Exception {
        class Condition1 extends ModifierHandler<Boolean>{
            @Override
            public Boolean handleDeserialize0(String fieldName, Object entity, InputStream in) throws IOException {
                return null;
            }
            @Override
            public Boolean handleSerialize0(String fieldName, Object entity) {
                return null;
            }
        }
        @SuppressWarnings("hiding")
        @BigEndian
        class Entity extends DataPacket{
            @Bit
            @Order(1)
            @Conditional(Condition1.class)
            public TestEnum2 e1;
        }
        try {
            Entity entity = new Entity();
            entity.deserialize(TestUtils.newInputStream(new byte[] {4}));
            Assert.fail();
        } catch (Throwable e) {
            TestUtils.assertExactExceptionInHierarchy(e, FieldInfo.class, 14);
        }
    }
    
    @Test
    public void testBit12() throws Exception {
        @SuppressWarnings("hiding")
        @BigEndian
        class Entity extends DataPacket{
            @Bit
            @Length(8)
            @Order(1)
            public List<Boolean> flags;
        }
        try {
            Entity entity = new Entity();
            entity.deserialize(TestUtils.newInputStream(new byte[] {4}));
            Assert.fail();
        } catch (Throwable e) {
            TestUtils.assertExactExceptionInHierarchy(e, ClassInfo.class, 5);
        }
    }
    
    @Test
    public void testBit13() throws Exception {
        @SuppressWarnings("hiding")
        @BigEndian
        class Entity extends DataPacket{
            @Bit(2)
            @Order(0)
            public Byte flag;
            @Bit(7)
            @Order(1)
            public Byte flag2;
        }
        try {
            Entity entity = new Entity();
            entity.deserialize(TestUtils.newInputStream(new byte[] {4}));
            Assert.fail();
        } catch (Throwable e) {
            TestUtils.assertExactExceptionInHierarchy(e, ClassInfo.class, 12);
        }
    }
    
    @Test
    public void testBit14() throws Exception {
        @SuppressWarnings("hiding")
        @BigEndian
        class Entity extends DataPacket{
            @Bit(2)
            @Order(0)
            public Byte flag;
        }
        try {
            Entity entity = new Entity();
            entity.deserialize(TestUtils.newInputStream(new byte[] {4}));
            Assert.fail();
        } catch (Throwable e) {
            TestUtils.assertExactExceptionInHierarchy(e, ClassInfo.class, 13);
        }
    }
    
    @Test
    public void testBit15() throws Exception {
        @SuppressWarnings("hiding")
        @BigEndian
        class Entity extends DataPacket{
            @Bit(2)
            @Order(0)
            public Boolean flag;
        }
        try {
            Entity entity = new Entity();
            entity.deserialize(TestUtils.newInputStream(new byte[] {4}));
            Assert.fail();
        } catch (Throwable e) {
            TestUtils.assertExactExceptionInHierarchy(e, FieldInfo.class, 14);
        }
    }
    
    public static class Entity1 extends DataPacket{
        @Order(0)
        @SHORT
        public int int1;
        @Order(1)
        @Bit(3)
        public byte b1;
        @Order(2)
        @Bit(5)
        public byte b2;
        @Order(3)
        @Variant(SubHandler.class)
        public DataPacket sub;
        
        public static class SubHandler extends EntityHandler{
            @Override
            public DataPacket handle0(String fieldName, Object entity, InputStream in) throws IOException {
                byte mark = (byte) in.read();
                @SuppressWarnings("unused")
                byte next = (byte) in.read();
                return mark == 1 ? new Sub1() : new Sub2();
            }
        }
        
        public static class Sub1 extends DataPacket{
            @Order(0)
            @BYTE
            public byte mark;
            @Order(1)
            @Bit(2)
            public byte b1;
            @Order(2)
            @Bit(6)
            public byte b2;
        }
        public static class Sub2 extends DataPacket{
            @Order(0)
            @Bit(3)
            public byte mark;
            @Order(1)
            @Bit(5)
            public byte b1;
        }
    }
    
    @Test
    public void testBit16() throws Exception {
        Entity1 entity = new Entity1();
        entity.deserialize(TestUtils.newInputStream(new byte[] {
                0x0,0x1,0b00011111,
                0x1, //mark
                (byte) 0b10101010
        }));
        Assert.assertEquals(entity.b1, 0);
        Assert.assertEquals(entity.b2, 0b00011111);
        Assert.assertTrue(entity.sub instanceof Sub1);
        Assert.assertEquals(((Entity1.Sub1)entity.sub).mark,1);
        Assert.assertEquals(((Entity1.Sub1)entity.sub).b1,0b010);
        Assert.assertEquals(((Entity1.Sub1)entity.sub).b2,0b0101010);
    }
}
