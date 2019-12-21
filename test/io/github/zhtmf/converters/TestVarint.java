package io.github.zhtmf.converters;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class TestVarint {

    @Test
    public void big1() throws Exception{
        {
            ByteArrayOutputStream dest = TestUtils.newByteArrayOutputStream();
            StreamUtils.writeUnsignedVarint(dest, 624485, true);
            byte[] array = dest.toByteArray();
            TestUtils.compareByteArray(array, slowButMoreCorrect(624485, true));
            
            Assert.assertEquals(StreamUtils.readVarint(
                    MarkableInputStream.wrap(TestUtils.newInputStream(dest.toByteArray())), true).longValue()
                    ,624485);
        }
        {
            ByteArrayOutputStream dest = TestUtils.newByteArrayOutputStream();
            StreamUtils.writeUnsignedVarint(dest, 12, true);
            byte[] array = dest.toByteArray();
            TestUtils.compareByteArray(array, slowButMoreCorrect(12, true));
            
            Assert.assertEquals(StreamUtils.readVarint(
                    MarkableInputStream.wrap(TestUtils.newInputStream(dest.toByteArray())), true).longValue()
                    ,12);
        }
    }
    
    @Test
    public void big2() throws Exception{
        {
            ByteArrayOutputStream total = new ByteArrayOutputStream();
            List<Integer> numbers = new ArrayList<>();
            
            for(int k=10000;k<100000;++k) {
                numbers.add(k);
                ByteArrayOutputStream dest = TestUtils.newByteArrayOutputStream();
                StreamUtils.writeUnsignedVarint(dest, k, true);
                byte[] array = dest.toByteArray();
                total.write(array);
                try {
                    TestUtils.compareByteArray(array, slowButMoreCorrect(k, true));
                } catch (Throwable e) {
                    Assert.fail("k at "+k+", "+e.getMessage());
                }
            }
            
            MarkableInputStream min = MarkableInputStream.wrap(TestUtils.newInputStream(total.toByteArray()));
            for(int i=0;i<numbers.size();++i) {
                int read = StreamUtils.readVarint(min, true).intValue();
                Assert.assertEquals("at "+i+"", read, numbers.get(i).intValue());
            }
        }
        
        {
            ByteArrayOutputStream total = new ByteArrayOutputStream();
            List<BigInteger> numbers = new ArrayList<>();
            
            for(BigInteger k=BigInteger.valueOf(Long.MAX_VALUE-1000);
                    k.compareTo(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.valueOf(100000)))<=0;
                    k = k.add(BigInteger.ONE)) {
                numbers.add(k);
                ByteArrayOutputStream dest = TestUtils.newByteArrayOutputStream();
                StreamUtils.writeUnsignedVarint(dest, k, true);
                byte[] array = dest.toByteArray();
                total.write(array);
                try {
                    TestUtils.compareByteArray(array, slowButMoreCorrect(k, true));
                } catch (Throwable e) {
                    Assert.fail("k at "+k+", "+e.getMessage());
                }
            }
            
            MarkableInputStream min = MarkableInputStream.wrap(TestUtils.newInputStream(total.toByteArray()));
            for(int i=0;i<numbers.size();++i) {
                BigInteger read = StreamUtils.readVarint(min, true);
                Assert.assertEquals("at "+i+"", read, numbers.get(i));
            }
        }
    }
    
    @Test
    public void little1() throws Exception{
        {
            ByteArrayOutputStream dest = TestUtils.newByteArrayOutputStream();
            StreamUtils.writeUnsignedVarint(dest, 624485, false);
            byte[] array = dest.toByteArray();
            TestUtils.compareByteArrayReversed(array, slowButMoreCorrect(624485, false));
            TestUtils.compareByteArray(array, new byte[] {(byte) 0xE5,(byte) 0x8E,0x26});
            
            Assert.assertEquals(StreamUtils.readVarint(
                    MarkableInputStream.wrap(TestUtils.newInputStream(dest.toByteArray())), false).longValue()
                    ,624485);
        }
        {
            ByteArrayOutputStream dest = TestUtils.newByteArrayOutputStream();
            StreamUtils.writeUnsignedVarint(dest, 12, false);
            byte[] array = dest.toByteArray();
            TestUtils.compareByteArrayReversed(array, slowButMoreCorrect(12, false));
            
            Assert.assertEquals(StreamUtils.readVarint(
                    MarkableInputStream.wrap(TestUtils.newInputStream(dest.toByteArray())), false).longValue()
                    ,12);
        }
    }
    
    @Test
    public void little2() throws Exception{
        {
            ByteArrayOutputStream total = new ByteArrayOutputStream();
            List<Integer> numbers = new ArrayList<>();
            
            for(int k=70000;k<170000;++k) {
                numbers.add(k);
                ByteArrayOutputStream dest = TestUtils.newByteArrayOutputStream();
                StreamUtils.writeUnsignedVarint(dest, k, false);
                byte[] array = dest.toByteArray();
                total.write(array);
                try {
                    TestUtils.compareByteArrayReversed(array, slowButMoreCorrect(k, false));
                } catch (Throwable e) {
                    Assert.fail("k at "+k+", "+e.getMessage());
                }
            }
            
            MarkableInputStream min = MarkableInputStream.wrap(TestUtils.newInputStream(total.toByteArray()));
            for(int i=0;i<numbers.size();++i) {
                int read = StreamUtils.readVarint(min, false).intValue();
                Assert.assertEquals("at "+i+"", read, numbers.get(i).intValue());
            }
        }
        
        {
            ByteArrayOutputStream total = new ByteArrayOutputStream();
            List<BigInteger> numbers = new ArrayList<>();
            
            for(BigInteger k=BigInteger.valueOf(Long.MAX_VALUE-10000);
                    k.compareTo(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.valueOf(170000)))<=0;
                    k = k.add(BigInteger.ONE)) {
                numbers.add(k);
                ByteArrayOutputStream dest = TestUtils.newByteArrayOutputStream();
                StreamUtils.writeUnsignedVarint(dest, k, false);
                byte[] array = dest.toByteArray();
                total.write(array);
                try {
                    TestUtils.compareByteArrayReversed(array, slowButMoreCorrect(k, false));
                } catch (Throwable e) {
                    Assert.fail("k at "+k+", "+e.getMessage());
                }
            }
            
            MarkableInputStream min = MarkableInputStream.wrap(TestUtils.newInputStream(total.toByteArray()));
            for(int i=0;i<numbers.size();++i) {
                BigInteger read = StreamUtils.readVarint(min, false);
                Assert.assertEquals("at "+i+"", read, numbers.get(i));
            }
        }
    }
    
    private byte[] slowButMoreCorrect(BigInteger num, boolean bigEndian) {
        String binary = num.toString(2);
        while(binary.length()%7!=0) {
            binary = "0" + binary;
        }
        byte[] ret = new byte[binary.length()/7];
        int ptr = ret.length-1;
        int left = binary.length()-7;
        int special = bigEndian ? binary.length() : 7;
        while(left>=0) {
            if(left+7 == special) {
                ret[ptr--] = (byte) Short.parseShort("0"+binary.substring(left,left+7),2);
            }else {
                ret[ptr--] = (byte) Short.parseShort("1"+binary.substring(left,left+7),2);
            }
            left -=7;
        }
        return ret;
    }
    
    private byte[] slowButMoreCorrect(long num, boolean bigEndian) {
        return slowButMoreCorrect(BigInteger.valueOf(num), bigEndian);
    }
}
