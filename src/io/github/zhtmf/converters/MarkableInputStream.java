package io.github.zhtmf.converters;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * An input stream implementation which supports <tt>mark</tt> and <tt>reset</tt>
 * operations but does not read more bytes than required.
 * <p>
 * Due to how this library is used, the <tt>InputStream</tt> passed in by client
 * codes may be used again thereafter. Classes like
 * {@link java.io.BufferedInputStream BufferedInputStream} prevent such use
 * because of internal buffering which reads more data than needed and those
 * data cannot be put back to the underlying stream.
 * 
 * @author dzh
 */
class MarkableInputStream extends InputStream implements AutoCloseable{
    
    private static final int[] SHARED_EMPTY_BUFFER = new int[0];
    private static final int INITIAL_BUFFER_SIZE = 16;
    private InputStream in;
    private int[] buffer = SHARED_EMPTY_BUFFER;
    private int readPos = 0;
    private int fillPos = 0;
    private int bytesProcessed;
    
    private byte bitMap;
    private int offset = -1;
    private static final byte[] masks = new byte[] {
            0x0,
            (byte) 0b00000001,
            (byte) 0b00000011,
            (byte) 0b00000111,
            (byte) 0b00001111,
            (byte) 0b00011111,
            (byte) 0b00111111,
            (byte) 0b01111111,
            (byte) 0b11111111,
    };
    
    static MarkableInputStream wrap(InputStream in) {
        if(in instanceof MarkableInputStream) {
            return new ResetCounterMarkableInputStream((MarkableInputStream) in);
        }
        return new MarkableInputStream(in);
    }
    
    protected MarkableInputStream() {
    }
    
    private MarkableInputStream(InputStream in) {
        if(in==null) {
            throw new NullPointerException();
        }
        this.in = in;
    }
    
    /**
     * Read <code>num</code> bits of current byte.
     * <p>
     * After calling this method once and before fully reading all bits of this
     * byte, calling {@link #read()} or related methods will result in an
     * {@link IllegalStateException}. While other methods like {@link #remaining()}
     * will act as if the byte has been fully read.
     * <p>
     * Fully reading current byte here means accumulatively reading 8 bits by
     * calling this method. After reading 8 bits this method can be called again to
     * start reading bits from another byte.
     * <p>
     * However one call to this method cannot read past the boundary of a byte, for
     * example if the first 7 bits of current byte has already been read, the next
     * call to this method can only read the remaining 1 bit but not a mixture of
     * bits from both the current byte and the next byte.
     * <p>
     * Bit values will be read from left to right. The result is a <code>byte</code>
     * whose right most bits being set to bits read from current byte.
     * 
     * @param num
     *            number of bits to read which should be in the range (0,8], reading
     *            8 bits will result in reading a <code>signed</code> byte which is
     *            consistent in binary format but different in value with what
     *            directly read from {@link #read()}.
     * @return a byte value whose right most <code>num</code> bits being set to bits
     *         read from the current byte.
     * @throws IOException
     *             if the underlying stream throws an exception.
     * @throws IllegalArgumentException
     *             If <code>num</code> is out of range or larger than number of
     *             remaining unread bits in the current byte.
     */
    public byte readBits(int num) throws IOException, IllegalArgumentException{
        if(offset==-1 || offset == 0) {
            bitMap = (byte) read();
            offset = 8;
        }
        if(num<0 || num>8) {
            throw new IllegalArgumentException("invalid number of bits to read");
        }
        if(offset<num) {
            throw new IllegalArgumentException("cannot read more than 8 bits from the same byte");
        }
        return (byte) (bitMap >> (offset -= num) & masks[num]);
    }
    
    @Override
    public int read() throws IOException {
        checkClosed();
        checkNotReadingBits();
        if(readPos < fillPos) {
            ++bytesProcessed;
            return buffer[readPos++];
        }
        int b = in.read();
        if(b==-1) {
            return b;
        }
        ++bytesProcessed;
        if(buffer == SHARED_EMPTY_BUFFER) {
            return b;
        }
        ensureCapacity();
        ++fillPos;
        buffer[readPos++] = b;
        return b;
    }
    
