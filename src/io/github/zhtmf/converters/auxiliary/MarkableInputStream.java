package io.github.zhtmf.converters.auxiliary;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * An input stream implementation that supports <tt>mark()</tt> and
 * <tt>reset()</tt> operations but does not maintain any internal buffer.
 * <p>
 * Due to the way how this library is used, the <tt>InputStream</tt> object
 * passed in by client code may be used again by client code thereafter. Classes
 * like {@link java.io.BufferedInputStream BufferedInputStream} effectively
 * prevent such use because of their internal buffering mechanics, which reads
 * more data than needed and those data cannot be put back to the original
 * stream.
 * 
 * @author dzh
 */
public class MarkableInputStream extends InputStream implements AutoCloseable{
    
    private static final int[] SHARED_EMPTY_BUFFER = new int[0];
    private static final int INITIAL_BUFFER_SIZE = 16;
    private InputStream in;
    private int[] buffer = SHARED_EMPTY_BUFFER;
    private int readPos = 0;
    private int fillPos = 0;
    private int resetPos = readPos;
    private int bytesProcessed;
    
    public static MarkableInputStream wrap(InputStream in) {
        if(in instanceof MarkableInputStream) {
            return new ResetCounterMarkableInputStream((MarkableInputStream) in);
        }
        return new MarkableInputStream(in);
    }
    
    protected MarkableInputStream() {
    }
    
    private MarkableInputStream(InputStream is) {
        if(is==null) {
            throw new NullPointerException();
        }
        this.in = is;
    }
    
    @Override
    public int read() throws IOException {
        if(in==null) {
            throw new IOException("Stream Closed");
        }
        if(readPos < fillPos) {
            ++bytesProcessed;
            return buffer[readPos++];
        }
        /*
         * may throw exception if size of the stream is exactly Integer.MAX_VALUE - 8
         * bytes. But this ensures the underlying stream is in a consistent state after
         * OutOfMemoryError is thrown
         */
        ensureCapacity();
        int b = in.read();
        if(b==-1) {
            return b;
        }
        ++bytesProcessed;
        ++fillPos;
        buffer[readPos++] = b;
        return b;
    }
    
    private void ensureCapacity() {
        int length = buffer.length;
        if(fillPos == length) {
            if(length==0) {
                length = INITIAL_BUFFER_SIZE;
            }else {
                length <<= 1;
            }
            if(length<=0) {
                //TODO: max array size
                length = Integer.MAX_VALUE - 8;
            }
            if(length == buffer.length) {
                throw new OutOfMemoryError();
            }
            buffer = Arrays.copyOf(buffer, length);
        }
    }
    
    @Override
    public void reset() throws IOException {
        bytesProcessed -= (readPos - resetPos);
        readPos = resetPos;
    }
    
    public int remaining() {
        return fillPos - readPos;
    }
    
    /**
     * TODO: javadoc/readlimit not respected
     */
    @Override
    public void mark(int readlimit) throws IllegalArgumentException, IllegalStateException {
        if(readlimit>buffer.length) {
            buffer = Arrays.copyOf(buffer, readlimit);
        }
        resetPos = readPos;
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
            //don't use value of bytesProcessed field directly
            //, as this object may wrap another ResetCounterMarkableInputStream
            this.delta = - delegate.actuallyProcessedBytes();
        }
        @Override
        int actuallyProcessedBytes() {
            return delegate.actuallyProcessedBytes() + delta;
        }
        
        public int read() throws IOException {
            return delegate.read();
        }
        public int hashCode() {
            return delegate.hashCode();
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
        public boolean equals(Object obj) {
            return delegate.equals(obj);
        }
    }
}
