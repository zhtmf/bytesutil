package io.github.zhtmf.converters;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.junit.Test;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.types.INT;
import test.TestUtils;

public class MakeJacocoHappy {
    @Test
    public void test0() throws Exception {
        {
            @SuppressWarnings("rawtypes")
            Constructor c = Converters.class.getDeclaredConstructor();
            c.setAccessible(true);
            c.newInstance();
        }
        {
            class Entity extends DataPacket{@Order(0)@INT int field1;}
            Entity obj = new Entity();
            obj.serialize(TestUtils.newByteArrayOutputStream());
            Method mtd = DataPacket.class.getDeclaredMethod("getClassInfo");
            mtd.setAccessible(true);
            ClassInfo ci = (ClassInfo) mtd.invoke(obj);
            ci.fieldInfoList().get(0).toString();
        }
    }
    
}
