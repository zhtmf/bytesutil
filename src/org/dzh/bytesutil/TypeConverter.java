package org.dzh.bytesutil;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;

public abstract class TypeConverter {
	
	public abstract void serialize(Object obj,Output output) throws IOException ;
	public abstract Object deserialize(Input input) throws IOException;
	
	public static interface Context{
		Class<?> getEntityClass();
		Class<?> getFieldClass();
		String getName();
		Charset getCharset();
		String getDatePattern();
		boolean isSigned();
		boolean isUnsigned();
		boolean isLittleEndian();
		boolean isBigEndian();
		int length();
	}
	
	/**
	 * Thin wrapper around underlying stream to provide basic serializing operations.
	 * <p>
	 * This class does not hold a buffer, all output methods directly write into the
	 * underlying output stream used for serializing. Exception will be thrown if
	 * client code tries to write more bytes than {@link Context#length()}
	 * indicates.
	 * <p>
	 * Use the {@link Context} object passed as another argument of the same method
	 * call to inspect other properties of corresponding class field (endianness,
	 * signedness etc.)
	 * 
	 * @author dzh
	 */
	public static interface Output extends Context{
		/**
		 * Write a byte to the underlying stream.<br>
		 * This method throws an exception if client code tries to write more bytes than 
		 * {@link Context#length()} indicates.
		 * @param n
		 */
		public void writeByte(byte n) throws IOException;
		/**
		 * Write an array of bytes to the underlying stream.<br>
		 * This method throws an exception if client code tries to write more bytes than 
		 * {@link Context#length()} indicates.
		 * @param array
		 */
		public void writeBytes(byte[] array) throws IOException;
		/**
		 * Write a short value with the same endianness indicated by
		 * {@link Context#isLittleEndian()} and {@link Context#isBigEndian()}.<br>
		 * If you want the integer be encoded otherwise, output it manually
		 * using {@link #writeByte(byte)}.
		 * <p>
		 * This method throws an exception if client code tries to write more bytes than
		 * {@link Context#length()} indicates.
		 * 
		 * @param array
		 */
		public void writeShort(short n) throws IOException;
		
		/**
		 * Write an int value with the same endianness indicated by
		 * {@link Context#isLittleEndian()} and {@link Context#isBigEndian()}.<br>
		 * If you want the integer be encoded otherwise, output it manually
		 * using {@link #writeByte(byte)}.
		 * <p>
		 * This method throws an exception if client code tries to write more bytes than
		 * {@link Context#length()} indicates.
		 * 
		 * @param array
		 */
		public void writeInt(int n) throws IOException;
		
		/**
		 * Write a long value with the same endianness indicated by
		 * {@link Context#isLittleEndian()} and {@link Context#isBigEndian()}.<br>
		 * If you want the integer be encoded otherwise, output it manually
		 * using {@link #writeByte(byte)}.
		 * <p>
		 * This method throws an exception if client code tries to write more bytes than
		 * {@link Context#length()} indicates.
		 * 
		 * @param array
		 */
		public void writeLong(long n) throws IOException;
		
		/**
		 * Returns number of bytes already written by client code
		 */
		public long written();
	}
	public static interface Input extends Context{
		public int readByte() throws IOException;
		public byte[] readBytes(int n) throws IOException;
		public short readShort() throws IOException;
		public int readInt() throws IOException;
		public long readLong() throws IOException;
		public int readUnsignedByte() throws IOException;
		public int readUnsignedShort() throws IOException;
		public long readUnsignedInt() throws IOException;
		public BigInteger readUnsignedLong() throws IOException;
		public long available();
	}
}
