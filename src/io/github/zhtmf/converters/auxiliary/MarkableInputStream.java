package io.github.zhtmf.converters.auxiliary;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public final class MarkableInputStream extends InputStream implements AutoCloseable{
    
    private static final int[] SHARED_EMPTY_BUFFER = new int[0];
    private InputStream in;
    private int[] buffer = SHARED_EMPTY_BUFFER;
    private int readPos = 0;
    private int fillPos = 0;
    private int bytesProcessed;
    
    public MarkableInputStream(InputStream is) {
        if(is==null) {
            throw new NullPointerException();
        }
        this.in = is;
    }
    
    @Override
    public int read() throws IOException {
        checkClosed();
        if(readPos<fillPos) {
            ++bytesProcessed;
            return buffer[readPos++];
        }
        if(fillPos==buffer.length) {
            buffer = SHARED_EMPTY_BUFFER;
            readPos = 0;
            fillPos = 0;
            return read0();
        }
        int b = read0();
        if(b==-1) {
            return -1;
        }
        buffer[fillPos++] = b;
        ++readPos;
        return b;
    }
    
    private int read0() throws IOException {
        int b = in.read();
        if(b!=-1) {
            ++bytesProcessed;
        }
        return b;
    }
    
    @Override
    public void mark(int readlimit) throws IllegalArgumentException, IllegalStateException {
        checkClosed();
        if(readlimit<=0) {
            throw new IllegalArgumentException("invalid mark limit "+readlimit);
        }
        if(marked() && fillPos != readPos) {
            throw new IllegalStateException("already marked");
        }
        buffer = new int[readlimit];
    }

    @Override
    public void reset() throws IOException {
        if(marked()) {
            bytesProcessed -= readPos;
        }
        readPos = 0;
    }
    
    //TODO:
    public void drain() {
        buffer = Arrays.copyOf(buffer, fillPos);
    }
    
    public int remaining() {
        return marked() ? fillPos - readPos : 0;
    }
    public boolean marked() {
        return buffer != SHARED_EMPTY_BUFFER;
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
    
    private void checkClosed() {
        if(in==null)
            throw new IllegalStateException("stream closed");
    }
    int actuallyProcessedBytes() {
        return bytesProcessed;
    }
}
