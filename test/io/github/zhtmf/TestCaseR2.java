package io.github.zhtmf;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.ListLength;
import io.github.zhtmf.annotations.modifiers.LittleEndian;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Script;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.CHAR;
import io.github.zhtmf.annotations.types.INT;
import io.github.zhtmf.converters.auxiliary.DataType;

public class TestCaseR2 {
    
    @LittleEndian
    @Unsigned
    public static class Entity extends DataPacket{
        //list with write-ahead length
        @Order(1)
        @Length(type=DataType.SHORT)
        @CHAR(5)
        public List<String> str;
        //list with definite length
        @Order(2)
        @Length(3)
        @CHAR(1)
        public List<String> str2;
        //list with handler
        @Order(4)
        @Length(scripts = @Script("2"))
        @CHAR(1)
        public List<String> str3;
        
        //same list, but marked with ListLength
        
        //list with write-ahead length
        @Order(5)
        @ListLength(type=DataType.SHORT)
        @INT
        public List<Integer> integer1;
        //list with definite length
        @Order(6)
        @ListLength(3)
        @INT
        public List<Integer> integer2;
        //list with handler
        @Order(8)
        @ListLength(scripts = @Script("2"))
        @INT
        public List<Integer> integer3;
    }
    
    @Test
    public void testOrder() throws ConversionException {
        Entity ent = new Entity();
        ent.str = Arrays.asList("abcde","ttttt");
        ent.str2 = Arrays.asList("a","b","c");
        ent.str3 = Arrays.asList("d","f");
        ent.integer1 = Arrays.asList(123,444,555,77777);
        ent.integer2 = Arrays.asList(111,222,333);
        ent.integer3 = Arrays.asList(1,2);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ent.serialize(baos);
        Assert.assertEquals(baos.toByteArray().length, ent.length());
    }
}
