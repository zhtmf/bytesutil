package io.github.zhtmf.converters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

import org.junit.Assert;

import io.github.zhtmf.ConversionException;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.converters.ExactException;

public class TestUtils {
    public static boolean assertException(Throwable leaf, Class<? extends Throwable> expected) {
        Throwable original = leaf;
        while(leaf!=null) {
            if(leaf.getClass() == expected) {
                return true;
            }
            leaf = leaf.getCause();
        }
        throw new IllegalArgumentException(expected+" not found",original);
    }
    public static boolean assertExactException(Throwable ex,Class<?> site, int ordinal) {
        if(!(ex instanceof ExactException)) {
            ex.printStackTrace();
            throw new IllegalArgumentException(ex.getClass()+" not expected");
        }
        if(((ExactException)ex).getSite() != site
        || ((ExactException)ex).getOrdinal() != ordinal){
        	ex.printStackTrace();
            throw new IllegalArgumentException(ex+" "+((ExactException)ex).getSite()+" "+((ExactException)ex).getOrdinal()+" not expected");
        }
        return true;
    }
    public static boolean assertExactException(Throwable ex,String fullName, int ordinal) {
        if(!((ex instanceof ExactException)
        && ((ExactException)ex).getSite().getName().equals(fullName)
        && ((ExactException)ex).getOrdinal() == ordinal)){
        	ex.printStackTrace();
            throw new IllegalArgumentException(ex+" "+((ExactException)ex).getSite()+" "+((ExactException)ex).getOrdinal()+" not expected");
        }
        return true;
    }
    public static boolean assertExactExceptionInHierarchy(Throwable ex,Class<?> site, int ordinal) {
        Throwable original = ex;
        while(ex!=null) {
            if(!((ex instanceof ExactException)
                    && ((ExactException)ex).getSite() == site
                    && ((ExactException)ex).getOrdinal() == ordinal)){
                ex = ex.getCause();
            }else {
                return true;
            }
        }
        if(original instanceof ExactException) {
            throw new IllegalArgumentException(
                    original+" "+((ExactException)original).getSite()+" "+((ExactException)original).getOrdinal()+" not expected",original);
        }else {
            throw new IllegalArgumentException(original+" not expected",original);

        }
    }
    public static ByteArrayOutputStream newByteArrayOutputStream() {
        return new ByteArrayOutputStream();
    }
    public static InputStream newZeroLengthInputStream() {
        return new ByteArrayInputStream(new byte[0]);
    }
    public static OutputStream newThrowOnlyOutputStream() {
        return new OutputStream() {
            
            @Override
            public void write(int b) throws IOException {
                throw new IOException("throws");
            }
        };
    }
    public static InputStream newInputStream(byte[] arr) {
        return new ByteArrayInputStream(arr);
    }
    public static byte[] serializeAndGetBytes(DataPacket src) throws IllegalArgumentException, ConversionException {
        ByteArrayOutputStream baos = newByteArrayOutputStream();
        src.serialize(baos);
        return baos.toByteArray();
    }
    public static InputStream serializeAndGetBytesAsInputStream(DataPacket src) throws IllegalArgumentException, ConversionException {
        ByteArrayOutputStream baos = newByteArrayOutputStream();
        src.serialize(baos);
        return newInputStream(baos.toByteArray());
    }
    public static void serializeAndRestore(DataPacket entity) throws Exception {
        ByteArrayOutputStream os = newByteArrayOutputStream();
        entity.serialize(os);
        byte[] data = os.toByteArray();
        DataPacket restored = entity.getClass().newInstance();
        restored.deserialize(newInputStream(data));
        Assert.assertTrue(equalsOrderFields(entity, restored));
        Assert.assertEquals(data.length, entity.length());
    }
    public static interface Provider<T>{
        T newInstance();
    }
    public static <T extends DataPacket> 
    void serializeMultipleTimesAndRestore(DataPacket entity, int times, Provider<T> t) throws Exception {
        ByteArrayOutputStream os = newByteArrayOutputStream();
        for(int i=0;i<times;++i) {
            entity.serialize(os);
        }
        byte[] data = os.toByteArray();
        Assert.assertEquals(data.length,entity.length()*times);
        InputStream is = newInputStream(data);
        for(int i=0;i<times;++i) {
            DataPacket restored = t.newInstance();
            restored.deserialize(is);
            Assert.assertTrue(equalsOrderFields(entity, restored));
        }
    }
    public static void serializeMultipleTimesAndRestore(DataPacket entity, int times) throws Exception {
        ByteArrayOutputStream os = newByteArrayOutputStream();
        for(int i=0;i<times;++i) {
            entity.serialize(os);
        }
        byte[] data = os.toByteArray();
        Assert.assertEquals(data.length,entity.length()*times);
        InputStream is = newInputStream(data);
        for(int i=0;i<times;++i) {
            DataPacket restored = entity.getClass().newInstance();
            restored.deserialize(is);
            Assert.assertTrue("failed on iteration "+i, equalsOrderFields(entity, restored));
        }
    }
    public static byte[] pseudoRandomArray(int size) {
        byte[] array = new byte[size];
        array[0] = (byte) (Math.random()*127);
        for(int i=1;i<array.length;++i) {
            array[i] = (byte) (array[i-1]+1);
        }
        return array;
    }
    public static void serializeMultipleTimesAndRestoreConcurrently(final DataPacket entity, final int times) throws Exception {
        Thread[] ts = new Thread[7];
        final ArrayBlockingQueue<Throwable> errors = new ArrayBlockingQueue<>(10);
        for(int i=0;i<ts.length;++i) {
            Thread t = new Thread() {
                @Override
                public void run() {
                    try {
                        TestUtils.serializeMultipleTimesAndRestore(entity,times);
                    } catch (Exception e) {
                        try {
                            errors.put(e);
                        } catch (InterruptedException e1) {
                            System.exit(1);
                        }
                    }
                }
            };
            ts[i] = t;
        }
        for(Thread thread:ts) {
            thread.start();
        }
        for(Thread thread:ts) {
            thread.join();
        }
        if( ! errors.isEmpty()) {
            for(Throwable t:errors) {
                t.printStackTrace();
            }
            Assert.fail(errors.toString());
        }
    }
    public static void serializeMultipleTimesAndRestore(DataPacket entity) throws Exception {
        serializeMultipleTimesAndRestore(entity,10);
    }
    public static boolean equalsOrderFields(Object o1, Object o2) {
        if(o1==o2) {
            return true;
        }
        if(o1==null && o2!=null
        ||(o1!=null && o2==null)) {
            return false;
        }
        Class<?> c1 = o1.getClass();
        Class<?> c2 = o2.getClass();
        if(Collection.class.isAssignableFrom(c1)
        && Collection.class.isAssignableFrom(c2)) {
            Collection<?> col1 = (Collection<?>)o1;
            Collection<?> col2 = (Collection<?>)o2;
            if(col1.size()!=col2.size()) {
                return false;
            }
            Iterator<?> iter = ((Collection<?>)o1).iterator();
            Iterator<?> iter2 = ((Collection<?>)o2).iterator();
            while(iter.hasNext()) {
                Object elem1 = iter.next();
                Object elem2 = iter2.next();
                if( ! equalsOrderFields(elem1,elem2)) {
                    return false;
                }
            }
            return true;
        }
        if(Map.class.isAssignableFrom(c1)
        && Map.class.isAssignableFrom(c2)) {
            Map<?,?> map1 = (Map<?,?>)o1;
            Map<?,?> map2 = (Map<?,?>)o2;
            if(map1.size()!=map2.size()) {
                return false;
            }
            Set<?> keySet = map1.keySet();
            for(Object obj:keySet) {
                Object elem1 = map1.get(obj);
                Object elem2 = map2.get(obj);
                if( ! equalsOrderFields(elem1,elem2)) {
                    return false;
                }
            }
            return true;
        }
        if(c1!=c2) {
            return false;
        }
        if(c1 == Double.class) {
            return Double.compare(((Double)o1), ((Double)o2)) == 0;
        }
        if(c1 == double.class) {
            return Double.compare(((Double)o1), ((Double)o2)) == 0;
        }
        if(Number.class.isAssignableFrom(c1)) {
            return ((Number)o1).longValue() == ((Number)o2).longValue() 
                    && ((Number)o1).doubleValue() == ((Number)o2).doubleValue();
        }
        if(c1==String.class) {
            return ((String)o1).equals((String)o2);
        }
        if(c1==Character.class) {
            return ((Character)o1).equals((Character)o2);
        }
        if(c1==Boolean.class) {
            return ((Boolean)o1).booleanValue() == ((Boolean)o2).booleanValue();
        }
        if(java.util.Date.class.isAssignableFrom(c1)) {
            return ((java.util.Date)o1).getTime() == ((java.util.Date)o2).getTime();
        }
        if(c1.isArray()) {
            int len1 = Array.getLength(o1);
            int len2 = Array.getLength(o2);
            if(len1!=len2) {
                return false;
            }
            for(int i=0;i<len1;++i) {
                Object elem1 = Array.get(o1,i);
                Object elem2 = Array.get(o2, i);
                if( ! equalsOrderFields(elem1,elem2)) {
                    return false;
                }
            }
            return true;
        }
        while(c1!=Object.class) {
            Field[] fields = c1.getDeclaredFields();
            for(Field f:fields) {
                if((f.getModifiers() & Modifier.STATIC)!=0) {
                    continue;
                }
                if(f.getAnnotation(Order.class)==null) {
                    continue;
                }
                f.setAccessible(true);
                try {
                    Object val1 = f.get(o1);
                    Object val2 = f.get(o2);
                    if( ! equalsOrderFields(val1,val2)) {
                        System.err.println("not equals:"+f+" "+val1+" "+val2);
                        return false;
                    }
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new Error(f.getName());
                }
            }
            c1 = c1.getSuperclass();
        }
        return true;
    }
    
