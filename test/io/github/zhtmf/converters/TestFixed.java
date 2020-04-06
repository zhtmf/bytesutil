package io.github.zhtmf.converters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.junit.Test;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.BigEndian;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.LittleEndian;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Signed;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.Fixed;
import io.github.zhtmf.annotations.types.SHORT;

public class TestFixed {
    
    private ScriptEngine nashorn = new ScriptEngineManager().getEngineByName("nashorn");
    
    @Test
    public void testExceptions() throws Exception{
        try {
            class Entity1 extends DataPacket{
                @Order(0)
                @Fixed({1,13})
                @Signed
                public Date d1;
            }
            new Entity1().serialize(TestUtils.newByteArrayOutputStream());
            fail();
        } catch (Exception e) {
            TestUtils.assertExactExceptionInHierarchy(e, FieldInfo.class, 1);
        }
        try {
            class Entity1 extends DataPacket{
                @Order(0)
                @Fixed({-1,13})
                @Signed
                public double d1;
            }
            new Entity1().serialize(TestUtils.newByteArrayOutputStream());
            fail();
        } catch (Exception e) {
            TestUtils.assertExactExceptionInHierarchy(e, FieldInfo.class, 29);
        }
        try {
            class Entity1 extends DataPacket{
                @Order(0)
                @Fixed({13})
                @Signed
                public double d1;
            }
            new Entity1().serialize(TestUtils.newByteArrayOutputStream());
            fail();
        } catch (Exception e) {
            TestUtils.assertExactExceptionInHierarchy(e, FieldInfo.class, 27);
        }
        try {
            class Entity1 extends DataPacket{
                @Order(0)
                @Fixed({7,8})
                @Signed
                public double d1;
            }
            new Entity1().serialize(TestUtils.newByteArrayOutputStream());
            fail();
        } catch (Exception e) {
            TestUtils.assertExactExceptionInHierarchy(e, FieldInfo.class, 28);
        }
        try {
            class Entity1 extends DataPacket{
                @Order(0)
                @Fixed({8,7})
                @Signed
                public double d1;
            }
            new Entity1().serialize(TestUtils.newByteArrayOutputStream());
            fail();
        } catch (Exception e) {
            TestUtils.assertExactExceptionInHierarchy(e, FieldInfo.class, 28);
        }
        try {
            class Entity1 extends DataPacket{
                @Order(0)
                @Fixed({8,16})
                @Signed
                public double d1 = Double.POSITIVE_INFINITY;
            }
            new Entity1().serialize(TestUtils.newByteArrayOutputStream());
            fail();
        } catch (Exception e) {
            TestUtils.assertExactExceptionInHierarchy(e, StreamUtils.class, 22);
        }
        try {
            class Entity1 extends DataPacket{
                @Order(0)
                @Fixed({8,16})
                @Signed
                public double d1 = Double.NEGATIVE_INFINITY;
            }
            new Entity1().serialize(TestUtils.newByteArrayOutputStream());
            fail();
        } catch (Exception e) {
            TestUtils.assertExactExceptionInHierarchy(e, StreamUtils.class, 22);
        }
        try {
            class Entity1 extends DataPacket{
                @Order(0)
                @Fixed({8,16})
                @Signed
                public double d1 = Double.NaN;
            }
            new Entity1().serialize(TestUtils.newByteArrayOutputStream());
            fail();
        } catch (Exception e) {
            TestUtils.assertExactExceptionInHierarchy(e, StreamUtils.class, 21);
        }
        try {
            class Entity1 extends DataPacket{
                @Order(0)
                @Fixed({16,16})
                @Signed
                public double d1 = -32768.345d;
            }
            new Entity1().serialize(TestUtils.newByteArrayOutputStream());
            fail();
        } catch (Exception e) {
            TestUtils.assertExactExceptionInHierarchy(e, StreamUtils.class, 26);
        }
        
        try {
            class Entity1 extends DataPacket{
                @Order(0)
                @Fixed({16,16})
                @Signed
                public double d1 = 32768.345d;
            }
            new Entity1().serialize(TestUtils.newByteArrayOutputStream());
            fail();
        } catch (Exception e) {
            TestUtils.assertExactExceptionInHierarchy(e, StreamUtils.class, 26);
        }
        
        try {
            class Entity1 extends DataPacket{
                @Order(0)
                @Fixed({1600,24})
                @Signed
                public BigDecimal d1 = new BigDecimal(Double.MAX_VALUE).add(BigDecimal.ONE);
            }
            class Entity2 extends DataPacket{
                @Order(0)
                @Fixed({1600,24})
                @Signed
                public double d1;
            }
            new Entity2().deserialize(TestUtils.newInputStream(
                    TestUtils.serializeAndGetBytes(new Entity1())));
            fail();
        } catch (Exception e) {
            TestUtils.assertExactExceptionInHierarchy(e, StreamUtils.class, 24);
        }
        
        try {
            class Entity1 extends DataPacket{
                @Order(0)
                @Fixed({1600,24})
                @Signed
                public BigDecimal d1 = new BigDecimal(-Double.MAX_VALUE).subtract(BigDecimal.ONE);
            }
            class Entity2 extends DataPacket{
                @Order(0)
                @Fixed({1600,24})
                @Signed
                public double d1;
            }
            new Entity2().deserialize(TestUtils.newInputStream(
                    TestUtils.serializeAndGetBytes(new Entity1())));
            fail();
        } catch (Exception e) {
            TestUtils.assertExactExceptionInHierarchy(e, StreamUtils.class, 25);
        }
    }
    
