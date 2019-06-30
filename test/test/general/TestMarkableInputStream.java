package test.general;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import examples.mysql.connector.datatypes.le.LEInt1;
import examples.mysql.connector.datatypes.le.LEIntHandler;
import examples.mysql.connector.datatypes.le.LEInteger;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Conditional;
import io.github.zhtmf.annotations.modifiers.LittleEndian;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.modifiers.Variant;
import io.github.zhtmf.annotations.types.BYTE;
import io.github.zhtmf.annotations.types.SHORT;
import io.github.zhtmf.converters.auxiliary.MarkableInputStream;
import io.github.zhtmf.converters.auxiliary.ModifierHandler;
import test.TestUtils;

public class TestMarkableInputStream {
    
    @LittleEndian
    @Unsigned
    public static class TestPacket extends DataPacket{
        public long capabilities;
        @Order(0)
        @BYTE
        public int header = 0x00;
        @Order(1)
        @Variant(LEIntHandler.class)
        public LEInteger affectedRows;
        @Order(2)
        @Variant(LEIntHandler.class)
        public LEInteger lastInsertId;
        @Order(3)
        @SHORT
        //SERVER_STATUS_flags_enum
        @Conditional(MyCondition.class)
        public int statusFlags;
        
        public static class MyCondition extends ModifierHandler<Boolean>{
            @Override
            public Boolean handleDeserialize0(String fieldName, Object entity, InputStream is) throws IOException {
                return Boolean.TRUE;
            }

            @Override
            public Boolean handleSerialize0(String fieldName, Object entity) {
                return Boolean.TRUE;
            }
        }
    }
    
