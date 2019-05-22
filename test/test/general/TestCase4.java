package test.general;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.github.zhtmf.ConversionException;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.BigEndian;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Signed;
import io.github.zhtmf.annotations.types.BCD;
import io.github.zhtmf.annotations.types.CHAR;
import io.github.zhtmf.annotations.types.RAW;
import test.TestUtils;

public class TestCase4{
    
    private Entity2 entity = new Entity2();
    
    @Signed
    @BigEndian
    public static final class Entity2 extends DataPacket{
        @Order(-1)
        @BCD(5)
        public long n1;
        
        @Order(1)
        @CHAR(3)
        public byte b;
        
        @Order(2)
        @CHAR(5)
        public short s;
        
        @Order(3)
        @CHAR(10)
        public int i;
        
        @Order(4)
        @CHAR(19)
        public long l;
        
        @Order(5)
        @CHAR
        @Length
        public String str;
        
        @Order(6)
        @RAW
        @Length
        public int[] ints;
    }
    
    @Before
    public void setValues() {
        entity.n1 = 1234567070L;
        entity.b = 125;
        entity.s = Short.MAX_VALUE;
        entity.i = Integer.MAX_VALUE;
        entity.l = Long.MAX_VALUE;
        entity.str = "abc";
        entity.ints = new int[] {120,55,-32,-1};
    }
    
    
    @Test
    public void testLength() throws ConversionException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        entity.serialize(baos);
        byte[] arr = baos.toByteArray();
        Assert.assertEquals(entity.length(), arr.length);
        Entity2 e2 = new Entity2();
        e2.deserialize(new ByteArrayInputStream(arr));
        Assert.assertTrue(TestUtils.equalsOrderFields(entity, e2));
    }
    
    public static void main(String[] args) throws ConversionException {
        TestCase4 tc3 = new TestCase4();
        tc3.setValues();
        tc3.testLength();
    }
}
