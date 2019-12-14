package io.github.zhtmf.converters;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;

import org.junit.Assert;
import org.junit.Test;

public class TestVarint {

    @Test
    public void big1() throws Exception{
        ByteArrayOutputStream dest = TestUtils.newByteArrayOutputStream();
        StreamUtils.writeVarint(dest, 624485, true);
        byte[] array = dest.toByteArray();
        TestUtils.compareByteArray(array, slowButMoreCorrect(624485, true));
    }
    
    @Test
    public void big2() throws Exception{
        for(int k=10000;k<100000;++k) {
            ByteArrayOutputStream dest = TestUtils.newByteArrayOutputStream();
            StreamUtils.writeVarint(dest, k, true);
            byte[] array = dest.toByteArray();
            try {
                TestUtils.compareByteArray(array, slowButMoreCorrect(k, true));
            } catch (Throwable e) {
                Assert.fail("k at "+k+", "+e.getMessage());
            }
        }
        for(BigInteger k=BigInteger.valueOf(Long.MAX_VALUE-1000);
                    k.compareTo(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.valueOf(100000)))<=0;
                    k = k.add(BigInteger.ONE)) {
            ByteArrayOutputStream dest = TestUtils.newByteArrayOutputStream();
            StreamUtils.writeVarint(dest, k, true);
            byte[] array = dest.toByteArray();
            try {
                TestUtils.compareByteArray(array, slowButMoreCorrect(k, true));
            } catch (Throwable e) {
                Assert.fail("k at "+k+", "+e.getMessage());
            }
        }
    }
    
    @Test
    public void little1() throws Exception{
        ByteArrayOutputStream dest = TestUtils.newByteArrayOutputStream();
        StreamUtils.writeVarint(dest, 624485, false);
        byte[] array = dest.toByteArray();
        TestUtils.compareByteArrayReversed(array, slowButMoreCorrect(624485, false));
        TestUtils.compareByteArray(array, new byte[] {(byte) 0xE5,(byte) 0x8E,0x26});
    }
    
    @Test
    public void little2() throws Exception{
        for(int k=70000;k<170000;++k) {
            ByteArrayOutputStream dest = TestUtils.newByteArrayOutputStream();
            StreamUtils.writeVarint(dest, k, false);
            byte[] array = dest.toByteArray();
            try {
                TestUtils.compareByteArrayReversed(array, slowButMoreCorrect(k, false));
            } catch (Throwable e) {
                Assert.fail("k at "+k+", "+e.getMessage());
            }
        }
        for(BigInteger k=BigInteger.valueOf(Long.MAX_VALUE-10000);
                    k.compareTo(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.valueOf(170000)))<=0;
                    k = k.add(BigInteger.ONE)) {
            ByteArrayOutputStream dest = TestUtils.newByteArrayOutputStream();
            StreamUtils.writeVarint(dest, k, false);
            byte[] array = dest.toByteArray();
            try {
                TestUtils.compareByteArrayReversed(array, slowButMoreCorrect(k, false));
            } catch (Throwable e) {
                Assert.fail("k at "+k+", "+e.getMessage());
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