    @Override
    public void reset() throws IOException {
        checkClosed();
        checkNotReadingBits();
        bytesProcessed -= (readPos);
        readPos = 0;
    }
    
    int remaining() {
        return fillPos - readPos;
    }
    
    /**
     * Behavior of this method is different from general contract of
     * {@link InputStream#mark(int)}, as all bytes read after first call to this
     * method are remembered and never get invalidated by {@link #read() read}.
     * <p>
     * When this method is called again before bytes remembered are not fully
     * consumed after a call to {@link #reset() reset}, bytes before
     * {@link #readPos} are discarded
     * 
     * @param readlimit
     *            a hint to this method but not respected
     */
    @Override
    public void mark(int readlimit){
        checkNotReadingBits();
        if(readlimit<=0) {
            readlimit = INITIAL_BUFFER_SIZE;
        }
        if(readlimit>buffer.length) {
            buffer = Arrays.copyOf(buffer, readlimit);
        }
        System.arraycopy(buffer, readPos, buffer, 0, fillPos - readPos);
        fillPos -= readPos;
        readPos = 0;
    }
    
    public void close() throws IOException {
        checkNotReadingBits();
        in = null;//intended
    }
    
    public int available() throws IOException {return in.available();}
    public int read(byte[] b) throws IOException {return super.read(b);}
    public int read(byte[] b, int off, int len) throws IOException {return super.read(b, off, len);}
    public boolean markSupported() {return true;}
    @Override
    public long skip(long n) throws IOException {
        if(n<=0) {
            return 0;
        }
        long tmp = n;
        while(n-->0){
            int b = read();
            if(b==-1) {
                break;
            }
        }
        return tmp - n - 1;
    }
    
    /**
     * Return how many bytes have been read and actually processed from underlying
     * stream so far.
     * <p>
     * Normally it is same as how many bytes have been {@link #read()}, however when
     * the internal buffer is rewound by {@link #reset()}, return value of this
     * method is also deducted to reflect the effect as some bytes are "put back" to
     * the underlying stream and may be read again in later processing.
     * 
     * @return  how many bytes have been read and actually processed
     */
    int actuallyProcessedBytes() {
        return bytesProcessed;
    }
    
    private void checkNotReadingBits() {
        if(offset>0) {
            throw new IllegalStateException("this method should not be called while reading bits from a byte");
        }
    }
    
    private void ensureCapacity() {
        int length = buffer.length;
        if(fillPos == length) {
            buffer = Arrays.copyOf(buffer, length <<= 1);
        }
    }
    
    private void checkClosed() throws IOException {
        if(in==null) {
            throw new IOException("Stream Closed");
        }
    }
    
    /**
     * Delegate sublcass which delegates most of operations to wrapped
     * MarkableInputStream object but reports count of processed bytes after
     * creation of this class.
     * 
     * @author dzh
     */
    private static final class ResetCounterMarkableInputStream extends MarkableInputStream{
        private MarkableInputStream delegate;
        private int delta;
        public ResetCounterMarkableInputStream(MarkableInputStream delegate) {
            this.delegate = delegate;
            //don't refer to value of bytesProcessed field directly
            //as this object may wrap another ResetCounterMarkableInputStream
            this.delta = - delegate.actuallyProcessedBytes();
        }
        @Override
        int actuallyProcessedBytes() {
            return delegate.actuallyProcessedBytes() + delta;
        }
        
        @Override
        public byte readBits(int num) throws IOException {
            return delegate.readBits(num);
        }
        public int read() throws IOException {
            return delegate.read();
        }
        public void reset() throws IOException {
            delegate.reset();
        }
        public int remaining() {
            return delegate.remaining();
        }
        public void mark(int readlimit) throws IllegalArgumentException, IllegalStateException {
            delegate.mark(readlimit);
        }
        public void close() throws IOException {
            delegate.close();
        }
        public int available() throws IOException {
            return delegate.available();
        }
        public int read(byte[] b) throws IOException {
            return delegate.read(b);
        }
        public int read(byte[] b, int off, int len) throws IOException {
            return delegate.read(b, off, len);
        }
        public boolean markSupported() {
            return delegate.markSupported();
        }
        public long skip(long n) throws IOException {
            return delegate.skip(n);
        }
    }
}
