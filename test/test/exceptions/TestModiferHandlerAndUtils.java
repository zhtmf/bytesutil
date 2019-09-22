package test.exceptions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import io.github.zhtmf.ConversionException;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.types.RAW;
import io.github.zhtmf.converters.auxiliary.ModifierHandler;
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
    public void test3() throws ConversionException {
        try {
            class Entity extends DataPacket{@Order(0)@RAW @Length(handler=Handler3.class) public byte[] ts = new byte[2];}
            new Entity().deserialize(TestUtils.newZeroLengthInputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactExceptionInHierarchy(e, ModifierHandler.class, 3);
        }
    }
}
