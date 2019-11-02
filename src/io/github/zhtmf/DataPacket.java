package io.github.zhtmf;

import java.io.InputStream;
import java.io.OutputStream;

import io.github.zhtmf.annotations.modifiers.Order;

/**
 * <p>
 * Main class of this library. Typical steps of using this library are:
 * <ol>
 * <li>Make a custom Java class inherit this class.</li>
 * <li>Use {@link Order} as well as other annotations under
 * {@code io.github.zhtmf.annotations} package to define the binary protocol
 * (scheme) that this class intends to implement.</li>
 * <li>Use the three methods inherited ({@link #serialize(OutputStream)
 * serialize}, {@link #deserialize(InputStream) deserialize} and
 * {@link #length() length}) to convert instances of this class to/from byte
 * streams.</li>
 * </ol>
 * <p>
 * The three methods in this class and this class itself is thread-safe. It is
 * declared as abstract only to remind users that it should be inherited but not
 * used alone.
 * 
 * @author dzh
 */
public abstract class DataPacket {
    
    /**
     * <p>
     * Serialize classes into specified output streams.
     * 
     * @param dest
     *            destination stream of serialization
     * @throws ConversionException
     *             If invalid input encountered during runtime.
     * @throws IllegalArgumentException
     *             If initial parsing of annotations on this class or fields
     *             failed, such exceptions should be checked during compile time
     *             but not caught and handled during runtime.
     * @throws NullPointerException
     *             if <tt>dest</tt> is null.
     */
    public void serialize(OutputStream dest) throws ConversionException, IllegalArgumentException{
        auxiliaryAccess.serialize(this, dest);
    }
    
    /**
     * <p>
     * Deserialize classes from input streams.
     * 
     * @param src
     *            the input stream
     * @throws ConversionException
     *             If invalid input encountered during runtime.
     * @throws IllegalArgumentException
     *             If initial parsing of annotations on this class or fields
     *             failed, such exceptions should be checked during compile time
     *             but not caught and handled during runtime.
     * @throws NullPointerException
     *             if <tt>src</tt> is null.
     */
    public void deserialize(InputStream src) throws ConversionException, IllegalArgumentException {
        auxiliaryAccess.deserialize(this, src);
    }
    
    /**
     * Calculate the length in bytes of this object as if it was serialized to an
     * output stream.
     * <p>
     * This is <b>NOT</b> a constant time operation as the actual length should and
     * can only be calculated at runtime. But this library does not implement this
     * by actually doing any serialization.
     * 
     * @throws IllegalArgumentException
     *             If initial parsing of annotations on this class or fields failed.
     *             We use {@code IllegalArgumentException} to achieve backward
     *             compatibility though some errors may be better expressed by a
     *             {@code ConversionException}.
     * 
     * @return length in bytes
     */
    public int length() throws IllegalArgumentException{
        return auxiliaryAccess.length(this);
    }
    
    public static interface AuxiliaryAccess{
        int length(Object self) throws IllegalArgumentException;
        void serialize(Object self,OutputStream dest)throws ConversionException, IllegalArgumentException;
        void deserialize(Object self,InputStream src) throws ConversionException, IllegalArgumentException;
    }
    private static AuxiliaryAccess auxiliaryAccess;
    public static final void setAuxiliaryAccess(AuxiliaryAccess auxiliaryAccess) {
        if(DataPacket.auxiliaryAccess==null) {
            DataPacket.auxiliaryAccess = auxiliaryAccess;
        }
    }
    
    static {
        try {
            Class.forName("io.github.zhtmf.converters.ClassInfo");
        } catch (ClassNotFoundException e) {
        }
    }
}