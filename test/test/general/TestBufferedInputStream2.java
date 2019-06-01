package test.general;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import test.TestUtils;

public class TestBufferedInputStream2 {
    @Test
    public void test() throws IOException {
        byte[] array = TestUtils.randomArray(1024);
        try(BufferedInputStream mis = new BufferedInputStream(new ByteArrayInputStream(array))){
            Assert.assertTrue(mis.markSupported());
            Assert.assertEquals(mis.available(),new ByteArrayInputStream(array).available());
            for(int i=0;i<10;++i) {
                Assert.assertEquals(mis.read(), array[i]);
            }
        }
        try(BufferedInputStream mis = new BufferedInputStream(new ByteArrayInputStream(array))){
            byte[] tmp = new byte[1024];
            mis.read(tmp);
            Assert.assertArrayEquals(tmp, array);
        }
        try(BufferedInputStream mis = new BufferedInputStream(new ByteArrayInputStream(array))){
            byte[] tmp = new byte[1024];
            mis.read(tmp,0,512);
            Assert.assertArrayEquals(Arrays.copyOf(tmp, 512), Arrays.copyOf(array, 512));
        }
        try(BufferedInputStream mis = new BufferedInputStream(new ByteArrayInputStream(array))){
            int count = 424;
            Assert.assertEquals(mis.skip(count),count);
            byte[] tmp = new byte[array.length-count];
            Assert.assertEquals(mis.read(tmp),array.length-count);
            Assert.assertArrayEquals(tmp, Arrays.copyOfRange(array, count, array.length));
        }
        try(BufferedInputStream mis = new BufferedInputStream(new ByteArrayInputStream(array))){
            int count = array.length;
            Assert.assertEquals(mis.skip(count),count);
            Assert.assertEquals(mis.skip(3), 0);
        }
    }
    @Test
    public void test2() throws IOException {
        byte[] array = TestUtils.randomArray(1024);
        try(BufferedInputStream mis = new BufferedInputStream(new ByteArrayInputStream(array))){
            mis.mark(300);
            for(int i=0;i<300;++i) {
                try {
                    Assert.assertEquals(mis.read(), array[i]);
                } catch (Error e) {
                    System.out.println(i);
                }
            }
            mis.reset();
            int i=0;
            for(;i<150;++i) {
                Assert.assertEquals(mis.read(), array[i]);
            }
            mis.mark(444);
            mis.reset();
            int k = 0;
            for(;k<444;++k) {
                Assert.assertEquals(mis.read(), array[k+i]);
            }
            mis.reset();
            for(int p=0;p<444;++p) {
                mis.read();
            }
            int pos = k+i;
            while(true) {
                int ret = mis.read();
                if(ret==-1) {
                    break;
                }
                Assert.assertEquals(ret,array[pos++]);
            }
        }
    }
    @Test
    public void test3() throws IOException {
        byte[] array = TestUtils.randomArray(1024);
        try(BufferedInputStream mis = new BufferedInputStream(new ByteArrayInputStream(array))){
            mis.mark(300);
            for(int i=0;i<400;++i) {
                Assert.assertEquals(mis.read(), array[i]);
            }
            try {
                mis.reset();
                Assert.fail();
            } catch (IOException e) {
            }
        }
    }
    @SuppressWarnings("resource")
    @Test
    public void test4() throws IOException {
        try {
            new BufferedInputStream(null);
            Assert.fail();
        } catch (NullPointerException e) {
        }
    }
    @Test
    public void test5() throws IOException {
        try {
            BufferedInputStream mis = new BufferedInputStream(System.in);
            Assert.assertEquals(mis.skip(-1), 0);
            Assert.assertEquals(mis.skip(0), 0);
            mis.close();
            mis.read();
            Assert.fail();
        } catch (IllegalStateException e) {
        }
        try {
            BufferedInputStream mis = new BufferedInputStream(System.in);
            mis.mark(-1);
            Assert.fail();
            mis.close();
        } catch (IllegalArgumentException e) {
        }
        try {
            BufferedInputStream mis = new BufferedInputStream(System.in);
            mis.mark(0);
            Assert.fail();
            mis.close();
        } catch (IllegalArgumentException e) {
        }
    }
    @Test
    public void test6() throws IOException {
        byte[] array = TestUtils.randomArray(13);
        BufferedInputStream mis = new BufferedInputStream(new ByteArrayInputStream(array));
        mis.mark(300);
        while(true) {
            int ret = mis.read();
            if(ret==-1) {
                break;
            }
        }
        mis.reset();
        mis.close();
    }
    @Test
    public void test7() throws IOException {
        byte[] array = TestUtils.randomArray(124);
        BufferedInputStream mis = new BufferedInputStream(new ByteArrayInputStream(array));
        mis.mark(4);
        mis.reset();
        mis.read();
        mis.reset();
        mis.read();
        mis.read();
        mis.read();
        mis.read();
        mis.reset();
        mis.mark(3);
        mis.read();
        mis.read();
        mis.read();
        mis.read();
        int pos = 5;
        while(true) {
            int ret = mis.read();
            if(ret==-1) {
                break;
            }
            Assert.assertEquals(ret, array[pos++]);
        }
        mis.close();
    }
    @Test
    public void test8() throws IOException {
        byte[] array = TestUtils.randomArray(12);
        BufferedInputStream mis = new BufferedInputStream(new ByteArrayInputStream(array));
        mis.mark(10);
        mis.reset();
        mis.mark(10);
        mis.reset();
        mis.mark(10);
        mis.reset();
        Assert.assertEquals(mis.read(), array[0]);
        Assert.assertEquals(mis.read(), array[1]);
        Assert.assertEquals(mis.read(), array[2]);
        mis.mark(10);
        Assert.assertEquals(mis.read(), array[0]);
        Assert.assertEquals(mis.read(), array[1]);
        Assert.assertEquals(mis.read(), array[2]);
        mis.reset();
        Assert.assertEquals(mis.read(), array[0]);
        Assert.assertEquals(mis.read(), array[1]);
        Assert.assertEquals(mis.read(), array[2]);
        Assert.assertEquals(mis.read(), array[3]);
        Assert.assertEquals(mis.read(), array[4]);
        Assert.assertEquals(mis.read(), array[5]);
        Assert.assertEquals(mis.read(), array[6]);
        Assert.assertEquals(mis.read(), array[7]);
        Assert.assertEquals(mis.read(), array[8]);
        Assert.assertEquals(mis.read(), array[9]);
        Assert.assertEquals(mis.read(), array[10]);
        Assert.assertEquals(mis.read(), array[11]);
        mis.close();
    }
}
