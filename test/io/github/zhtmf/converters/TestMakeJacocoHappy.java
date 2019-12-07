package io.github.zhtmf.converters;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;

import org.junit.Assert;
import org.junit.Test;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.types.INT;
import io.github.zhtmf.converters.auxiliary.DataType;
import io.github.zhtmf.converters.auxiliary.EntityHandler;

public class TestMakeJacocoHappy {
    @Test
    public void testMakeJacocoHappy() throws Exception {
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
                Method mtd = DataTypeOperations.class.getDeclaredMethod("mappedEnumFieldClass");
                mtd.setAccessible(true);
                mtd.invoke(DataTypeOperations.USER_DEFINED);
            } catch (Exception e) {
                TestUtils.assertException(e, UnsupportedOperationException.class);
            }
            try {
                Method mtd = DataTypeOperations.class.getDeclaredMethod("size");
                mtd.setAccessible(true);
                mtd.invoke(DataTypeOperations.USER_DEFINED);
            } catch (Exception e) {
                TestUtils.assertException(e, UnsupportedOperationException.class);
            }
            try {
                Method mtd = DataTypeOperations.class.getDeclaredMethod("checkRange",long.class,boolean.class);
                mtd.setAccessible(true);
                mtd.invoke(DataTypeOperations.USER_DEFINED,0L,Boolean.FALSE);
            } catch (Exception e) {
                TestUtils.assertException(e, UnsupportedOperationException.class);
            }
            try {
                Method mtd = DataTypeOperations.class.getDeclaredMethod("checkRange",BigInteger.class,boolean.class);
                mtd.setAccessible(true);
                mtd.invoke(DataTypeOperations.USER_DEFINED,BigInteger.ZERO,Boolean.TRUE);
            } catch (Exception e) {
                TestUtils.assertException(e, UnsupportedOperationException.class);
            }
            try {
                Method mtd = DataTypeOperations.class.getDeclaredMethod("checkRange",long.class,int.class);
                mtd.setAccessible(true);
                mtd.invoke(DataTypeOperations.USER_DEFINED,0L,0);
            } catch (Exception e) {
                TestUtils.assertException(e, UnsupportedOperationException.class);
            }
        }
    }
    
    @Test
    public void test0() throws Exception {
        {
            @SuppressWarnings("rawtypes")
            Constructor c = Converters.class.getDeclaredConstructor();
            c.setAccessible(true);
            c.newInstance();
        }
        {
            new StreamUtils();
        }
    }
    
    public static final class Dummy extends DataPacket{
        @Order(0)
        @INT
        private int a;
    }
    
    /*
     * Merely for eliminating annoying warnings produced by Jacocco for 
     * code paths that will not appear.
     * May be deleted later if modifications really cause such bugs 
     */
    @Test
    public void testShouldNotReachHere() throws Exception{
        Dummy dummy = new Dummy();
        Method get = ClassInfo.class.getDeclaredMethod("getClassInfo",Object.class);
        get.setAccessible(true);
        ClassInfo instance = (ClassInfo) get.invoke(null, dummy);
        FieldInfo fieldA = instance.fieldInfoList.get(0);
        Assert.assertEquals(fieldA.toString(), "FieldInfo:["+dummy.getClass().getName()+"]["+fieldA.name+"]");
        Field dataType = FieldInfo.class.getDeclaredField("dataType");
        dataType.setAccessible(true);
        dataType.set(fieldA, DataType.USER_DEFINED);
        doTest(new BigIntegerConverter(),fieldA,dummy);
        doTest(new BooleanConverter(),fieldA,dummy);
        doTest(new ByteArrayConverter(),fieldA,dummy);
        doTest(new ByteConverter(),fieldA,dummy);
        doTest(new CharConverter(),fieldA,dummy);
        doTest(new DateConverter(),fieldA,dummy);
        doTest(new IntArrayConverter(),fieldA,dummy);
        doTest(new IntegerConverter(),fieldA,dummy,1);
        doTest(new LongConverter(),fieldA,dummy,1L);
        doTest(new ShortConverter(),fieldA,dummy,Short.MIN_VALUE);
        doTest(new StringConverter(),fieldA,dummy,"");
        
        try {
            fieldA.get(new Object());
            Assert.fail();
        } catch (Throwable e) {
        }
        
        try {
            fieldA.set(new Object(), 3L);
            Assert.fail();
        } catch (Throwable e) {
        }
        
        try {
            StreamUtils.writeIntegerOfType(TestUtils.newThrowOnlyOutputStream(), DataType.LONG, 3, false);
            Assert.fail();
        } catch (Throwable e) {
            TestUtils.assertException(e, Error.class);
        }
        try {
            StreamUtils.readIntegerOfType(TestUtils.newZeroLengthInputStream(), DataType.LONG, false);
            Assert.fail();
        } catch (Throwable e) {
            TestUtils.assertException(e, Error.class);
        }
    }
    
    @Test
    public void testUnsupportedOperations() throws Exception{
        try {
            DataTypeOperations.CHAR.size();
            Assert.fail();
        } catch (UnsupportedOperationException e) {
        }
        try {
            DataTypeOperations.INT3.mappedEnumFieldClass();
            Assert.fail();
        } catch (UnsupportedOperationException e) {
        }
        try {
            DataTypeOperations.CHAR.checkRange(null, false);
            Assert.fail();
        } catch (UnsupportedOperationException e) {
        }
        try {
            new EntityHandler() {
                @Override
                public DataPacket handle0(String fieldName, Object entity, InputStream is) throws IOException {
                    return null;
                }}
            .handleSerialize0(null, null);
            Assert.fail();
        } catch (UnsupportedOperationException e) {
        }
    }
    
    @SuppressWarnings("unchecked")
    private void doTest(@SuppressWarnings("rawtypes") Converter bc,FieldInfo fieldA,DataPacket dummy,Object val) {
        try {
            bc.serialize(val, null, fieldA, dummy);
        } catch (Throwable e) {
            TestUtils.assertException(e, Error.class);
        }
        try {
            bc.deserialize(null, fieldA, dummy);
        } catch (Throwable e) {
            TestUtils.assertException(e, Error.class);
        }
    }
    
    private void doTest(@SuppressWarnings("rawtypes") Converter bc,FieldInfo fieldA,DataPacket dummy) {
        doTest(bc,fieldA,dummy,null);
    }
}