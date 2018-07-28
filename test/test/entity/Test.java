package test.entity;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.dzh.bytesutil.converters.auxiliary.StreamUtils;

public class Test {
	public static void main(String[] args) throws IOException {
		{
			int val = 150;
			byte b = (byte) val;
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			StreamUtils.writeBYTE(os, b);
			BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(os.toByteArray()));
			System.out.println(StreamUtils.readUnsignedByte(bis)==val);
		}
		{
			int val = -3;
			byte b = (byte) val;
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			StreamUtils.writeBYTE(os, b);
			BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(os.toByteArray()));
			System.out.println((byte)StreamUtils.readUnsignedByte(bis)==val);
		}
		
		{
			int val = Short.MAX_VALUE+333;
			short b = (short) val;
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			StreamUtils.writeSHORT(os, b, true);
			BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(os.toByteArray()));
			System.out.println(StreamUtils.readUnsignedShort(bis, true)==val);
		}
		{
			int val = Short.MAX_VALUE+333;
			short b = (short) val;
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			StreamUtils.writeSHORT(os, b, false);
			BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(os.toByteArray()));
			System.out.println((short)StreamUtils.readUnsignedShort(bis, false)==(short)val);
		}
		
		{
			long val = (long)Integer.MAX_VALUE+(long)133333;
			int b = (int) val;
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			StreamUtils.writeInt(os, b, true);
			BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(os.toByteArray()));
			System.out.println(StreamUtils.readUnsignedInt(bis, true)==val);
		}
		{
			long val = (long)Integer.MAX_VALUE+(long)133333;
			int b = (int) val;
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			StreamUtils.writeInt(os, b, false);
			BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(os.toByteArray()));
			System.out.println(StreamUtils.readUnsignedInt(bis, false)==val);
		}
		
		{
			long val = (long)Integer.MAX_VALUE+(long)133333;
			int b = (int) val;
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			StreamUtils.writeInt(os, b, true);
			BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(os.toByteArray()));
			System.out.println(StreamUtils.readUnsignedInt(bis, true)==val);
		}
		{
			long val = (long)Integer.MAX_VALUE+(long)133333;
			int b = (int) val;
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			StreamUtils.writeInt(os, b, false);
			BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(os.toByteArray()));
			System.out.println(StreamUtils.readUnsignedInt(bis, false)==val);
		}
	}
}
