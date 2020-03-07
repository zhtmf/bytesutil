package io.github.zhtmf.converters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import io.github.zhtmf.ConversionException;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.CHARSET;
import io.github.zhtmf.annotations.modifiers.Conditional;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Script;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.BYTE;
import io.github.zhtmf.annotations.types.CHAR;
import io.github.zhtmf.annotations.types.RAW;
import io.github.zhtmf.converters.auxiliary.ModifierHandler;

public class TestCaseScriptModifierHandler {
    
    @Unsigned
    public static class EntityA extends DataPacket{
        @BYTE
        @Order(0)
        public int len;
        @RAW
        @Length(scripts = @Script(value = "", deserialize = "entity.len"))
        @Order(1)
        public byte[] array;
    }
    
    @Test
    public void testA() throws ConversionException {
        byte[] data = new byte[] {3,4,5,6};
        EntityA entity = new EntityA();
        entity.deserialize(TestUtils.newInputStream(data));
        assertEquals(entity.len, 3);
        try {
            entity.serialize(TestUtils.newByteArrayOutputStream());
            fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, ScriptModifierHandler.class, 4);
        }
    }
    
    @Unsigned
    public static class EntityB extends DataPacket{
        @BYTE
        @Order(0)
        public int len;
        @RAW
        @Length(scripts = @Script(value = "entity.len", deserialize = ""))
        @Order(1)
        public byte[] array;
    }
    
    @Test
    public void testB() throws ConversionException {
        byte[] data = new byte[] {3,4,5,6};
        EntityB entity = new EntityB();
        try {
            entity.deserialize(TestUtils.newInputStream(data));
            fail();
        } catch (Exception e) {
            TestUtils.assertExactExceptionInHierarchy(e, ScriptModifierHandler.class, 3);
        }
    }
    
    @Unsigned
    public static class EntityC extends DataPacket{
        @BYTE
        @Order(0)
        public int len;
        @RAW
        @Length(scripts = @Script(value = "null"))
        @Order(1)
        public byte[] array;
    }
    
    @Test
    public void testC() throws ConversionException {
        byte[] data = new byte[] {3,4,5,6};
        EntityC entity = new EntityC();
        try {
            entity.deserialize(TestUtils.newInputStream(data));
            fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, ModifierHandler.class, 1);
        }
    }
    
    @Unsigned
    public static class EntityD extends DataPacket{
        @BYTE
        @Order(0)
        public int len;
        @RAW
        @Length(scripts = @Script(value = "return 'abc';", deserialize = "return 'abc';"))
        @Order(1)
        public byte[] array;
    }
    
    @Test
    public void testD() throws ConversionException {
        byte[] data = new byte[] {3,4,5,6};
        EntityD entity = new EntityD();
        try {
            entity.deserialize(TestUtils.newInputStream(data));
            fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, ScriptModifierHandler.class, 2);
        }
    }
    
    @Unsigned
    public static class EntityE extends DataPacket{
        @BYTE
        @Order(0)
        public int len;
        @CHAR(3)
        @Conditional(scripts = @Script("return true;"))
        @CHARSET(scripts = @Script("return 'utf-8';"))
        @Order(1)
        public String str;
    }
    
    @Test
    public void testE() throws ConversionException {
        EntityE entity = new EntityE();
        entity.len = 5;
        entity.str = "abc";
        entity.serialize(TestUtils.newByteArrayOutputStream());
    }
    
    @Unsigned
    public static class EntityF extends DataPacket{
        @BYTE
        @Order(0)
        public int len;
        @CHAR(3)
        @Conditional(scripts = @Script("return true;"))
        @CHARSET(scripts = @Script("return 3;"))
        @Order(1)
        public String str;
    }
    
    @Test
    public void testF() throws ConversionException {
        EntityF entity = new EntityF();
        entity.len = 5;
        entity.str = "abc";
        try {
            entity.serialize(TestUtils.newByteArrayOutputStream());
            fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, ScriptModifierHandler.class, 2);
        }
    }
    
    @Unsigned
    public static class EntityG extends DataPacket{
        @BYTE
        @Order(0)
        public int len;
        @CHAR(3)
        @Conditional(scripts = @Script("return handler.abcdef*3;"))
        @CHARSET(scripts = @Script("return 3;"))
        @Order(1)
        public String str;
    }
    
    @Test
    public void testG() throws ConversionException {
        EntityG entity = new EntityG();
        entity.len = 5;
        entity.str = "abc";
        try {
            entity.serialize(TestUtils.newByteArrayOutputStream());
            fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, ScriptModifierHandler.class, 1);
        }
        try {
            entity.deserialize(TestUtils.newInputStream(new byte[] {3,4,5,6,7,8,9,10,0,0,1,2,3}));
            fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, ScriptModifierHandler.class, 0);
        }
    }
}
