package org.dzh.bytesutil.converters.auxiliary;

import java.io.InputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;

public class StreamUtils {
	
	public static void writeBYTE(OutputStream os, byte value) throws IOException {
		os.write(value);
	}
	public static void writeSHORT(OutputStream os, short value, boolean bigendian) throws IOException {
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
		if(value.length %2 !=0) {
			throw new IOException("illegal value array length for BCD:"+value.length+", as value array length is "+value.length);
		}
		byte tmp = 0;
		for(int i=0;i<value.length;i+=2) {
			tmp |= ((byte)value[i] << 4);
			tmp |= ((byte)value[i+1]);
			os.write(tmp);
			tmp = 0;
		}
	}
	public static void writeBCD(OutputStream os, int num, int digits) throws IOException {
		int[] values = new int[digits];
		int ptr = values.length-1;
		while(ptr>=0 && num>0) {
			values[ptr--] = num % 10;
			num /= 10;
		}
		if(num>0) {
			throw new IOException("BCD digits "+digits+" not equal to that of decimal number:"+num);
		}
		writeBCD(os, values);
	}
	
	public static void writeBytes(OutputStream os, int[] raw) throws IOException{
		for(int i : raw) {
			if(i<Byte.MIN_VALUE || i>Byte.MAX_VALUE) {
				throw new IOException("int value "+i+" cannot be written as a byte");
			}
			os.write(i);
		}
	}
	
	public static void writeBytes(OutputStream os, byte[] raw) throws IOException{
		os.write(raw);
	}
	
	public static void writeIntegerOfType(OutputStream os, DataType type, int val, boolean bigEndian) throws IOException{
		switch(type) {
		case BYTE:
			if(val>255) {
				throw new IllegalArgumentException("byte value overflow");
			}
			StreamUtils.writeBYTE(os, (byte)val);
			break;
		case SHORT:
			if(val>Character.MAX_VALUE) {
				throw new IllegalArgumentException("short value overflow");
			}
			StreamUtils.writeSHORT(os, (short)val, bigEndian);
			break;
		case INT:
			StreamUtils.writeInt(os, val, bigEndian);
			break;
		default:
			throw new IllegalArgumentException("data type "+type+" unsupported for length type");
		}
	}
	
	//---------------------------------
	
	public static int readBYTE(InputStream is) throws IOException{
		return read(is);
	}
	public static int readSHORT(InputStream is, boolean bigendian) throws IOException{
		int b1 = read(is);
		int b2 = read(is);
		return bigendian ? ((b1<<8) | b2) : ((b2<<8) | b1);
	}
	public static long readInt(InputStream is, boolean bigendian) throws IOException{
		long ret = 0;
		if(bigendian) {
			ret |= ((long)read(is))<<24;
			ret |= ((long)read(is) << 16);
			ret |= ((long)read(is) << 8);
			ret |= (long)read(is);
		}else {
			ret |= (long)read(is);
			ret |= ((long)read(is) << 8);
			ret |= ((long)read(is) << 16);
			ret |= ((long)read(is))<<24;
		}
		return ret;
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
				throw new IOException("BCD value overflow");
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
			length = StreamUtils.readBYTE(src);
			break;
		case SHORT:
			length = StreamUtils.readSHORT(src, bigEndian);
			break;
		case INT:
			long _length = StreamUtils.readInt(src, bigEndian);
			if(_length>Integer.MAX_VALUE) {
				throw new IllegalArgumentException("unsigned int value encountered");
			}
			length = (int)_length;
			break;
		default:
			throw new IllegalArgumentException("data type "+type+" unsupported for length type");
		}
		return length;
	}
	
	private static int read(InputStream bis) throws IOException {
		int b = bis.read();
		if(b==-1) {
			throw new EOFException();
		}
		return b;
	}
}