    public static class FixedEntity extends DataPacket{
        
        @Signed
        @LittleEndian
        @Fixed({64,64})
        @Order(-2)
        @Length(4)
        public List<Double> dList1 = 
            Arrays.asList(255.125,255.12334,128.12334,-477.129);
        
        @Signed
        @LittleEndian
        @Fixed({16,16})
        @Order(-1)
        public double d7 = -32768;
        
        @Signed
        @BigEndian
        @Fixed({32,56})
        @Order(0)
        public double d1 = -477.129;
        
        @Unsigned
        @BigEndian
        @Fixed({32,56})
        @Order(1)
        public double d2 = 1477.876;
        
        @Signed
        @LittleEndian
        @Fixed({32,56})
        @Order(2)
        public double d3 = -477.129;
        
        @Unsigned
        @LittleEndian
        @Fixed({32,56})
        @Order(3)
        public double d4 = 1477.876;
        
        @Signed
        @BigEndian
        @Fixed({32,56})
        @Order(4)
        public BigDecimal d11 = new BigDecimal("-14777.1295");
        
        @Unsigned
        @BigEndian
        @Fixed({32,56})
        @Order(5)
        public BigDecimal d12 = new BigDecimal("24777.295");
        
        @Signed
        @LittleEndian
        @Fixed({32,56})
        @Order(6)
        public BigDecimal d13 = new BigDecimal("-14777.1295");
        
        @Unsigned
        @LittleEndian
        @Fixed({32,56})
        @Order(7)
        public BigDecimal d14 = new BigDecimal("24777.295");
        
        @Unsigned
        @LittleEndian
        @Fixed({8,8})
        @Order(8)
        public double d5 = 255.125;
        
        @Unsigned
        @LittleEndian
        @Fixed({16,16})
        @Order(9)
        public double d6 = 65535.125d;
        
        @Signed
        @LittleEndian
        @Fixed({16,16})
        @Order(10)
        public double d8 = 32767.125;
        
        @Unsigned
        @LittleEndian
        @Fixed({16,0})
        @Order(11)
        public double d9 = 65534;
        
        @Signed
        @LittleEndian
        @Fixed({16,0})
        @Order(12)
        public double d100 = Short.MAX_VALUE;
        
        @Signed
        @LittleEndian
        @Fixed({16,0})
        @Order(13)
        public double d111 = Short.MIN_VALUE;
    }
    
    @Test
    public void testEntity() throws Exception{
        TestUtils.serializeMultipleTimesAndRestore(new FixedEntity(), 5);
    }
    
