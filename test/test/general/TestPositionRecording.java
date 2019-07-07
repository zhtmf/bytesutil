package test.general;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.CHARSET;
import io.github.zhtmf.annotations.modifiers.Conditional;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.types.BYTE;
import io.github.zhtmf.annotations.types.CHAR;
import io.github.zhtmf.annotations.types.INT;
import io.github.zhtmf.annotations.types.RAW;
import io.github.zhtmf.converters.auxiliary.ModifierHandler;
import test.TestUtils;

public class TestPositionRecording {
    
    @CHARSET("UTF-8")
    public static class Test1 extends DataPacket{
        @INT
        @Order(0)
        public int int1 = 30;
        @BYTE
        @Order(1)
        public byte b= 100;
        @RAW
        @Order(2)
        @Length(handler=TmpModifierHandler.class)
        public byte[] checkPoint1 = new byte[] {0}; //5
        @CHAR(6)
        @Order(3)
        public String str = "abcdef";
        @RAW
        @Order(4)
        @Length(handler=TmpModifierHandler.class)
        public byte[] checkPoint2 = new byte[] {0};//12
        @CHAR
        @Length(handler=TmpModifierHandler2.class)
        @Order(5)
        public String str2 = "abc";
        @RAW(6)
        @Order(6)
        public byte[] interm = {0,1,2,3,4,5};
        @RAW
        @Order(7)
        @Length(handler=TmpModifierHandler.class)
        public byte[] checkPoint3 = new byte[] {0};//22
        @INT
        @Order(8)
        public int int2 = 30;
        @INT
        @Order(9)
        public int int3 = 30;
        @RAW
        @Order(10)
        @Length(handler=TmpModifierHandler.class)
        public byte[] checkPoint4 = new byte[] {0};//31
    }
    
    public static final class TmpModifierHandler2 extends ModifierHandler<Integer>{
        @Override
        public Integer handleDeserialize0(String fieldName, Object entity, InputStream is) throws IOException {
            is.read();
            is.read();
            is.read();
            is.read();
            is.read();
            is.read();
            return 3;
        }
        @Override
        public Integer handleSerialize0(String fieldName, Object entity) {
            return 3;
        }
    }
    
    public static final class TmpModifierHandler extends ModifierHandler<Integer>{
        @Override
        public Integer handleDeserialize0(String fieldName, Object entity, InputStream is) throws IOException {
            int expected = 0;
            switch(fieldName) {
            case "checkPoint1":
                expected = 5;break;
            case "checkPoint2":
                expected = 12;break;
            case "checkPoint3":
                expected = 22;break;
            case "checkPoint4":
                expected = 31;break;
            }
            Assert.assertEquals(expected,super.currentPosition());
            return 1;
        }
        @Override
        public Integer handleSerialize0(String fieldName, Object entity) {
            return 1;
        }
    }
    
    @Test
    public void test() throws Exception {
        TestUtils.serializeAndRestore(new Test1());
    }
    
    public static class Level1 extends DataPacket{
        @Order(0)
        @BYTE
        @Conditional(Test2Conditional.class)
        public int level1Field1;
        @Order(1)
        @BYTE
        @Conditional(Test2Conditional.class)
        public int level1Field2;
        @Order(2)
        @Conditional(Test2Conditional.class)
        public Level2 level2;
        @Order(3)
        @BYTE
        @Conditional(Test2Conditional.class)
        public int level1Field4;
    }
    
    public static class Level2 extends DataPacket{
        @Order(0)
        @BYTE
        @Conditional(Test2Conditional.class)
        public int level2Field1;
        @Order(1)
        @Conditional(Test2Conditional.class)
        public Level3 level3;
        @Order(2)
        @BYTE
        @Conditional(Test2Conditional.class)
        public int level2Field3;
    }
    
    public static class Level3 extends DataPacket{
        @Order(0)
        @BYTE
        @Conditional(Test2Conditional.class)
        public int level3Field1;
        @Order(1)
        @BYTE
        @Conditional(Test2Conditional.class)
        public int level3Field2;
    }
    
    public static class Test2Conditional extends ModifierHandler<Boolean>{

        @Override
        public Boolean handleDeserialize0(String fieldName, Object entity, InputStream is) throws IOException {
            int offset = super.currentPosition();
            switch(fieldName) {
            case "level1Field1":Assert.assertEquals(offset, 0);break;
            case "level1Field2":Assert.assertEquals(offset, 1);break;
            case "level2":Assert.assertEquals(offset, 2);break;
            case "level2Field1":Assert.assertEquals(offset, 0);break;
            case "level3":Assert.assertEquals(offset, 1);break;
            case "level3Field1":Assert.assertEquals(offset, 0);break;
            case "level3Field2":Assert.assertEquals(offset, 1);break;
            case "level2Field3":Assert.assertEquals(offset, 3);break;
            case "level1Field4":Assert.assertEquals(offset, 6);break;
            }
            return true;
        }

        @Override
        public Boolean handleSerialize0(String fieldName, Object entity) {
            return true;
        }
    }
    
    @Test
    public void test2() throws Exception {
        Level1 level1 = new Level1();
        level1.level2 = new Level2();
        level1.level2.level3 = new Level3();
        TestUtils.serializeAndRestore(level1);
    }
}