    @Test
    public void testReset() throws Exception{
        TestPacket packet = new TestPacket();
        packet.affectedRows = new LEInt1();
        packet.lastInsertId = new LEInt1();
        packet.statusFlags = 30;
        TestUtils.serializeAndRestore(packet);
    }
    
    
    @Test
    public void test() throws IOException {
        byte[] array = TestUtils.randomArray(1024);
        try(MarkableInputStream mis = MarkableInputStream.wrap(new ByteArrayInputStream(array))){
            Assert.assertTrue(mis.markSupported());
            Assert.assertFalse(mis.marked());
            Assert.assertEquals(mis.available(),new ByteArrayInputStream(array).available());
            for(int i=0;i<10;++i) {
                Assert.assertEquals((byte)mis.read(), array[i]);
            }
        }
        try(MarkableInputStream mis = MarkableInputStream.wrap(new ByteArrayInputStream(array))){
            byte[] tmp = new byte[1024];
            mis.read(tmp);
            Assert.assertArrayEquals(tmp, array);
        }
        try(MarkableInputStream mis = MarkableInputStream.wrap(new ByteArrayInputStream(array))){
            byte[] tmp = new byte[1024];
            mis.read(tmp,0,512);
            Assert.assertArrayEquals(Arrays.copyOf(tmp, 512), Arrays.copyOf(array, 512));
        }
        try(MarkableInputStream mis = MarkableInputStream.wrap(new ByteArrayInputStream(array))){
            int count = 424;
            Assert.assertEquals(mis.skip(count),count);
            byte[] tmp = new byte[array.length-count];
            Assert.assertEquals(mis.read(tmp),array.length-count);
            Assert.assertArrayEquals(tmp, Arrays.copyOfRange(array, count, array.length));
        }
        try(MarkableInputStream mis = MarkableInputStream.wrap(new ByteArrayInputStream(array))){
            int count = array.length;
            Assert.assertEquals(mis.skip(count),count);
            Assert.assertEquals(mis.skip(3), 0);
        }
    }
    @Test
    public void test2() throws IOException {
        byte[] array = TestUtils.randomArray(1024);
        try(MarkableInputStream mis = MarkableInputStream.wrap(new ByteArrayInputStream(array))){
            Assert.assertTrue(mis.marked()==false);
            mis.mark(300);
            Assert.assertTrue(mis.marked());
            Assert.assertEquals(mis.remaining(),0);
            for(int i=0;i<300;++i) {
                Assert.assertEquals((byte)mis.read(), array[i]);
                Assert.assertEquals(mis.remaining(),0);
            }
            mis.reset();
            Assert.assertEquals(mis.remaining(),300);
            int i=0;
            for(;i<150;++i) {
                Assert.assertEquals((byte)mis.read(), array[i]);
                Assert.assertEquals(mis.remaining(),300-i-1);
            }
            Assert.assertEquals(mis.remaining(),300-i);
            mis.reset();
            Assert.assertEquals(mis.remaining(),300);
            int k = 0;
            for(;k<300;++k) {
                Assert.assertEquals((byte)mis.read(), array[k]);
            }
            mis.reset();
            Assert.assertEquals(mis.remaining(),300);
            for(int p=0;p<300;++p) {
                mis.read();
            }
            int pos = k;
            while(true) {
                int ret = mis.read();
                Assert.assertFalse(mis.marked());
                if(ret==-1) {
                    break;
                }
                Assert.assertEquals((byte)ret,array[pos++]);
            }
        }
    }
    @Test
    public void test4() throws IOException {
        try {
            MarkableInputStream.wrap(null);
            Assert.fail();
        } catch (NullPointerException e) {
        }
    }
    @Test
    public void test5() throws IOException {
        try {
            MarkableInputStream mis = MarkableInputStream.wrap(System.in);
            Assert.assertEquals(mis.skip(-1), 0);
            Assert.assertEquals(mis.skip(0), 0);
            mis.close();
            mis.read();
            Assert.fail();
        } catch (IllegalStateException e) {
        }
        try {
            MarkableInputStream mis = MarkableInputStream.wrap(System.in);
            mis.mark(-1);
            Assert.fail();
            mis.close();
        } catch (IllegalArgumentException e) {
        }
        try {
            MarkableInputStream mis = MarkableInputStream.wrap(System.in);
            mis.mark(0);
            Assert.fail();
            mis.close();
        } catch (IllegalArgumentException e) {
        }
    }
    @Test
    public void test6() throws IOException {
        byte[] array = TestUtils.randomArray(13);
        MarkableInputStream mis = MarkableInputStream.wrap(new ByteArrayInputStream(array));
        mis.mark(300);
        while(true) {
            int ret = mis.read();
            if(ret==-1) {
                break;
            }
        }
        mis.reset();
        Assert.assertEquals(mis.remaining(), 13);
        mis.close();
    }
    @Test
    public void test7() throws IOException {
        byte[] array = TestUtils.randomArray(124);
        MarkableInputStream mis = MarkableInputStream.wrap(new ByteArrayInputStream(array));
        mis.mark(4);
        Assert.assertEquals(mis.remaining(), 0);
        mis.reset();
        Assert.assertEquals(mis.remaining(), 0);
        mis.read();
        mis.reset();
        Assert.assertEquals(mis.remaining(), 1);
        mis.read();
        mis.read();
        mis.read();
        mis.read();
        mis.reset();
        Assert.assertEquals(mis.remaining(), 4);
        int pos = 0;
        while(true) {
            int ret = mis.read();
            if(ret==-1) {
                break;
            }
            Assert.assertEquals((byte)ret, array[pos++]);
        }
        mis.close();
    }
    @Test
    public void test8() throws IOException {
        byte[] array = TestUtils.randomArray(12);
        final int limit = 10;
        try(MarkableInputStream mis = MarkableInputStream.wrap(new ByteArrayInputStream(array))){
            mis.mark(limit);
            mis.reset();
            mis.mark(limit);
            mis.reset();
            mis.mark(limit);
            mis.reset();
            for(int i=0;i<limit;++i) {
                Assert.assertEquals(i+"",(byte)mis.read(), array[i]);
            }
            mis.reset();
            for(int i=0;i<limit-1;++i) {
                Assert.assertEquals(i+"",(byte)mis.read(), array[i]);
            }
            try {
                mis.mark(10);
                Assert.fail();
            } catch (Exception e) {
            }
        }
    }
    @Test
    public void test9() throws IOException {
        byte[] array = TestUtils.randomArray(12);
        final int limit = 3;
        try(MarkableInputStream mis = MarkableInputStream.wrap(new ByteArrayInputStream(array))){
            mis.mark(limit);
            mis.reset();
            mis.mark(limit);
            mis.reset();
            mis.mark(limit);
            mis.reset();
            for(int i=0;i<limit;++i) {
                Assert.assertEquals(i+"",(byte)mis.read(), array[i]);
            }
            try {
                mis.mark(10);
            } catch (Exception e) {
                Assert.fail();
            }
            int pos = 3;
            for(;;) {
                int ret = mis.read();
                if(ret==-1) {
                    break;
                }
                Assert.assertEquals((byte)ret, array[pos++]);
            }
        }
    }
}
