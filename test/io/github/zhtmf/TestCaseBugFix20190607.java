package io.github.zhtmf;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.BigEndian;
import io.github.zhtmf.annotations.modifiers.CHARSET;
import io.github.zhtmf.annotations.modifiers.ListLength;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Signed;
import io.github.zhtmf.annotations.types.SHORT;
import io.github.zhtmf.converters.auxiliary.exceptions.TestUtils;

//testing conversion of datapacket when called from within ListConverter
//a bug-fix
public class TestCaseBugFix20190607 {
    
    public static final class Sub1 extends DataPacket{
        @Order(0)
        @SHORT
        public int i1 = 5;
    }
    
    @Signed
    @BigEndian
    @CHARSET("UTF-8")
    public static final class Entity extends DataPacket{
        @Order(1)
        @ListLength(1)
        public List<Sub1> subs = Arrays.asList(new Sub1());
        @Order(2)
        public Sub1 sub1 = new Sub1();
    }

    @Test
    public void test() throws Exception {
        Entity obj = new Entity();
        TestUtils.serializeMultipleTimesAndRestore(obj);
        
        InputStream in = TestUtils.serializeAndGetBytesAsInputStream(obj);
        Sub1 original = obj.sub1;
        obj.deserialize(in);
        //always create a new object
        Assert.assertTrue(original!=obj.sub1);
    }
}
