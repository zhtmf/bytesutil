package examples.javaclass;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import examples.javaclass.entities.JavaClass;
import io.github.zhtmf.converters.TestUtils;

public class TestClassParsing {

    @Test
    public void testParsing2() throws Exception {
        testParsing("TestLongFieldClass.classfile");
    }

    @Test
    public void testParsing1() throws Exception {
        testParsing("DataPacket.classfile");
    }

    public void testParsing(String name) throws Exception {
        byte[] original = null;
        {
            InputStream inputStream = getInputStream(name);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int read = -1;
            while((read = inputStream.read(buffer))!=-1) {
                baos.write(buffer,0,read);
            }
            original = baos.toByteArray();
        }
        byte[] deserialized = null;
        {
            InputStream inputStream = getInputStream(name);
            JavaClass clazz = new JavaClass();
            try {
                clazz.deserialize(inputStream);
                System.out.println(clazz);
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    System.out.println(clazz);
                } catch (Exception e1) {
                    System.out.println("print error"+e1.getMessage());
                }
                Assert.fail();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            clazz.serialize(baos);
            deserialized = baos.toByteArray();
            
            TestUtils.serializeMultipleTimesAndRestore(clazz);
            
            Assert.assertEquals(original.length, clazz.length());
        }
        Assert.assertArrayEquals(original, deserialized);
    }
    
    private InputStream getInputStream(String name) throws FileNotFoundException {
        File f = new File(name);
        if( ! f.exists()) {
            String path = "test/" + this.getClass().getPackage().getName().replace('.', '/') + "/" + name;
            f = new File(path);
        }
        return new FileInputStream(f);
    }
}
