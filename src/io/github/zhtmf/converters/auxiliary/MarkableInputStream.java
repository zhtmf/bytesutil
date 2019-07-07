package io.github.zhtmf.converters.auxiliary;

import java.io.IOException;
import java.io.InputStream;

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
    private InputStream in;
    private int[] buffer = SHARED_EMPTY_BUFFER;
    private int readPos = 0;
    private int fillPos = 0;
    private int markPos = 0;
    private boolean marked;
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
        int b = in.read();
        if(b==-1) {
            return b;
        }
        ++bytesProcessed;
        if(fillPos < markPos) {
            ++fillPos;
            buffer[readPos++] = b;
        }else {
            marked = false;
        }
        return b;
    }
    
    @Override
    public void reset() throws IOException {
        bytesProcessed -= readPos;
        readPos = 0;
    }
    
    //TODO:
    public void drain() throws IOException {
        markPos = fillPos;
    }
    
    public int remaining() {
        return marked() ? fillPos - readPos : 0;
    }
    public boolean marked() {
        return marked;
    }
    @Override
    public void mark(int readlimit) throws IllegalArgumentException, IllegalStateException {
        if(readlimit<=0)
            throw new IllegalArgumentException("readlimit should be greater than 0");
        if(marked() && fillPos<markPos) {
            throw new IllegalStateException("already marked");
        }
        if(readlimit>buffer.length) {
            buffer = new int[readlimit];
        }
        fillPos = 0;
        readPos = 0;
        markPos = readlimit;
        marked = true;
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
        public void drain() throws IOException {
            delegate.drain();
        }
        public int remaining() {
            return delegate.remaining();
        }
        public boolean marked() {
            return delegate.marked();
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