    public static long pow(long base ,int power) {
        long tmp = base;
        while(power-->1) {
            base = base*tmp;
            if(base<0) {
                throw new ArithmeticException();
            }
        }
        return base;
    }
    
    public static void compareByteArrayReversed(byte[] left, byte[] right) {
        StringBuilder error = new StringBuilder();
        if(left.length!=right.length) {
            error.append(String.format("left length is %d while right legth is %d", left.length, right.length));
        }else {
            for(int k=0;k<left.length;++k) {
                if((byte)left[k]!=(byte)right[right.length-k-1]) {
                    error.append(String.format("at %d value %d != %d\n", k,left[k], right[right.length-k-1]));
                }else {
                }
            }
        }
        if(error.length()>0) {
            Assert.fail(error.toString());
        }
    }
    
    public static void compareByteArray(byte[] left, byte[] right) {
        StringBuilder error = new StringBuilder();
        if(left.length!=right.length) {
            error.append(String.format("left length is %d while right legth is %d", left.length, right.length));
        }else {
            for(int k=0;k<left.length;++k) {
                if((byte)left[k]!=(byte)right[k]) {
                    error.append(String.format("at %d value %d != %d\n", k,left[k], right[k]));
                }else {
                }
            }
        }
        if(error.length()>0) {
            Assert.fail(error.toString());
        }
    }
}