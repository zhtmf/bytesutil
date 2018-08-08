package test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.dzh.bytesutil.ConversionException;
import org.dzh.bytesutil.converters.auxiliary.CharDecoder;
import org.junit.Assert;
import org.junit.Test;

public class TestCase10{
	
	private void test(String s,Charset cs) throws IOException {
		byte[] bs = s.getBytes(cs);
		StringBuilder sb = new StringBuilder();
		ByteArrayInputStream bais = new ByteArrayInputStream(bs);
		CharDecoder cd = new CharDecoder(cs);
		for(int i=0;i<s.length();++i) {
			sb.append(cd.decodeAndReset(bais));
		}
		Assert.assertEquals(sb.toString(),s);
	}
	
	@Test
	public void testLength() throws ConversionException, IOException {
		String s = "abcd阿中中中efghi阿jk阿lmn阿中中中中o阿阿阿pq阿阿rstu阿vwxyz阿0123456中中中789阿中中田!@#$%^&阿**()阿{}[]:\";'阿<阿>,./?|\\a!田a阿bc手田$，。（）*）*）#";
		test(s, Charset.forName("GB2312"));
		test(s, Charset.forName("UTF-8"));
		test(s, Charset.forName("UTF32"));
		test(s, Charset.forName("GBK"));
		test(s, Charset.forName("MS932"));
		test(s, Charset.forName("SHIFT-JIS"));
		test(s, Charset.forName("GB18030"));
		test(s, StandardCharsets.UTF_16BE);
		test(s, StandardCharsets.UTF_16BE);
		test(s, StandardCharsets.UTF_16BE);
		//TODO: UTF16 with BOM sucks
	}
	
	public static void main(String[] args) throws Exception {
		TestCase10 tc3 = new TestCase10();
		tc3.testLength();
	}
}
