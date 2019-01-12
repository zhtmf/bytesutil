package classparser;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import classparser.entities.JavaClass;

public class TestClassParsing {

	@Test
	public void testParsing() throws Exception {
		byte[] original = null;
		{
			InputStream inputStream = TestClassParsing.class.getResourceAsStream("DataPacket.classfile");
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
			InputStream inputStream = TestClassParsing.class.getResourceAsStream("DataPacket.classfile");
			JavaClass clazz = new JavaClass();
			try {
				clazz.deserialize(inputStream);
				System.out.println(clazz);
			} catch (Exception e) {
				System.out.println(clazz);
				throw e;
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			clazz.serialize(baos);
			deserialized = baos.toByteArray();
		}
		Assert.assertArrayEquals(original, deserialized);
	}
}