    @Test
    public void testNumbersGreaterThanZero() throws Exception{
        compare(34.0);
        compare(255.12334);
        compare(477.129);
        compare(1.3);
        compare(256.12334);
        compare(1.1255);
        compare(13456345.1255);
        compare(13456.12334);
        compare(128.125556);
        compare(127.125556);
        compare(128.12334);
        compare(255.125556);
        compare(32767.125556);
        compare(32767.12334);
        compare(32768.125556);
        compare(65535.12334);
        compare(-477.129, 32, 56);
    }
    
    @Test
    public void testFractional() throws Exception{
        compare(Math.pow(2, -25));
        compare(Math.pow(2, -1));
        compare(Math.pow(2, -2));
        compare(Math.pow(2, -3));
        compare(Math.pow(2, -4));
        compare(Math.pow(2, -5));
        compare(Math.pow(2, -6));
        compare(Math.pow(2, -7));
        compare(Math.pow(2, -8));
        compare(Math.pow(2, -9));
        compare(Math.pow(2, -10));
        compare(Math.pow(2, -11));
        compare(Math.pow(2, -12));
        compare(Math.pow(2, -13));
        compare(Math.pow(2, -14));
        compare(Math.pow(2, -15));
        compare(Math.pow(2, -16));
        compare(Math.pow(2, -17));
        compare(Math.pow(2, -18));
        compare(Math.pow(2, -19));
        compare(Math.pow(2, -20));
        compare(Math.pow(2, -21));
        compare(Math.pow(2, -22));
        compare(Math.pow(2, -23));
        compare(Math.pow(2, -24));
        compare(Math.pow(2, -26));
        compare(Math.pow(2, -27));
        compare(Math.pow(2, -28));
        compare(Math.pow(2, -29));
        compare(Math.pow(2, -30));
        compare(Math.pow(2, -31));
        compare(Math.pow(2, -32));
        compare(Math.pow(2, -33));
        compare(Math.pow(2, -34));
        compare(Math.pow(2, -35));
        compare(Math.pow(2, -36));
        compare(Math.pow(2, -37));
        compare(Math.pow(2, -38));
        compare(Math.pow(2, -39));
        compare(Math.pow(2, -40));
        compare(Math.pow(2, -41));
        compare(Math.pow(2, -42));
        compare(Math.pow(2, -43));
        compare(Math.pow(2, -44));
        compare(Math.pow(2, -45));
        compare(Math.pow(2, -46));
        compare(Math.pow(2, -47));
        compare(Math.pow(2, -48));
        compare(Math.pow(2, -49));
        compare(Math.pow(2, -50));
        compare(Math.pow(2, -51));
        compare(Math.pow(2, -52));
        compare(Math.pow(2, -53));
        compare(Math.pow(2, -54));
        compare(Math.pow(2, -55));
        compare(Math.pow(2, -56));
        compare(Math.pow(2, -57));
        compare(Math.pow(2, -58));
        compare(Math.pow(2, -59));
        compare(Math.pow(2, -60));
        compare(Math.pow(2, -61));
        compare(Math.pow(2, -62));
        compare(Math.pow(2, -63));
        compare(0.68791256000011451419198101145147524);
        compare(0.6879125600001145141919810114514752);
        compare(0.687912560000114514191981011451475);
        compare(0.68791256000011451419198101145147);
        compare(0.68791256000011451419198101145141);
        compare(0.6879125600001145141919810114514);
        compare(0.687912560000114514191981011451);
        compare(0.68791256000011451419198101145);
        compare(0.6879125600001145141919810114);
        compare(0.687912560000114514191981011);
        compare(0.68791256000011451419198101);
        compare(0.687912560000114514191981);
        compare(0.68791256000011451419198);
        compare(0.6879125600001145141919);
        compare(0.687912560000114514191);
        compare(0.68791256000011451419);
        compare(0.6879125600001145141);
        compare(0.687912560000114514);
        compare(0.687912560000514);
        compare(0.68791256000051);
        compare(0.6879125600005);
        compare(0.687912560004);
        compare(0.68791256789);
        compare(0.6879125678);
        compare(0.687912567);
        compare(0.68791256);
        compare(0.6879125);
        compare(0.687912);
        compare(0.68791);
        compare(0.6879);
        compare(0.125);
        compare(0.12);
        compare(0.3);
        compare(0.5);
        compare(0.0);
        compare(0.1110);
        compare(0.1);
        compare(0.1990000);
    }
    
