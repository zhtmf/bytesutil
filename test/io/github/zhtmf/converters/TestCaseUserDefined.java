package io.github.zhtmf.converters;

import java.sql.Timestamp;

import org.junit.Test;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.TestCaseUserDefined.Converter2;
import io.github.zhtmf.annotations.modifiers.BigEndian;
import io.github.zhtmf.annotations.modifiers.CHARSET;
import io.github.zhtmf.annotations.modifiers.DatePattern;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Signed;
import io.github.zhtmf.annotations.types.UserDefined;

public class TestCaseUserDefined {
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
    @Test
    public void test3() throws Exception {
        Entity2 entity = new Entity2();
        entity.ts = Timestamp.valueOf("2011-1-1 23:00:59.333");
        try {
            TestUtils.serializeMultipleTimesAndRestore(entity);
        } catch (Exception e) {
            TestUtils.assertExactExceptionInHierarchy(e, UserDefinedTypeConverter.class, 1);
        }
    }
    @Test
    public void test4() throws Exception {
        Entity2 entity = new Entity2();
        try {
            entity.deserialize(TestUtils.newZeroLengthInputStream());
        } catch (Exception e) {
            TestUtils.assertExactExceptionInHierarchy(e, UserDefinedTypeConverter.class, 2);
        }
    }
}
