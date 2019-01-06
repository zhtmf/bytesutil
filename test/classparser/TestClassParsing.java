package classparser;

import java.io.InputStream;

import org.junit.Test;

import classparser.entities.JavaClass;

public class TestClassParsing {

	@Test
	public void testParsing() throws Exception {
		JavaClass clazz = new JavaClass();
		InputStream inputStream = TestClassParsing.class.getResourceAsStream("DataPacket.classfile");
		try {
			clazz.deserialize(inputStream);
		} catch (Exception e) {
			System.out.println(clazz);
			throw e;
		}
		System.out.println(clazz);
	}
}
