package io.github.zhtmf;

import java.util.Calendar;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Signed;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.INT;
import io.github.zhtmf.converters.TestUtils;

public class TestIntegralToDate {
    @Test
    public void test() throws Exception{
        @Signed
        class Entity extends DataPacket{
            @Order(0)
            @INT
            Date date1;
        }
        Entity entity = new Entity();
        entity.date1 = new Date();
        Entity entity2 = new Entity();
        entity2.deserialize(TestUtils.serializeAndGetBytesAsInputStream(entity));
        Assert.assertEquals(entity.date1.getTime()/1000*1000, entity2.date1.getTime());
        
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MILLISECOND, 0);
        entity.date1 = cal.getTime();
        Entity entity3 = new Entity();
        entity3.deserialize(TestUtils.serializeAndGetBytesAsInputStream(entity));
        Assert.assertEquals(entity.date1.getTime(), entity3.date1.getTime());
    }    
    @Test
    public void test2() throws Exception{
        @Unsigned
        class Entity extends DataPacket{
            @Order(0)
            @INT
            Date date1;
        }
        Entity entity = new Entity();
        entity.date1 = new Date();
        Entity entity2 = new Entity();
        entity2.deserialize(TestUtils.serializeAndGetBytesAsInputStream(entity));
        Assert.assertEquals(entity.date1.getTime()/1000*1000, entity2.date1.getTime());
        
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MILLISECOND, 0);
        entity.date1 = cal.getTime();
        Entity entity3 = new Entity();
        entity3.deserialize(TestUtils.serializeAndGetBytesAsInputStream(entity));
        Assert.assertEquals(entity.date1.getTime(), entity3.date1.getTime());
    }
}