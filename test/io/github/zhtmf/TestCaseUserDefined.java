package io.github.zhtmf;

import java.io.IOException;
import java.sql.Timestamp;

import org.junit.Assert;
import org.junit.Test;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.TypeConverter;
import io.github.zhtmf.TypeConverter.Input;
import io.github.zhtmf.TypeConverter.Output;
import io.github.zhtmf.annotations.modifiers.BigEndian;
import io.github.zhtmf.annotations.modifiers.CHARSET;
import io.github.zhtmf.annotations.modifiers.DatePattern;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Signed;
import io.github.zhtmf.annotations.types.UserDefined;
import io.github.zhtmf.converters.TestUtils;

public class TestCaseUserDefined {
    @Signed
    @BigEndian
    @CHARSET("UTF-8")
    public static final class Entity extends DataPacket{
        @Order(0)
        @UserDefined(Converter.class)
        @DatePattern("yyyy-MM-dd")
        @Length(8)
        public Timestamp ts;
    }
    
    public static class Converter extends TypeConverter<Timestamp>{
        @Override
        public void serialize(Timestamp obj, TypeConverter.Output context) throws IOException {
            context.writeLong(Long.MAX_VALUE);
            Assert.assertEquals(context.written(), 8);
            context.writeLong(8);
        }
        @Override
        public Timestamp deserialize(Input context) throws IOException{
            Assert.assertEquals(context.available(), 8);
            context.readLong();
            Assert.assertEquals(context.available(), 0);
            context.readLong();
            return null;
        }
    }
    @Test
    public void test1() throws Exception {
        Entity entity = new Entity();
        entity.ts = Timestamp.valueOf("2011-1-1 23:00:59.333");
        try {
            TestUtils.serializeMultipleTimesAndRestore(entity);
        } catch (Exception e) {
            TestUtils.assertExactExceptionInHierarchy(e, Output.class, 1);
        }
    }
    @Test
    public void test2() throws Exception {
        Entity entity = new Entity();
        try {
            entity.deserialize(TestUtils.newInputStream(new byte[] {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}));
        } catch (Exception e) {
            TestUtils.assertExactExceptionInHierarchy(e, Input.class, 1);
        }
    }
    
    @Signed
    @BigEndian
    @CHARSET("UTF-8")
    public static final class Entity2 extends DataPacket{
        @Order(0)
        @UserDefined(Converter2.class)
        @DatePattern("yyyy-MM-dd")
        @Length(8)
        public Timestamp ts;
    }
    
    public static class Converter2 extends TypeConverter<Timestamp>{
        @Override
        public void serialize(Timestamp obj, TypeConverter.Output context) throws IOException {
            context.writeInt(3);
            Assert.assertEquals(context.written(), 4);
        }
        @Override
        public Timestamp deserialize(Input context) throws IOException{
            Assert.assertEquals(context.available(), 8);
            return null;
        }
    }
}
