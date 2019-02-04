package org.dzh.bytesutil.converters.auxiliary;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.dzh.bytesutil.converters.auxiliary.exceptions.UnsatisfiedIOException;

public class StreamUtils {
	
	public static void writeBYTE(OutputStream os, byte value) throws IOException {
		os.write(value);
	}
	public static void writeSHORT(OutputStream os, int value, boolean bigendian) throws IOException {
		if(bigendian) {
			os.write((byte)(value>>8 & 0xFF));
			os.write((byte)(value & 0xFF));
		}else {
			os.write((byte)(value & 0xFF));
			os.write((byte)(value>>8 & 0xFF));
		}
	}
	public static void writeInt(OutputStream os, int value, boolean bigendian) throws IOException {
		if(bigendian) {
			os.write((byte)(value>>24 & 0xFF));
			os.write((byte)(value>>16 & 0xFF));
			os.write((byte)(value>>8 & 0xFF));
			os.write((byte)(value & 0xFF));
		}else {
			os.write((byte)(value & 0xFF));
			os.write((byte)(value>>8 & 0xFF));
			os.write((byte)(value>>16 & 0xFF));
			os.write((byte)(value>>24 & 0xFF));
		}
	}
	public static void writeBCD(OutputStream os, int[] value) throws IOException {
		byte tmp = 0;
		for(int i=0;i<value.length;i+=2) {
			tmp |= ((byte)value[i] << 4);
			tmp |= ((byte)value[i+1]);
			os.write(tmp);
			tmp = 0;
		}
	}
	
	public static void writeBytes(OutputStream os, int[] raw) throws IOException{
		for(int i : raw) {
			os.write(i);
		}
	}
	
	public static void writeBytes(OutputStream os, byte[] raw) throws IOException{
		os.write(raw);
	}
	
	public static void writeIntegerOfType(OutputStream os, DataType type, int val, boolean bigEndian) throws IOException{
		String error;
		if((error = type.checkRange(val, true))!=null) {
			throw new UnsatisfiedIOException(error)
				.withSiteAndOrdinal(StreamUtils.class, 1);
		}
		switch(type) {
		case BYTE:
			StreamUtils.writeBYTE(os, (byte)val);
			break;
		case SHORT:
			StreamUtils.writeSHORT(os, (short)val, bigEndian);
			break;
		case INT:
			StreamUtils.writeInt(os, val, bigEndian);
			break;
		default:throw new Error("cannot happen");
		}
	}
	
	//---------------------------------
	
	public static int readUnsignedByte(InputStream is) throws IOException{
		return read(is);
	}
	public static int readSignedByte(InputStream is) throws IOException{
		return (byte)readUnsignedByte(is);
	}
	public static int readSignedShort(InputStream is, boolean bigendian) throws IOException{
		return (short)readUnsignedShort(is,bigendian);
	}
	public static int readUnsignedShort(InputStream is, boolean bigendian) throws IOException{
		int b1 = read(is);
		int b2 = read(is);
		return bigendian ? ((b1<<8) | b2) : ((b2<<8) | b1);
	}
	public static long readSignedInt(InputStream is, boolean bigendian) throws IOException{
		return (int)readUnsignedInt(is,bigendian);
	}
	public static long readUnsignedInt(InputStream is, boolean bigendian) throws IOException{
		int ret = 0;
		if(bigendian) {
			ret |= (read(is)<<24);
			ret |= (read(is) << 16);
			ret |= (read(is) << 8);
			ret |= read(is);
		}else {
			ret |= read(is);
			ret |= (read(is) << 8);
			ret |= (read(is) << 16);
			ret |= (read(is)<<24);
		}
		return (((long)ret) & 0xFFFFFFFFL);
	}
	public static final String readStringBCD(InputStream is, int len) throws IOException {
		byte[] arr = readBytes(is, len);
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<arr.length;++i) {
			sb.append((char)(((arr[i] >> 4) & 0x0F)+'0'));
			sb.append((char)((arr[i] & 0x0F)+'0'));
		}
		return sb.toString();
	}
	public static final long readIntegerBCD(InputStream is, int len) throws IOException {
		byte[] arr = readBytes(is, len);
		long ret = 0;
		for(int i=0;i<arr.length;++i) {
			ret = ret * 10 + ((arr[i] >> 4) & 0x0F);
			ret = ret * 10 + (arr[i] & 0x0F);
			if(ret<0) {
				throw new UnsatisfiedIOException("BCD value overflows Java long type range")
						.withSiteAndOrdinal(StreamUtils.class, 2);
			}
		}
		return ret;
	}
	public static byte[] readBytes(InputStream is, int numBytes) throws IOException{
		byte[] arr = new byte[numBytes];
		int len = 0;
		while(len<arr.length) {
			int l = is.read(arr,len,arr.length-len);
			if(l==-1) {
				throw new EOFException();
			}
			len += l;
		}
		return arr;
	}
	
	public static int readIntegerOfType(InputStream src, DataType type, boolean bigEndian) throws IOException{
		int length = 0;
		switch(type) {
		case BYTE:
			length = StreamUtils.readUnsignedByte(src);
			break;
		case SHORT:
			length = StreamUtils.readUnsignedShort(src, bigEndian);
			break;
		case INT:
			long _length = StreamUtils.readUnsignedInt(src, bigEndian);
			String error;
			//array or list length in Java cannot exceed signed 32-bit integer
			if((error = DataType.INT.checkRange(_length, false))!=null) {
				throw new UnsatisfiedIOException(error)
					.withSiteAndOrdinal(StreamUtils.class, 3);
			}
			length = (int)_length;
			break;
		default:throw new Error("cannot happen");
		}
		return length;
	}
	
	private static int read(InputStream bis) throws IOException {
		int b = bis.read();
		if(b==-1) {
			throw new EOFException(b+"");
		}
		return b;
	}
}