    private final int limit1 = 20;
    private final int limit2 = 25;
    
    public static class DummyEntity extends DataPacket{
        @Order(0)
        @SHORT
        public int f1;
    }
    
    private void compare(double d, int limit1, int limit2) throws Exception {

        String nas = nashorn.eval("("+d+").toString(2);")+"";
        
        ClassInfo ci = new ClassInfo(DummyEntity.class);
        if(d >= 0) {
            
            FieldInfo dummy = ci.fieldInfoList.get(0);
            {
                Field field = FieldInfo.class.getDeclaredField("fixedNumberLengths");
                field.setAccessible(true);
                field.set(dummy, new int[] {limit1, limit2});
            }
            {
                Field field = FieldInfo.class.getDeclaredField("unsigned");
                field.setAccessible(true);
                field.set(dummy, true);
            }
            {
                Field field = FieldInfo.class.getDeclaredField("signed");
                field.setAccessible(true);
                field.set(dummy, false);
            }
            
            byte[] array = StreamUtils.doubleToFixedPointBytes(d, dummy);
            double restored = StreamUtils.fixedPointBytesToDouble(array, dummy);
            assertTrue(restored+" "+d, Double.compare(restored, d) == 0);
            
            byte[] integer = Arrays.copyOf(array, limit1);
            byte[] fraction = Arrays.copyOfRange(array, limit1, array.length);
            assertEquals(nas, convertToString(integer, fraction));
            
            if(d != 0.0d) {
                /*
                 * JavaScript outputs "0" for (-0.0).toString(2)
                 * So does nashorn engine
                 */
                compare(-d);
            }
        }else {
            
            FieldInfo dummy = ci.fieldInfoList.get(0);
            {
                Field field = FieldInfo.class.getDeclaredField("fixedNumberLengths");
                field.setAccessible(true);
                field.set(dummy, new int[] {limit1, limit2});
            }
            {
                Field field = FieldInfo.class.getDeclaredField("unsigned");
                field.setAccessible(true);
                field.set(dummy, false);
            }
            {
                Field field = FieldInfo.class.getDeclaredField("signed");
                field.setAccessible(true);
                field.set(dummy, true);
            }
            
            byte[] array = StreamUtils.doubleToFixedPointBytes(d, dummy);
            double restored = StreamUtils.fixedPointBytesToDouble(array, dummy);
            assertTrue(restored+" "+d, Double.compare(restored, d) == 0);
            
            for(int k = 0; k < array.length; ++k) {
                array[k] = (byte) ~array[k];
            }
            
            int carryover = 1;
            for(int p = array.length - 1; p >= 0; --p) {
                if(array[p] == (byte)-1 && carryover == 1) {
                    array[p] = 0;
                }else {
                    array[p] += carryover;
                    carryover = 0;
                }
            }
            
            byte[] integer = Arrays.copyOf(array, limit1);
            byte[] fraction = Arrays.copyOfRange(array, limit1, array.length);
            assertEquals(nas, "-"+convertToString(integer, fraction));
        }
    }
    
    private void compare(double d) throws Exception {
        compare(d, limit1, limit2);
    }
    
    private String convertToString(byte[] integer, byte[] fraction) {
        StringBuilder sb = new StringBuilder();
        
        BigInteger integerPart = new BigInteger(integer);
        sb.append(integerPart.toString(2));
        
        int m = fraction.length - 1;
        for(; m >= 0 && fraction[m] == 0; --m);
        if(m >= 0){
            sb.append('.');
            for(int k=0;k<=m;++k) {
                byte b = fraction[k];
                int num = b >= 0 ? (int)b : ((int)(b & 0x7F) | 0x80);
                sb.append(toBinaryString(num));
            } 
            int until = sb.length() - 1;
            while(sb.charAt(until)=='0') -- until;
            sb.delete(until+1, sb.length());
        }
        
        return sb.toString();
    }
    
    private static String toBinaryString(int b) {
        String str = Integer.toBinaryString(b);
        while(str.length() < 8)
            str = "0" + str;
        if(str.length() > 8)
            str = str.substring(str.length()-8, str.length());
        return str; 
    }
}
