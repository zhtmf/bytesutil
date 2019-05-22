package test.general;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.github.zhtmf.ConversionException;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.BigEndian;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.ListLength;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Signed;
import io.github.zhtmf.annotations.types.BCD;
import io.github.zhtmf.annotations.types.BYTE;
import io.github.zhtmf.annotations.types.CHAR;
import io.github.zhtmf.annotations.types.INT;
import io.github.zhtmf.annotations.types.RAW;
import io.github.zhtmf.annotations.types.SHORT;
import io.github.zhtmf.converters.auxiliary.ModifierHandler;
import test.TestUtils;

public class TestCase3{
    
    private Entity2 entity = new Entity2();
    
    @Signed
    @BigEndian
    public static final class SubSubEntity extends DataPacket{
        @Order(0)
        @INT
        public int b1;
    }
    
    @Signed
    @BigEndian
    public static final class SubEntity extends DataPacket{
        
        @Order(-1)
        @BYTE
        public int b1;
        
        @Order(1)
        @BYTE
        @Length(2)
        public List<Integer> byteList;
        
        @Order(2)
        @SHORT
        @Length(2)
        public List<Integer> shortList;
        
        @Order(3)
        @INT
        @Length(2)
        public List<Integer> intList;
        
        @Order(4)
        @BCD(3)
        @Length(2)
        public List<Integer> bcdList;
        
        @Order(5)
        @CHAR(5)
        @ListLength(handler = Handler2.class)
        public List<String> strList;
        
        @Order(6)
        @CHAR
        @Length(handler = Handler2.class)
        @ListLength(handler = Handler3.class)
        public List<String> strList2;
        
        @Order(7)
        @Length(handler = Handler2.class)
        public List<SubSubEntity> entityList;
    }
    
    //1+1+2+4+2+5+1+3+1
    @Signed
    @BigEndian
    public static final class Entity2 extends DataPacket{
        @Order(-1)
        @BYTE
        public int b1;
        @Order(0)
        @BYTE
        public int totalLength;
        @Order(1)
        @SHORT
        public int b2;
        @Order(2)
        @INT
        public int b3;
        @Order(3)
        @BCD(2)
        public int b4;
        @Order(4)
        @CHAR(5)
        public String chars;
        @Order(5)
        @CHAR
        @Length(handler = Handler.class)
        public String chars2;
        @Order(6)
        @RAW(3)
        public byte[] raw1;
        @Order(7)
        @RAW
        @Length(handler = Handler.class)
        public byte[] raw2;
        @Order(8)
        public SubEntity entity;
    }
    
    public static final class Handler extends ModifierHandler<Integer>{

        @Override
        public Integer handleDeserialize0(String fieldName, Object entity, InputStream is) {
            Entity2 ent = (Entity2)entity;
            return ent.b1;
        }

        @Override
        public Integer handleSerialize0(String fieldName, Object entity) {
            Entity2 ent = (Entity2)entity;
            return ent.b1;
        }
        
    }
    
    public static final class Handler2 extends ModifierHandler<Integer>{

        @Override
        public Integer handleDeserialize0(String fieldName, Object entity, InputStream is) {
            SubEntity ent = (SubEntity)entity;
            return ent.b1;
        }

        @Override
        public Integer handleSerialize0(String fieldName, Object entity) {
            SubEntity ent = (SubEntity)entity;
            return ent.b1;
        }
        
    }
    
    public static final class Handler3 extends ModifierHandler<Integer>{

        @Override
        public Integer handleDeserialize0(String fieldName, Object entity, InputStream is) {
            return 5;
        }

        @Override
        public Integer handleSerialize0(String fieldName, Object entity) {
            return 5;
        }
        
    }
    
    @Before
    public void setValues() {
        entity.b1 = 1;
        entity.b2 = 2;
        entity.b3 = 3;
        entity.b4 = 4200;
        entity.chars = "abcde";
        entity.chars2 = "F";
        entity.raw1 = new byte[] {1,2,3};
        entity.raw2 = new byte[] {0};
        SubEntity sub = new SubEntity();
        sub.b1 = 1;
        sub.byteList = Arrays.asList(1,2);
        sub.shortList = Arrays.asList(11,22);
        sub.intList = Arrays.asList(11,22);
        sub.bcdList = Arrays.asList(320111,114110);
        sub.strList = Arrays.asList("hahah");
        sub.strList2 = Arrays.asList("a","b","x","<",">");
        SubSubEntity subsub = new SubSubEntity();
        subsub.b1 = 120;
        sub.entityList = Arrays.asList(subsub);
        entity.entity = sub;
    }
    
    
    @Test
    public void testLength() throws ConversionException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        entity.totalLength = entity.length();
        entity.serialize(baos);
        byte[] arr = baos.toByteArray();
        Assert.assertEquals(arr.length, entity.totalLength);
        
        Entity2 e2 = new Entity2();
        e2.deserialize(new ByteArrayInputStream(arr));
        Assert.assertTrue(TestUtils.equalsOrderFields(entity, e2));
    }
    
    public static void main(String[] args) throws ConversionException {
        TestCase3 tc3 = new TestCase3();
        tc3.setValues();
        tc3.testLength();
    }
}
