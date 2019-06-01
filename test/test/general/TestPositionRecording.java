package test.general;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.CHARSET;
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
}
