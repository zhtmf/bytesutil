package test.general;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

import org.dzh.bytesutil.ConversionException;
import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.BigEndian;
import org.dzh.bytesutil.annotations.modifiers.CHARSET;
import org.dzh.bytesutil.annotations.modifiers.Length;
import org.dzh.bytesutil.annotations.modifiers.ListLength;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Signed;
import org.dzh.bytesutil.annotations.types.BCD;
import org.dzh.bytesutil.annotations.types.CHAR;
import org.dzh.bytesutil.annotations.types.RAW;
import org.junit.Assert;
import org.junit.Test;

import test.TestUtils;

public class TestCase9{
    
    private Entity entity = new Entity();
    
    @Signed
    @BigEndian
    @CHARSET("UTF-8")
    public static final class Entity extends DataPacket{
        @Order(-1)
        @BCD(5)
        public long n1;
        
        @Order(1)
        @CHAR(3)
        public String char1;
        
        @Order(2)
        @CHAR(4)
        public String char2;
        
        @Order(6)
        @RAW
        @Length
        public int[] ints;
        
        @Order(7)
        @CHAR
        @Length
        @ListLength(1)
        public List<String> strs;
    }
    
    
    @Test
    public void testLength() throws ConversionException {
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            entity.n1 = 1234567070L;
            entity.char1 = "abc";
            entity.char2 = "tttt";
            entity.ints = new int[] {120,55,-32};
            entity.strs = Arrays.asList("abcsssssssssssss");
            entity.serialize(baos);
            entity.serialize(baos);
            entity.serialize(baos);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            {
                Entity e2 = new Entity();
                e2.deserialize(bais);
                Assert.assertTrue(TestUtils.equalsOrderFields(entity, e2));
            }
            {
                Entity e2 = new Entity();
                e2.deserialize(bais);
                Assert.assertTrue(TestUtils.equalsOrderFields(entity, e2));
            }
            {
                Entity e2 = new Entity();
                e2.deserialize(bais);
                Assert.assertTrue(TestUtils.equalsOrderFields(entity, e2));
            }
        }
    }
    
    public static void main(String[] args) throws ConversionException {
        TestCase9 tc3 = new TestCase9();
        tc3.testLength();
    }
}
