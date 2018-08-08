package test;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;

import org.dzh.bytesutil.converters.auxiliary.MarkableStream;
import org.junit.Assert;
import org.junit.Test;

public class TestCase8{
	
	@Test
	public void testLength() throws Exception {
		String str = "1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz";
		try(MarkableStream bs = new MarkableStream(new ByteArrayInputStream(str.getBytes()))){
			StringBuilder sb = new StringBuilder();
			for(int i=0;i<str.length();++i) {
				sb.append((char)bs.read());
			}
			bs.close();
			Assert.assertTrue(str.contentEquals(sb));
		}
		try(MarkableStream bs = new MarkableStream(new ByteArrayInputStream(str.getBytes()))){
			StringBuilder sb = new StringBuilder();
			int marklimit = 23;
			bs.mark(marklimit);
			for(int k=0;k<4;++k) {
				bs.reset();
				sb.setLength(0);
				for(int i=0;i<marklimit;++i) {
					sb.append((char)bs.read());
				}
				Assert.assertTrue(str.substring(0, marklimit).contentEquals(sb));
			}
			for(int k=0;k<4;++k) {
				bs.reset();
				sb.setLength(0);
				for(int i=0;i<marklimit/2;++i) {
					sb.append((char)bs.read());
				}
				Assert.assertTrue(str.substring(0, marklimit/2).contentEquals(sb));
			}
			for(int i=0;i<str.length()-marklimit/2;++i) {
				sb.append((char)bs.read());
			}
			Assert.assertTrue(str.contentEquals(sb));
		}
		{
			BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(str.getBytes()));
			bis.mark(1000);
			int pre = 2;
			for(int k=0;k<pre;++k) {
				bis.read();
			}
			MarkableStream bs = new MarkableStream(bis);
			int marklimit = 10;
			bs.mark(marklimit);
			StringBuilder sb = new StringBuilder();
			for(int k=0;k<4;++k) {
				bs.reset();
				sb.setLength(0);
				for(int i=0;i<marklimit;++i) {
					sb.append((char)bs.read());
				}
				Assert.assertTrue(str.substring(pre, pre+marklimit).contentEquals(sb));
			}
			for(int i=0;i<str.length()-marklimit-pre;++i) {
				sb.append((char)bs.read());
			}
			bs.close();
			Assert.assertTrue(str.substring(pre).contentEquals(sb));
			bis.reset();
			sb.setLength(0);
			for(int i=0;i<str.length();++i) {
				sb.append((char)bis.read());
			}
			Assert.assertTrue(str.contentEquals(sb));
		}
		{
			ByteArrayInputStream bais = new ByteArrayInputStream(str.getBytes());
			MarkableStream bs = new MarkableStream(bais);
			bs.mark(10);
			StringBuilder sb = new StringBuilder();
			int marklimit = 10;
			for(int i=0;i<marklimit;++i) {
				bs.read();
			}
			bs.close();
			for(int i=0;i<str.length()-marklimit;++i) {
				sb.append((char)bais.read());
			}
			Assert.assertTrue(str.substring(marklimit).contentEquals(sb));
		}
	}
	
	public static void main(String[] args) throws Exception {
		TestCase8 tc3 = new TestCase8();
		tc3.testLength();
	}
}
