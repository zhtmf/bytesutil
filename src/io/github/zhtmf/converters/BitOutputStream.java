package io.github.zhtmf.converters;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An delegating <code>OutputStream</code> which provides two additional methods
 * for progressively writing bits to the underlying stream.
 * 
 * @author dzh
 */
class BitOutputStream extends OutputStream{
    private OutputStream dest;
    private byte value;
    private int offset = -1;
    public BitOutputStream(OutputStream dest) throws NullPointerException {
        if(dest==null)
            throw new NullPointerException();
        this.dest = dest;
    }
    @Override
    public void write(int b) throws IOException,IllegalStateException {
        checkStatus();
        dest.write(b);
    }

    /**
     * Write right most {@link #num} bits of {@link #value} to an internal
     * <code>byte</code> value and if accumulatively 8 bits has been written the
     * byte will be flushed to the underlying stream.
     * <p>
     * An {@link IllegalStateException} will be thrown if <code>num</code> is larger
     * than "remaining" bits of the internal byte. The current byte should be fully
     * appended and flushed before start writing bits of another byte.
     * <p>
     * After calling this method and before the current byte is flushed, calling
     * other methods of this class like {@link #write(int)} will result in an
     * {@link IllegalStateException}.
     * 
     * @param value
     *            the byte value which right most <code>num</code> bits is to be
     *            written.
     * @param num
     *            number of right most bits of <code>value</code> to be processed.
     *            This value should be in the range of (0,8]. If this value equals
     *            8, calling this method is same as calling {@link #write(int)}.
     * @throws IOException
     *             if underlying stream throws an exception.
     * @throws IllegalArgumentException
     *             if <code>num</code> is out of range
     * @throws IllegalStateException
     *             if <code>num</code> is larger than remaining bits of current
     *             byte.
     */
    public void writeBits(byte val, int num) throws IOException, IllegalArgumentException, IllegalStateException{
        if(num<=0 || num>8) {
            throw new IllegalArgumentException();
        }
        if(offset==-1) {
            offset = 8;
        }
        else if(offset < num) {
            throw new IllegalStateException();
        }
        offset -= num;
        value |= val << offset;
        if(offset==0) {
            dest.write(value);
            value = 0;
            offset = -1;
        }
    }

    /**
     * Same as calling <code>writeBIT(1, 1)</code> if <code>b</code> is true or
     * <code>writeBIT(0, 1)</code> if <code>b</code> is false.
     * 
     * @param b
     * @throws IOException
     */
    public void writeBits(boolean b) throws IOException, IllegalArgumentException, IllegalStateException {
        writeBits(b ? (byte)1 : 0, 1);
    }
    
    @Override
    public void close() throws IOException,IllegalStateException {
        checkStatus();
        dest.close();
    }
    
    @Override
    public void flush() throws IOException,IllegalStateException {
        checkStatus();
        dest.flush();
    }
    
    private void checkStatus() throws IOException,IllegalStateException {
        if(offset==0) {
            dest.write(value);
            offset = -1;
        }
        else if(offset>0) {
            throw new IllegalStateException();
        }
    }
}
