package test.exceptions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.dzh.bytesutil.ConversionException;
import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.Length;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.types.BCD;
import org.dzh.bytesutil.annotations.types.RAW;
import org.dzh.bytesutil.converters.auxiliary.ModifierHandler;
import org.dzh.bytesutil.converters.auxiliary.Utils;
import org.junit.Assert;
import org.junit.Test;

import test.TestUtils;

public class TestModiferHandlerAndUtils {
    public static class Handler extends ModifierHandler<Integer>{
        @Override
        public Integer handleDeserialize0(String fieldName, Object entity, InputStream is) throws IOException {
            return null;
        }
        @Override
        public Integer handleSerialize0(String fieldName, Object entity) {
            return null;
        }
    };
    public static class Handler2 extends ModifierHandler<Integer>{
        @Override
        public Integer handleDeserialize0(String fieldName, Object entity, InputStream is) throws IOException {
            return -1;
        }
        @Override
        public Integer handleSerialize0(String fieldName, Object entity) {
            return -1;
        }
    };
    public static class Handler3 extends ModifierHandler<Integer>{
        @Override
        public Integer handleDeserialize0(String fieldName, Object entity, InputStream is) throws IOException {
            throw new IOException();
        }
        @Override
        public Integer handleSerialize0(String fieldName, Object entity) {
            return -1;
        }
    };
    @Test
    public void test0() throws ConversionException {
        try {
            class Entity extends DataPacket{@Order(0)@RAW @Length(handler=Handler.class) public byte[] ts = new byte[2];}
            new Entity().serialize(new ByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactExceptionInHierarchy(e, ModifierHandler.class, 1);
        }
        try {
            class Entity extends DataPacket{@Order(0)@RAW @Length(handler=Handler.class) public byte[] ts = new byte[2];}
            new Entity().deserialize(TestUtils.newZeroLengthInputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactExceptionInHierarchy(e, ModifierHandler.class, 1);
        }
    }
    
    @Test
    public void test1() throws ConversionException {
        try {
            class Entity extends DataPacket{@Order(0)@RAW @Length(handler=Handler2.class) public byte[] ts = new byte[2];}
            new Entity().serialize(new ByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactExceptionInHierarchy(e, ModifierHandler.class, 2);
        }
        try {
            class Entity extends DataPacket{@Order(0)@RAW @Length(handler=Handler2.class) public byte[] ts = new byte[2];}
            new Entity().deserialize(TestUtils.newZeroLengthInputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactExceptionInHierarchy(e, ModifierHandler.class, 2);
        }
    }
    
    @Test
    public void test2() throws ConversionException {
        try {
            class Entity extends DataPacket{@Order(0)@BCD(2) public int ts = -1;}
            new Entity().serialize(new ByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactExceptionInHierarchy(e, Utils.class, 5);
        }
        try {
            class Entity extends DataPacket{@Order(0)@BCD(2) public int ts = 133456;}
            new Entity().serialize(new ByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactExceptionInHierarchy(e, Utils.class, 6);
        }
        try {
            class Entity extends DataPacket{@Order(0)@BCD(10) public int ts = 133456;}
            new Entity().serialize(new ByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactExceptionInHierarchy(e, Utils.class, 6);
        }
        try {
            class Entity extends DataPacket{@Order(0)@BCD(7) public String ts = "133456";}
            new Entity().serialize(new ByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactExceptionInHierarchy(e, Utils.class, 7);
        }
    }
    
    @Test
    public void test3() throws ConversionException {
        try {
            class Entity extends DataPacket{@Order(0)@RAW @Length(handler=Handler3.class) public byte[] ts = new byte[2];}
            new Entity().deserialize(TestUtils.newZeroLengthInputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactExceptionInHierarchy(e, ModifierHandler.class, 3);
        }
    }
    
    public static class Handler4 extends ModifierHandler<Integer>{
        @Override
        public Integer handleDeserialize0(String fieldName, Object entity, InputStream is) throws IOException {
            for(int i=0;i<ModifierHandler.HANDLER_READ_BUFFER_SIZE*2;++i) {
                is.read();
            }
            return 1;
        }
        @Override
        public Integer handleSerialize0(String fieldName, Object entity) {
            return 1;
        }
    };
    @Test
    public void test4() throws ConversionException {
        try {
            class Entity extends DataPacket{@Order(0)@RAW @Length(handler=Handler4.class) public byte[] ts;}
            new Entity().deserialize(TestUtils.newInputStream(TestUtils.randomArray(248)));
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactExceptionInHierarchy(e, ModifierHandler.class, 4);
        }
    }
}
