package org.dzh.bytesutil;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;

import org.dzh.bytesutil.annotations.modifiers.BigEndian;
import org.dzh.bytesutil.annotations.modifiers.CHARSET;
import org.dzh.bytesutil.annotations.modifiers.DatePattern;
import org.dzh.bytesutil.annotations.modifiers.LittleEndian;
import org.dzh.bytesutil.annotations.modifiers.Signed;
import org.dzh.bytesutil.annotations.modifiers.Unsigned;
import org.dzh.bytesutil.annotations.types.UserDefined;

/**
 * Interface for custom converters used in processing fields marked with
 * {@link UserDefined}
 * 
 * @author dzh
 */
public abstract class TypeConverter<T> {
    
    /**
     * Serializing a custom object to output stream.
     * <p>
     * Users are expected to directly write to the {@link Output} argument rather
     * than return serialized bytes as an array. This is to avoid copying data into
     * intermediate buffer and using a wrapper class to provide basic
     * support.
     * <p>
     * There is no buffer for the output, any output is done immediately and
     * directly to the underlying stream.
     * <p>
     * Users should write exactly the number of bytes as specified by
     * {@link UserDefined#length()} or separate {@link Length}, otherwise an
     * exception will be thrown during or after this method returns.
     * 
     * @param obj
     *            the object to serialize, namely value of the related field.
     * @param output
     *            wrapper of the underlying output stream.
     * 
     * @throws IOException
     *             if an error occurred during output or users attempts to write
     *             more bytes than expected.
     */
    public abstract void serialize(T obj,Output output) throws IOException;
    
    /**
     * Deserializing bytes from stream as a custom object.
     * <p>
     * Users are expected to directly read sequentially from the {@link Input}
     * arguments rather than an intermediate buffer. This is to avoid copying data
     * into intermediate buffer and using a wrapper class to provide basic support.
     * <p>
     * There is no buffer for the input, any read operation directly and immediately
     * affects the underlying stream.
     * <p>
     * Users should only read exactly the number of bytes as specified by
     * {@link UserDefined#length()} or separate {@link Length}, otherwise an
     * exception will be thrown during or after this method returns.
     * 
     * @param input
     *            wrapper of the underlying input stream.
     * @return custom object, should not be null.
     * @throws IOException
     *             if an error occurred during output or users attempts to read more
     *             bytes than expected.
     */
    public abstract T deserialize(Input input) throws IOException;
    
    /**
     * Thin wrapper around underlying stream to provide basic serializing support
     * and getters for properties of related class field.
     * <p>
     * This class does not hold a buffer, all output methods directly write into the
     * underlying output stream. Exception will be thrown if user attempts to write
     * more bytes as specified by {@link UserDefined#length()} or separate
     * {@link Length}.
     * 
     * @author dzh
     */
    public static interface Output extends Context{
        /**
         * Write a byte to the underlying stream.<br>
         * This method throws an exception if user attempts to write more bytes than 
         * {@link Context#length()} indicates.
         * @param n
         */
        public void writeByte(byte n) throws IOException;
        /**
         * Write an array of bytes to the underlying stream.<br>
         * This method throws an exception if user attempts to write more bytes than 
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
         * This method throws an exception if user attempts to write more bytes than
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
         * This method throws an exception if user attempts to write more bytes than
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
         * This method throws an exception if user attempts to write more bytes than
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
    
    /**
     * Thin wrapper around underlying stream to provide basic deserializing support
     * and getters for properties of related class field.
     * <p>
     * This class does not hold a buffer, all input methods directly affects the
     * underlying output stream. Exception will be thrown if user attempts to read
     * more bytes as specified by {@link UserDefined#length()} or separate
     * {@link Length}.
     * 
     * @author dzh
     */
    public static interface Input extends Context{
        /**
         * Read a signed byte value
         * @return    a signed byte value
         * @throws IOException
         */
        public byte readByte() throws IOException;
        /**
         * Read {@code n} number of bytes as an array
         * @param n    number of bytes to read
         * @return
         * @throws IOException
         */
        public byte[] readBytes(int n) throws IOException;
        /**
         * Read a signed short value
         * @return    a signed short value
         * @throws IOException
         */
        public short readShort() throws IOException;
        /**
         * Read a signed int value
         * @return    a signed int value
         * @throws IOException
         */
        public int readInt() throws IOException;
        /**
         * Read a signed long value
         * @return    a signed long value
         * @throws IOException
         */
        public long readLong() throws IOException;
        /**
         * Read a single byte and interpret it as an unsigned byte value
         * @return    an unsigned byte value
         * @throws IOException
         */
        public int readUnsignedByte() throws IOException;
        /**
         * Read 2 bytes and interpret it as an unsigned short value
         * @return    an unsigned short value
         * @throws IOException
         */
        public int readUnsignedShort() throws IOException;
        /**
         * Read 4 bytes and interpret it as an unsigned int value
         * @return    an unsigned int value
         * @throws IOException
         */
        public long readUnsignedInt() throws IOException;
        /**
         * Read 8 bytes and interpret it as an unsigned long value
         * @return    an unsigned long value
         * @throws IOException
         */
        public BigInteger readUnsignedLong() throws IOException;

        /**
         * How many bytes users are permitted to read. This value changes as other input
         * methods are called.
         * 
         * @return number of bytes
         * @throws IOException
         */
        public long available();
    }
    
    private static interface Context{
        
        /**
         * Class of related entity object.
         * @return    class of entity object
         */
        Class<?> getEntityClass();
        
        /**
         * Type of related class field.
         * @return    type of class field
         */
        Class<?> getFieldClass();
        
        /**
         * Name of related class field.
         * @return    name of class field
         */
        String getName();
        
        /**
         * Charset as defined by {@link CHARSET} annotation.
         * 
         * @return
         */
        Charset getCharset();

        /**
         * Date pattern string as defined by {@link DatePattern} annotation. Null if
         * undefined.
         * 
         * @return
         */
        String getDatePattern();
        
        /**
         * Signedness as defined by {@link Signed}
         * 
         * @return
         */
        boolean isSigned();
        
        /**
         * Unsignedness as defined by {@link Unsigned}
         * 
         * @return
         */
        boolean isUnsigned();
        
        /**
         * Endianness as defined by {@link LittleEndian}
         * 
         * @return
         */
        boolean isLittleEndian();
        
        /**
         * Endianness as defined by {@link BigEndian}
         * 
         * @return
         */
        boolean isBigEndian();
        
        /**
         * Number of bytes as specified by {@link UserDefined#length()} or separate
         * {@link Length}.<br/>
         * This value is constant and serves as a limit for input/output operations.
         * 
         * @return    number of bytes
         */
        int length();
    }
}
