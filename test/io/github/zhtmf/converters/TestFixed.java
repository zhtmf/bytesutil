package io.github.zhtmf.converters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.Test;

public class TestFixed {
    
    private ScriptEngine nashorn = new ScriptEngineManager().getEngineByName("nashorn");
    
    @Test
    public void testSpecialValue() throws Exception{
        compare(Double.MIN_EXPONENT);
        compare(Double.MAX_VALUE);
        compare(Double.MIN_NORMAL);
        compare(Double.MAX_EXPONENT);
    }
    
    @Test
    public void testMinusAndBoundary() throws Exception{
        compare(-1.3);
        compare(256.12334);
        compare(-1.125);
        compare(-1.1255);
        compare(-13456345.1255);
        compare(-13456.12334);
        compare(-128.125556);
        compare(-255.12334);
        compare(127.125556);
        compare(128.12334);
        compare(255.125556);
        compare(32767.125556);
        compare(-32767.12334);
        compare(-32768.125556);
        compare(65535.12334);
        compare(-65535.12334);
    }
    
    @Test
    public void testFractional() throws Exception{
        /*
         * JavaScript outputs "0" for (-0.0).toString(2)
         * So does nashorn engine
         */
        assertEquals("-0", convertToString(StreamUtils.doubleToFixedFloatBytes(-0.0d, limit1, limit2)));
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
    
    private final int limit1 = 200;
    private final int limit2 = 200;
    
    private void compare(double d) throws ScriptException {
        String nas = nashorn.eval("("+d+").toString(2);")+"";
        byte[] array = StreamUtils.doubleToFixedFloatBytes(d, limit1, limit2);
        
        assertEquals(nas, convertToString(array));
        
        double restored = StreamUtils.fixedFloatBytesToDouble(array, limit1, limit2, false);
        assertTrue(restored+" "+d, Double.compare(restored, d) == 0);
    }
    
    private String convertToString(byte[] array) {
        StringBuilder sb = new StringBuilder();
        
        boolean negative = array[0] == (byte)0x80;
        boolean zero = false;
        int i = 0;
        if(array[0] == (byte)0x80) {
            negative = true;
            i = 1;
        }
        
        boolean nonZeroMet = false;
        for(;i<limit1;++i) {
            byte b = array[i];
            if(!nonZeroMet && b == 0)
                continue;
            nonZeroMet = true;
            int num = b >= 0 ? (int)b : ((int)(b & 0x7F) | 0x80);
            sb.append(toBinaryString(num));
        }
        
        zero = sb.length() == 0;
        
        int m = array.length - 1;
        for(; m >= 0 && array[m] == 0; --m);
        if(m >= limit1){
            sb.append('.');
            for(int k=limit1;k<=m;++k) {
                byte b = array[k];
                int num = b >= 0 ? (int)b : ((int)(b & 0x7F) | 0x80);
                sb.append(toBinaryString(num));
            } 
            int until = sb.length() - 1;
            while(sb.charAt(until)=='0') -- until;
            sb.delete(until+1, sb.length());
        }
        
        if(sb.length() > 0) {
            m = 0;
            for(; m < limit1 && sb.charAt(m) == '0'; ++m);
            sb.delete(0, m);
        }
        
        if(zero) {
            sb.insert(0, '0');
        }
        if(negative) {
            sb.insert(0, '-');
        }
        
        return sb.toString();
    }
    
    private String toBinaryString(int b) {
        String str = Integer.toBinaryString(b);
        while(str.length() < 8)
            str = "0" + str;
        if(str.length() > 8)
            str = str.substring(str.length()-8, str.length());
        return str;
    }
    
    public static void main(String[] args) {
    }
}
