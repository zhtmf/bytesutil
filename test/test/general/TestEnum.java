package test.general;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

import org.dzh.bytesutil.ConversionException;
import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.enums.NumericEnum;
import org.dzh.bytesutil.annotations.enums.StringEnum;
import org.dzh.bytesutil.annotations.modifiers.CHARSET;
import org.dzh.bytesutil.annotations.modifiers.Length;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Unsigned;
import org.dzh.bytesutil.annotations.types.BYTE;
import org.dzh.bytesutil.annotations.types.CHAR;
import org.dzh.bytesutil.annotations.types.INT;
import org.junit.Assert;
import org.junit.Test;

public class TestEnum {
    
    private enum NEnum1 implements NumericEnum{
        FLAG1 {
            @Override
            public long getValue() {
                return (long)Integer.MAX_VALUE*2+1;
            }
        },
        FLAG2 {
            @Override
            public long getValue() {
                return 300;
            }
        };
        @Override
        public abstract long getValue();
    }
    
    private enum NEnum2{
        FLAG1 {
            @Override
            public String toString() {
                return "127";
            }
        },
        FLAG2 {
            @Override
            public String toString() {
                return "255";
            }
        };
    }
    
    private enum SEnum1 implements StringEnum{
        FLAG1 {
            @Override
            public String getValue() {
                return "SUCCESS";
            }
        },
        FLAG2 {
            @Override
            public String getValue() {
                return "FAILURE";
            }
        };
    }
    
    private enum SEnum2{
        FLAG1 {
            @Override
            public String toString() {
                return "成功";
            }
        },
        FLAG2 {
            @Override
            public String toString() {
                return "失败";
            }
        };
    }
    
    @CHARSET("UTF-8")
    public static class Test1 extends DataPacket{
        @INT
        @Order(0)
        public NEnum1 nenum1;
        @BYTE
        @Unsigned
        @Order(1)
        public NEnum2 nenum2;
        @CHAR(7)
        @Order(2)
        public SEnum1 senum1;
        @CHAR
        @Length(6)
        @Order(3)
        public SEnum2 senum2;
    }
    
    @Test
    public void test() throws ConversionException, IOException {
        Test1 src = new Test1();
        Test1 dest = new Test1();
        {
            src.nenum1 = NEnum1.FLAG1;
            src.nenum2 = NEnum2.FLAG2;
            src.senum1 = SEnum1.FLAG1;
            src.senum2 = SEnum2.FLAG2;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            src.serialize(baos);
            dest.deserialize(new ByteArrayInputStream(baos.toByteArray()));
            Assert.assertEquals(src.nenum1, dest.nenum1);
            Assert.assertEquals(src.nenum2, dest.nenum2);
            Assert.assertEquals(src.senum1, dest.senum1);
            Assert.assertEquals(src.senum2, dest.senum2);
            
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(baos.toByteArray()));
            Assert.assertEquals(dis.readInt(), (int)NEnum1.FLAG1.getValue());
            Assert.assertEquals(dis.readByte(), (byte)Long.parseLong(NEnum2.FLAG2.toString()));
            byte[] s7 = new byte[7];
            dis.readFully(s7);
            Assert.assertEquals(new String(s7,"UTF-8"),SEnum1.FLAG1.getValue());
            byte[] s6 = new byte[6];
            dis.readFully(s6);
            Assert.assertEquals(new String(s6,"UTF-8"),SEnum2.FLAG2.toString());
        }
        {
            src.nenum1 = NEnum1.FLAG2;
            src.nenum2 = NEnum2.FLAG1;
            src.senum1 = SEnum1.FLAG2;
            src.senum2 = SEnum2.FLAG1;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            src.serialize(baos);
            dest.deserialize(new ByteArrayInputStream(baos.toByteArray()));
            Assert.assertEquals(src.nenum1, dest.nenum1);
            Assert.assertEquals(src.nenum2, dest.nenum2);
            Assert.assertEquals(src.senum1, dest.senum1);
            Assert.assertEquals(src.senum2, dest.senum2);
            
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(baos.toByteArray()));
            Assert.assertEquals(dis.readInt(), (int)NEnum1.FLAG2.getValue());
            Assert.assertEquals(dis.readByte(), (byte)Long.parseLong(NEnum2.FLAG1.toString()));
            byte[] s7 = new byte[7];
            dis.readFully(s7);
            Assert.assertEquals(new String(s7,"UTF-8"),SEnum1.FLAG2.getValue());
            byte[] s6 = new byte[6];
            dis.readFully(s6);
            Assert.assertEquals(new String(s6,"UTF-8"),SEnum2.FLAG1.toString());
        }
    }
    
    public static void main(String[] args) throws Exception {
        new TestEnum().test();
    }
}
