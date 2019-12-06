package io.github.zhtmf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import io.github.zhtmf.annotations.enums.NumericEnum;
import io.github.zhtmf.annotations.enums.StringEnum;
import io.github.zhtmf.annotations.modifiers.BigEndian;
import io.github.zhtmf.annotations.modifiers.CHARSET;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.BYTE;
import io.github.zhtmf.annotations.types.CHAR;
import io.github.zhtmf.annotations.types.INT;
import io.github.zhtmf.converters.TestUtils;

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
    
    public enum OrdinalEnum{
        A,B,C,D,E,F
    }
    
    @Test
    public void test2() throws ConversionException, IOException {
        class Test3 extends DataPacket{
            @BYTE
            @Order(0)
            public OrdinalEnum e1;
            @BYTE
            @Order(1)
            public OrdinalEnum e2;
            @BYTE
            @Order(2)
            public OrdinalEnum e3;
            @BYTE
            @Order(3)
            public OrdinalEnum e4;
            @BYTE
            @Order(4)
            public OrdinalEnum e5;
            @BYTE
            @Order(5)
            public OrdinalEnum e6;
        }
        Test3 test3 = new Test3();
        test3.deserialize(TestUtils.newInputStream(new byte[] {5,1,2,3,4,0}));
        Assert.assertEquals(test3.e1, OrdinalEnum.F);
        Assert.assertEquals(test3.e2, OrdinalEnum.B);
        Assert.assertEquals(test3.e3, OrdinalEnum.C);
        Assert.assertEquals(test3.e4, OrdinalEnum.D);
        Assert.assertEquals(test3.e5, OrdinalEnum.E);
        Assert.assertEquals(test3.e6, OrdinalEnum.A);
    }
    
    
    public static enum OrdinalEnum2{
        A{
            @Override
            public String toString() {
                return "1";
            }
        },B{
            @Override
            public String toString() {
                return "2";
            }
        },C
    }
    
    //one of its members fail to provide a reasonable toString
    @Test
    public void testBit6() throws Exception {
        @BigEndian
        class Test4 extends DataPacket{
            @BYTE
            @Order(1)
            public OrdinalEnum2 e1;
            @BYTE
            @Order(2)
            public OrdinalEnum2 e2;
            @BYTE
            @Order(3)
            public OrdinalEnum2 e3;
        }
        Test4 test4 = new Test4();
        test4.deserialize(TestUtils.newInputStream(new byte[] {2,1,0}));
        Assert.assertEquals(test4.e1, OrdinalEnum2.C);
        Assert.assertEquals(test4.e2, OrdinalEnum2.B);
        Assert.assertEquals(test4.e3, OrdinalEnum2.A);
    }
    
    public static enum OrdinalEnum3{
        A{
            @Override
            public String toString() {
                return "1";
            }
        },B{
            @Override
            public String toString() {
                return "2";
            }
        },C{
            @Override
            public String toString() {
                return "3";
            }
        }
    }
    
    //toString takes precedence
    @Test
    public void testBit7() throws Exception {
        @BigEndian
        class Test4 extends DataPacket{
            @BYTE
            @Order(1)
            public OrdinalEnum3 e1;
            @BYTE
            @Order(2)
            public OrdinalEnum3 e2;
            @BYTE
            @Order(3)
            public OrdinalEnum3 e3;
        }
        Test4 test4 = new Test4();
        test4.deserialize(TestUtils.newInputStream(new byte[] {3,2,1}));
        Assert.assertEquals(test4.e1, OrdinalEnum3.C);
        Assert.assertEquals(test4.e2, OrdinalEnum3.B);
        Assert.assertEquals(test4.e3, OrdinalEnum3.A);
    }
}
