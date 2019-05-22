package test.general;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.github.zhtmf.ConversionException;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.BigEndian;
import io.github.zhtmf.annotations.modifiers.CHARSET;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.ListLength;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Signed;
import io.github.zhtmf.annotations.types.BCD;
import io.github.zhtmf.annotations.types.CHAR;
import io.github.zhtmf.annotations.types.RAW;
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
