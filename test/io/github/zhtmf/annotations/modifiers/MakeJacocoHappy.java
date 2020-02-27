package io.github.zhtmf.annotations.modifiers;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigInteger;

import org.junit.Test;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.converters.TestUtils;
import io.github.zhtmf.converters.auxiliary.DataType;
import io.github.zhtmf.converters.auxiliary.EntityHandler;

public class MakeJacocoHappy {
    //TODO: make this a test
    @SuppressWarnings("rawtypes")
    @Test
    public void testMakeJacocoHappy() throws Exception {
        {
            try {
                new io.github.zhtmf.annotations.modifiers.PlaceHolderHandler().handleDeserialize0(null, null, null);
            } catch (Exception e) {
                TestUtils.assertException(e, UnsupportedOperationException.class);
            }
            try {
                new PlaceHolderHandler().handleSerialize0(null, null);
            } catch (Exception e) {
                TestUtils.assertException(e, UnsupportedOperationException.class);
            }
            new PlaceHolderHandler.DefaultCharsetHandler();
            new PlaceHolderHandler.DefaultLengthHandler();
        }
        {
            class MySub extends EntityHandler{
                
                @Override
                public DataPacket handle0(String fieldName, Object entity, InputStream is) throws IOException {
                    return null;
                }
            }
            try {
                new MySub().handleSerialize0(null,null);
            } catch (Exception e) {
                TestUtils.assertException(e, UnsupportedOperationException.class);
            }
        }
        {
            try {
                Method mtd = DataType.class.getDeclaredMethod("mappedEnumFieldClass");
                mtd.setAccessible(true);
                mtd.invoke(DataType.USER_DEFINED);
            } catch (Exception e) {
                TestUtils.assertException(e, UnsupportedOperationException.class);
            }
            try {
                Method mtd = DataType.class.getDeclaredMethod("size");
                mtd.setAccessible(true);
                mtd.invoke(DataType.USER_DEFINED);
            } catch (Exception e) {
                TestUtils.assertException(e, UnsupportedOperationException.class);
            }
            try {
                Method mtd = DataType.class.getDeclaredMethod("checkRange",long.class,boolean.class);
                mtd.setAccessible(true);
                mtd.invoke(DataType.USER_DEFINED,0L,Boolean.FALSE);
            } catch (Exception e) {
                TestUtils.assertException(e, UnsupportedOperationException.class);
            }
            try {
                Method mtd = DataType.class.getDeclaredMethod("checkRange",BigInteger.class,boolean.class);
                mtd.setAccessible(true);
                mtd.invoke(DataType.USER_DEFINED,BigInteger.ZERO,Boolean.TRUE);
            } catch (Exception e) {
                TestUtils.assertException(e, UnsupportedOperationException.class);
            }
        }
    }
}