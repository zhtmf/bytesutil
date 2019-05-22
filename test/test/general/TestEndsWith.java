package test.general;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.CHARSET;
import io.github.zhtmf.annotations.modifiers.EndsWith;
import io.github.zhtmf.annotations.modifiers.ListLength;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.types.BYTE;
import io.github.zhtmf.annotations.types.CHAR;
import io.github.zhtmf.annotations.types.RAW;
import io.github.zhtmf.annotations.types.SHORT;
import test.TestUtils;
import test.TestUtils.Provider;

public class TestEndsWith {
    @Test
    public void testByte() throws Exception {
        //General Test
        class Entity extends DataPacket{
            @BYTE
            @Order(-1)
            public int integer2;
            @CHAR
            @Order(0)
            @EndsWith({0x17,0x69})
            public String str1;
            @RAW(3)
            @Order(1)
            public int[] integer1;
            @CHAR
            @CHARSET("UTF-8")
            @Order(2)
            @ListLength(3)
            @EndsWith({0x0})
            public List<String> strList;
        }
        Entity entity = new Entity();
        entity.integer2 = 30;
        entity.str1 = "abc";
        entity.integer1 = new int[] {3,4,5};
        entity.strList = Arrays.asList("1","a","啊啊啊");
        TestUtils.serializeMultipleTimesAndRestore(entity, 10,new Provider<Entity>() {

            @Override
            public Entity newInstance() {
                return new Entity();
            }
            
        });
    }
    @Test
    public void testByte2() throws Exception {
        //When strings are of length zero
        class Entity extends DataPacket{
            @SHORT
            @Order(-1)
            public int integer2;
            @CHAR
            @Order(0)
            @EndsWith({0x17,0x69})
            public String str1;
            @RAW(2)
            @Order(1)
            public int[] integer1;
            @CHAR
            @Order(2)
            @ListLength(2)
            @EndsWith({0x17,0x0})
            public List<String> strList;
        }
        Entity entity = new Entity();
        entity.integer2 = 133;
        entity.str1 = "";
        entity.integer1 = new int[] {1,2};
        entity.strList = Arrays.asList("","");
        TestUtils.serializeMultipleTimesAndRestore(entity, 10, new Provider<Entity>() {

            @Override
            public Entity newInstance() {
                return new Entity();
            }
            
        });
    }
    @Test
    public void testByte3() throws Exception {
        //When strings contains part of the ending array
        class Entity extends DataPacket{
            @SHORT
            @Order(-1)
            public int integer2;
            @CHAR
            @Order(0)
            @EndsWith({0x17,0x69,0x33,0x34})
            public String str1;
            @RAW(2)
            @Order(1)
            public int[] integer1;
            @CHAR
            @Order(2)
            @ListLength(2)
            @EndsWith({0x08,0x07,0x08,0x07,0x05,0x08,0x07})
            public List<String> strList;
            @SHORT
            @Order(3)
            public int integer3;
        }
        Entity entity = new Entity();
        entity.integer2 = 133;
        entity.str1 = new String(new byte[] {0x17,0x69,0x33,0x69,0x69,0x70},StandardCharsets.ISO_8859_1);
        entity.integer1 = new int[] {1,2};
        entity.strList = Arrays.asList(
                new String(new byte[] {0x08,0x07,0x08,0x08,0x07,0x08,0x07,0x05,0x08},StandardCharsets.ISO_8859_1)
                ,new String(new byte[] {
                        0x08,0x08,0x08,0x08,0x07,0x07,0x07,0x03,0x07,0x06,0x08,0x08,0x07,0x08,0x07,0x05,0x08},StandardCharsets.ISO_8859_1));
        entity.integer3 = 12345;
        TestUtils.serializeMultipleTimesAndRestore(entity, 10, new Provider<Entity>() {

            @Override
            public Entity newInstance() {
                return new Entity();
            }
            
        });
    }
}
