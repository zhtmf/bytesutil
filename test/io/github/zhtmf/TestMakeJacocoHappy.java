package io.github.zhtmf;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;

import org.junit.Assert;
import org.junit.Test;

import io.github.zhtmf.DataPacket.AuxiliaryAccess;

public class TestMakeJacocoHappy {
    @Test
    public void testMakeJacocoHappy() throws Exception {
        {
            Field field = DataPacket.class.getDeclaredField("auxiliaryAccess");
            field.setAccessible(true);
            Object access = field.get(null);
            Assert.assertNotNull(access);
            DataPacket.setAuxiliaryAccess(new AuxiliaryAccess() {
                @Override
                public void serialize(Object self, OutputStream dest) throws ConversionException, IllegalArgumentException {
                }
                
                @Override
                public int length(Object self) throws IllegalArgumentException {
                    return 0;
                }
                
                @Override
                public void deserialize(Object self, InputStream src) throws ConversionException, IllegalArgumentException {
                }
            });
            Object mayBeAltered = field.get(null);
            Assert.assertEquals(access, mayBeAltered);
        }
    }
}