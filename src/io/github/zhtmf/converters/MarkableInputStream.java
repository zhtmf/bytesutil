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
    
    @Override
    public int read() throws IOException {
        checkClosed();
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
