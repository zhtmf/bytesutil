package io.github.zhtmf;

import org.junit.Assert;
import org.junit.Test;

public class TestConversionException {
    @Test
    public void testToString() throws Exception {
        Class<?> cls = TestConversionException.class;
        String fieldName = "field1";
        String msg = "error";
        ConversionException exp = new ConversionException(TestConversionException.class, fieldName, msg);
        Assert.assertEquals(exp.toString(), String.format("Entity Class[%s], field [%s], error:[%s]",cls, fieldName,msg));
    }
}
