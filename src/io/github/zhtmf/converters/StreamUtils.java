package io.github.zhtmf.converters;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.nio.charset.Charset;
import java.util.Arrays;

import io.github.zhtmf.ConversionException;
import io.github.zhtmf.annotations.types.BCD;
import io.github.zhtmf.converters.auxiliary.DataType;

class StreamUtils {
    
    private StreamUtils() {}
    
    public static void writeBYTE(OutputStream os, byte value) throws IOException {
        os.write(value);
    }
    public static void writeSHORT(OutputStream os, int value, boolean bigendian) throws IOException {
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
    public static void writeInt3(OutputStream os, int value, boolean bigendian) throws IOException {
        if(bigendian) {
            os.write((byte)(value>>16 & 0xFF));
            os.write((byte)(value>>8 & 0xFF));
            os.write((byte)(value & 0xFF));
        }else {
            os.write((byte)(value & 0xFF));
            os.write((byte)(value>>8 & 0xFF));
            os.write((byte)(value>>16 & 0xFF));
        }
    }
    public static void writeInt5(OutputStream os, long value, boolean bigendian) throws IOException {
        if(bigendian) {
            os.write((byte)(value>>32 & 0xFF));
            os.write((byte)(value>>24 & 0xFF));
            os.write((byte)(value>>16 & 0xFF));
            os.write((byte)(value>>8 & 0xFF));
            os.write((byte)(value & 0xFF));
        }else {
            os.write((byte)(value & 0xFF));
            os.write((byte)(value>>8 & 0xFF));
            os.write((byte)(value>>16 & 0xFF));
            os.write((byte)(value>>24 & 0xFF));
            os.write((byte)(value>>32 & 0xFF));
        }
    }
    public static void writeInt6(OutputStream os, long value, boolean bigendian) throws IOException {
        if(bigendian) {
            os.write((byte)(value>>40 & 0xFF));
            os.write((byte)(value>>32 & 0xFF));
            os.write((byte)(value>>24 & 0xFF));
            os.write((byte)(value>>16 & 0xFF));
            os.write((byte)(value>>8 & 0xFF));
            os.write((byte)(value & 0xFF));
        }else {
            os.write((byte)(value & 0xFF));
            os.write((byte)(value>>8 & 0xFF));
            os.write((byte)(value>>16 & 0xFF));
            os.write((byte)(value>>24 & 0xFF));
            os.write((byte)(value>>32 & 0xFF));
            os.write((byte)(value>>40 & 0xFF));
        }
    }
    public static void writeInt7(OutputStream os, long value, boolean bigendian) throws IOException {
        if(bigendian) {
            os.write((byte)(value>>48 & 0xFF));
            os.write((byte)(value>>40 & 0xFF));
            os.write((byte)(value>>32 & 0xFF));
            os.write((byte)(value>>24 & 0xFF));
            os.write((byte)(value>>16 & 0xFF));
            os.write((byte)(value>>8 & 0xFF));
            os.write((byte)(value & 0xFF));
        }else {
            os.write((byte)(value & 0xFF));
            os.write((byte)(value>>8 & 0xFF));
            os.write((byte)(value>>16 & 0xFF));
            os.write((byte)(value>>24 & 0xFF));
            os.write((byte)(value>>32 & 0xFF));
            os.write((byte)(value>>40 & 0xFF));
            os.write((byte)(value>>48 & 0xFF));
        }
    }
    public static void writeLong(OutputStream os, long value, boolean bigendian) throws IOException {
        if(bigendian) {
            os.write((byte)(value>>56 & 0xFF));
            os.write((byte)(value>>48 & 0xFF));
            os.write((byte)(value>>40 & 0xFF));
            os.write((byte)(value>>32 & 0xFF));
            os.write((byte)(value>>24 & 0xFF));
            os.write((byte)(value>>16 & 0xFF));
            os.write((byte)(value>>8 & 0xFF));
            os.write((byte)(value & 0xFF));
        }else {
            os.write((byte)(value & 0xFF));
            os.write((byte)(value>>8 & 0xFF));
            os.write((byte)(value>>16 & 0xFF));
            os.write((byte)(value>>24 & 0xFF));
            os.write((byte)(value>>32 & 0xFF));
            os.write((byte)(value>>40 & 0xFF));
            os.write((byte)(value>>48 & 0xFF));
            os.write((byte)(value>>56 & 0xFF));
        }
    }
    public static void writeBCD(OutputStream os, int[] value) throws IOException {
        byte tmp = 0;
        for(int i=0;i<value.length;i+=2) {
            tmp |= ((byte)value[i] << 4);
            tmp |= ((byte)value[i+1]);
            os.write(tmp);
            tmp = 0;
        }
    }
    
    public static void writeBytes(OutputStream os, int[] raw) throws IOException{
        for(int i : raw) {
            os.write(i);
        }
    }
    
    public static void writeBytes(OutputStream os, byte[] raw) throws IOException{
        writeBytes(os, raw, 0);
    }
    
    public static void writeBytes(OutputStream os, byte[] raw, int from) throws IOException{
        os.write(raw, from, raw.length - from);
    }
    
    public static void writeIntegerOfType(OutputStream os, DataType type, int val, boolean bigEndian) throws IOException{
        String error;
        if((error = DataTypeOperations.of(type).checkRange(val, true))!=null) {
            throw new UnsatisfiedIOException(error)
                .withSiteAndOrdinal(StreamUtils.class, 1);
        }
        switch(type) {
        case BYTE:
            writeBYTE(os, (byte)val);
            break;
        case SHORT:
            writeSHORT(os, (short)val, bigEndian);
            break;
        case INT:
            writeInt(os, val, bigEndian);
            break;
        default:throw new Error("should not reach here");
        }
    }
    
    public static void writeBit(BitOutputStream os, byte value, int num, boolean bigEndian) throws IOException {
        if(!bigEndian)
            value = reverseNBits(value, num);
        os.writeBits(value, num);
    }
    
    public static void writeBit(BitOutputStream os, boolean value) throws IOException {
        os.writeBits(value);
    }
    
    private static final BigInteger BIT7 = BigInteger.valueOf(0b01111111);
    private static final BigInteger SET_FIRST = BigInteger.valueOf(0b10000000);
    
    static void writeUnsignedVarint(
            OutputStream os
            , long value
            , boolean bigEndian) throws IOException {
        writeUnsignedVarint(os, BigInteger.valueOf(value), bigEndian);
    }
    
    public static int getUnsignedVarintLength(BigInteger value) {
        int bitLength = value.bitLength();
        return bitLength / 7 + Integer.signum(bitLength % 7);
    }
    
    public static void writeUnsignedVarint(
            OutputStream os
            , BigInteger value
            , boolean bigEndian) throws IOException {
        
        int result = value.compareTo(BigInteger.ZERO);
        
        if(result == 0) {
            //0b000_000_00
            writeBYTE(os, (byte)0);
            return;
        }
            
        byte[] array = new byte[getUnsignedVarintLength(value)];
        int length = array.length;
        int ptr = length - 1;
        while(value.compareTo(BigInteger.ZERO) > 0) {
            array[ptr--] = (byte) value.and(BIT7).or(SET_FIRST).shortValue();
            value = value.shiftRight(7);
        }
        if(!bigEndian) {
            reverse(array);
        }
        
        array[length-1] &= 0b01111111;
        writeBytes(os, array);
    }
    
    static byte[] reverse(byte[] array) {
        reverse(array, 0, array.length);
        return array;
    }
    
    static byte[] reverse(byte[] array, int from, int to) {
        for(int k = from, l = to - from, h = l/2 + from; k < h; ++k) {
            int r = from + to - k - 1;
            byte temp = array[k];
            array[k] = array[r];
            array[r] = temp;
        }
        return array;
    }
    
    //---------------------------------
    
    public static BigInteger readVarint(
            MarkableInputStream os
            , boolean bigEndian) throws IOException {
        BigInteger ret = BigInteger.ZERO;
        if(bigEndian) {
            for(;;) {
                byte flag = os.readBits(1);
                ret = ret.shiftLeft(7).or(BigInteger.valueOf(os.readBits(7)));
                if(flag == 0) {
                    break;
                }
            }
        }else {
            int count = 0;
            for(;;) {
                byte flag = os.readBits(1);
                ret = ret.or(BigInteger.valueOf(os.readBits(7)).shiftLeft(count));
                if(flag == 0) {
                    break;
                }
                count += 7;
            }
        }
        return ret;
    }
    
    public static byte readBit(MarkableInputStream in, int num, boolean bigEndian) throws IOException {
        byte ret = in.readBits(num);
        if(!bigEndian)
            ret = reverseNBits(ret, num);
        return ret;
    }
    
    public static int readByte(InputStream in,boolean signed) throws IOException{
        return signed ? (byte)read(in) : read(in);
    }
    
    public static int readShort(InputStream in,boolean signed, boolean bigEndian) throws IOException{
        int ret = 0;
        int b1 = read(in);
        int b2 = read(in);
        if(bigEndian) {
            ret = ((b1<<8) | b2);
        }else {
            ret = ((b2<<8) | b1);
        }
        return signed ? (short)ret : ret;
    }
    
    public static long readInt(InputStream in,boolean signed, boolean bigEndian) throws IOException{
        int ret = 0;
        if(bigEndian) {
            ret |= (read(in) <<24);
            ret |= (read(in) << 16);
            ret |= (read(in) << 8);
            ret |= read(in);
        }else {
            ret |= read(in);
            ret |= (read(in) << 8);
            ret |= (read(in) << 16);
            ret |= (read(in)<<24);
        }
        long tmp = (((long)ret) & 0xFFFFFFFFL);
        return signed ? (int)tmp : tmp;
    }
    
    public static int readInt3(InputStream in,boolean signed, boolean bigEndian) throws IOException{
        int ret = 0;
        if(bigEndian) {
            ret |= (read(in) << 16);
            ret |= (read(in) << 8);
            ret |= read(in);
        }else {
            ret |= read(in);
            ret |= (read(in) <<  8);
            ret |= (read(in) << 16);
        }
        if(signed && ((ret & 0b10000000_00000000_00000000) !=0)) {
            ret |= 0b11111111_00000000_00000000_00000000;
        }
        return ret;
    }
    
    public static long readInt5(InputStream in,boolean signed, boolean bigEndian) throws IOException{
        long ret = 0;
        if(bigEndian) {
            ret |= (((long)read(in)) << 32);
            ret |= (((long)read(in)) << 24);
            ret |= (((long)read(in)) << 16);
            ret |= (((long)read(in)) << 8);
            ret |= ((long)read(in));
        }else {
            ret |= ((long)read(in));
            ret |= (((long)read(in)) <<  8);
            ret |= (((long)read(in)) << 16);
            ret |= (((long)read(in)) << 24);
            ret |= (((long)read(in)) << 32);
        }
        if(signed && ((ret & 0b10000000_00000000_00000000_00000000_00000000L) !=0)) {
            ret |= 0b11111111_11111111_11111111_00000000_00000000_00000000_00000000_00000000L;
        }
        return ret;
    }
    
    public static long readInt6(InputStream in,boolean signed, boolean bigEndian) throws IOException{
        long ret = 0;
        if(bigEndian) {
            ret |= (((long)read(in)) << 40);
            ret |= (((long)read(in)) << 32);
            ret |= (((long)read(in)) << 24);
            ret |= (((long)read(in)) << 16);
            ret |= (((long)read(in)) << 8);
            ret |= ((long)read(in));
        }else {
            ret |= ((long)read(in));
            ret |= (((long)read(in)) <<  8);
            ret |= (((long)read(in)) << 16);
            ret |= (((long)read(in)) << 24);
            ret |= (((long)read(in)) << 32);
            ret |= (((long)read(in)) << 40);
        }
        if(signed && ((ret & 0b10000000_00000000_00000000_00000000_00000000_00000000L) !=0)) {
            ret |= 0b11111111_11111111_00000000_00000000_00000000_00000000_00000000_00000000L;
        }
        return ret;
    }
    
    public static long readInt7(InputStream in,boolean signed, boolean bigEndian) throws IOException{
        long ret = 0;
        if(bigEndian) {
            ret |= (((long)read(in)) << 48);
            ret |= (((long)read(in)) << 40);
            ret |= (((long)read(in)) << 32);
            ret |= (((long)read(in)) << 24);
            ret |= (((long)read(in)) << 16);
            ret |= (((long)read(in)) << 8);
            ret |= ((long)read(in));
        }else {
            ret |= ((long)read(in));
            ret |= (((long)read(in)) <<  8);
            ret |= (((long)read(in)) << 16);
            ret |= (((long)read(in)) << 24);
            ret |= (((long)read(in)) << 32);
            ret |= (((long)read(in)) << 40);
            ret |= (((long)read(in)) << 48);
        }
        if(signed && ((ret & 0b10000000_00000000_00000000_00000000_00000000_00000000_00000000L) !=0)) {
            ret |= 0b11111111_00000000_00000000_00000000_00000000_00000000_00000000_00000000L;
        }
        return ret;
    }
    
    public static long readLong(InputStream in, boolean bigEndian) throws IOException{
        long ret = 0;
        if(bigEndian) {
            ret |= (((long)read(in)) << 56);
            ret |= (((long)read(in)) << 48);
            ret |= (((long)read(in)) << 40);
            ret |= (((long)read(in)) << 32);
            ret |= (((long)read(in)) << 24);
            ret |= (((long)read(in)) << 16);
            ret |= (((long)read(in)) << 8);
            ret |= ((long)read(in));
        }else {
            ret |= ((long)read(in));
            ret |= (((long)read(in)) << 8);
            ret |= (((long)read(in)) << 16);
            ret |= (((long)read(in)) << 24);
            ret |= (((long)read(in)) << 32);
            ret |= (((long)read(in)) << 40);
            ret |= (((long)read(in)) << 48);
            ret |= (((long)read(in)) << 56);
        }
        return ret;
    }
    
    public static BigInteger readUnsignedLong(InputStream in, boolean bigEndian) throws IOException{
        byte[] array = readBytes(in, 8);
        if(!bigEndian) {
            swap(array,0,7);
            swap(array,1,6);
            swap(array,2,5);
            swap(array,3,4);
        }
        return new BigInteger(1, array);
    }
    private static void swap(byte[] array, int left, int right) {
        byte tmp = array[left];
        array[left] = array[right];
        array[right] = tmp;
    }
    
    public static final String readStringBCD(InputStream in, int len) throws IOException {
        byte[] arr = readBytes(in, len);
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<arr.length;++i) {
            sb.append((char)(((arr[i] >> 4) & 0x0F)+'0'));
            sb.append((char)((arr[i] & 0x0F)+'0'));
        }
        return sb.toString();
    }
    public static final long readIntegerBCD(InputStream in, int len) throws IOException {
        byte[] arr = readBytes(in, len);
        long ret = 0;
        for(int i=0;i<arr.length;++i) {
            ret = ret * 10 + ((arr[i] >> 4) & 0x0F);
            ret = ret * 10 + (arr[i] & 0x0F);
            if(ret<0) {
                throw new UnsatisfiedIOException("BCD value overflows Java long dataType range")
                        .withSiteAndOrdinal(StreamUtils.class, 2);
            }
        }
        return ret;
    }
    public static byte[] readBytes(InputStream in, int numBytes) throws IOException{
        byte[] arr = new byte[numBytes];
        int len = 0;
        while(len<arr.length) {
            int l = in.read(arr,len,arr.length-len);
            if(l==-1) {
                throw new EOFException();
            }
            len += l;
        }
        return arr;
    }
    
    public static int readIntegerOfType(InputStream in, DataType type, boolean bigEndian) throws IOException{
        int length = 0;
        switch(type) {
        case BYTE:
            length = readByte(in, false);
            break;
        case SHORT:
            length = readShort(in, false, bigEndian);
            break;
        case INT:
            long _length = readInt(in, false, bigEndian);
            String error;
            //array or list length in Java cannot exceed signed 32-bit integer
            if((error = DataTypeOperations.INT.checkRange(_length, false))!=null) {
                throw new UnsatisfiedIOException(error)
                    .withSiteAndOrdinal(StreamUtils.class, 3);
            }
            length = (int)_length;
            break;
        default:throw new Error("should not reach here");
        }
        return length;
    }
    
    public static final void serializeBCD(String str, OutputStream dest, FieldInfo ctx, Object self) 
            throws ConversionException, IOException {
        checkBCDLength(str, ctx.annotation(BCD.class).value());
        int len = str.length();
        int[] values = new int[len];
        for(int i=0;i<len;++i) {
            char c = str.charAt(i);
            if(!(c>='0' && c<='9')) {
                throw new ExtendedConversionException(
                        ctx.enclosingEntityClass,ctx.name,
                        "only numeric value is supported in bcd")
                            .withSiteAndOrdinal(StreamUtils.class, 14);
            }
            values[i] = c-'0';
        }
        writeBCD(dest, values);
    }
    
    private static final void checkBCDLength(String src, int length) {
        if((src.length()>>1)!=length) {
            throw new UnsatisfiedConstraintException(String.format(
                    "length of string should be [%d] (double long as declared BCD value), but it was [%d]", length*2, src.length()))
                        .withSiteAndOrdinal(StreamUtils.class, 17);
        }
    }
    
    public static final void serializeAsCHAR(String str, OutputStream dest, FieldInfo ctx, Object self)
            throws ConversionException, IOException {
        Charset cs = ctx.charsetForSerializingCHAR(self);
        byte[] bytes = str.getBytes(cs);
        byte[] ending = ctx.endsWith;
        int length = ctx.lengthForSerializingCHAR(self);
        if(length<0) {
            if(ending==null) {
                length = bytes.length;
                writeIntegerOfType(dest, ctx.lengthType(), length, ctx.bigEndian);
                writeBytes(dest, bytes);
            }else {
                writeBytes(dest, bytes);
                writeBytes(dest, ending);
            }
        }else if(length!=bytes.length) {
            throw new ExtendedConversionException(
                    ctx.enclosingEntityClass,ctx.name,
                    String.format("length of string representation [%s] does not equals with declared CHAR length [%d]"
                                ,str,length))
                        .withSiteAndOrdinal(StreamUtils.class, 22);
        }else {
            writeBytes(dest, bytes);
        }
    }
    
    public static final void serializeAsCHAR(long val, OutputStream dest, FieldInfo ctx, Object self)
            throws ConversionException, IOException {
        if(val<0) {
            //implementation choice
            throw new ExtendedConversionException(
                    ctx.enclosingEntityClass,ctx.name
                    ,"negative number should not be converted to CHAR")
                        .withSiteAndOrdinal(StreamUtils.class, 0);
        }
        String str = Long.toString(val);
        serializeAsCHAR(str,dest,ctx,self);
    }
    
    public static final void serializeAsCHAR(BigInteger val, OutputStream dest, FieldInfo ctx, Object self)
            throws ConversionException, IOException {
        if(val.compareTo(BigInteger.ZERO)<0) {
            //implementation choice
            throw new ExtendedConversionException(
                    ctx.enclosingEntityClass,ctx.name
                    ,"negative number should not be converted to CHAR")
                        .withSiteAndOrdinal(StreamUtils.class, 18);
        }
        String str = val.toString();
        serializeAsCHAR(str,dest,ctx,self);
    }
    
    public static final long deserializeAsCHAR(
            InputStream in, FieldInfo ctx, Object self, DataType type)
            throws IOException, ConversionException {
        long ret = 0;
        String error = null;
        byte[] numChars = readBytesForDeserializingCHAR(in, ctx, self, type);
        parsing:{
            for(byte b:numChars) {
                if(!(b>='0' && b<='9')) {
                    error = "streams contains non-numeric characters";
                    break parsing;
                }
                ret = (ret<<3)+(ret<<1)+(b-'0');
                if(ret<0) {
                    error = "numeric string overflows:"+Arrays.toString(numChars);
                    break parsing;
                }
            }
        }
        if(error!=null) {
            throw new ExtendedConversionException(
                    ctx.enclosingEntityClass,ctx.name, 
                    error)
                    .withSiteAndOrdinal(StreamUtils.class, 13);
        }
        if(type!=null) {
            checkRangeInContext(type, ret, ctx);
        }
        return ret;
    }
    
    //TODO:
    public static final BigInteger deserializeAsBigCHAR(
            InputStream in, FieldInfo ctx, Object self, DataType type)
            throws IOException, ConversionException {
        long ret = 0;
        BigInteger ret2 = null;
        String error = null;
        byte[] numChars = readBytesForDeserializingCHAR(in, ctx, self, type);
        parsing:{
            for(byte b:numChars) {
                if(!(b>='0' && b<='9')) {
                    error = "streams contains non-numeric characters";
                    break parsing;
                }
                if(ret2==null) {
                    long tmp = (ret<<3)+(ret<<1)+(b-'0');
                    if(tmp<0) {
                        ret2 = BigInteger.valueOf(ret);
                        ret2 = ret2.multiply(BigInteger.TEN).add(BigInteger.valueOf(b-'0'));
                    }else {
                        ret = tmp;
                    }
                }else {
                    ret2 = ret2.multiply(BigInteger.TEN).add(BigInteger.valueOf(b-'0'));
                }
            }
        }
        if(error!=null) {
            throw new ExtendedConversionException(ctx.enclosingEntityClass,ctx.name, error)
                    .withSiteAndOrdinal(StreamUtils.class, 19);
        }
        //no need to check the range here
        //as we do not permit negative sign in the character streams
        //and BigInteger never overflows a BigInteger except it overflows the memory
        return ret2==null ? BigInteger.valueOf(ret) : ret2;
    }
    
    public static int[] checkAndConvertToBCD(long val, int bcdBytes) {
        if(val<0) {
            throw new UnsatisfiedConstraintException(String.format("negative number [%d] cannot be stored as BCD",val))
                        .withSiteAndOrdinal(StreamUtils.class, 15);
        }
        long copy = val;
        int[] values = new int[bcdBytes*2];
        int ptr = values.length-1;
        while(ptr>=0 && copy>0) {
            values[ptr--] = (int) (copy % 10);
            copy /= 10;
        }
        if(copy>0 || ptr>0) {
            throw new UnsatisfiedConstraintException(
                    String.format("string format of number [%d] cannot fit in [%d]-byte BCD value", val, bcdBytes))
                        .withSiteAndOrdinal(StreamUtils.class, 16);
        }
        return values;
    }
    
    public static final void checkRangeInContext(DataType type,long val,FieldInfo ctx) throws ConversionException {
        String error;
        if((error = DataTypeOperations.of(type).checkRange(val, ctx.unsigned))!=null) {
            throw new ExtendedConversionException(ctx.enclosingEntityClass, ctx.name, error)
                        .withSiteAndOrdinal(StreamUtils.class, 11);
        }
    }
    
    private static byte[] readBytesForDeserializingCHAR(
            InputStream in, FieldInfo ctx, Object self, DataType type) throws IOException, ConversionException {
        int length = ctx.lengthForDeserializingCHAR(self, in);
        if(length<0) {
            length = readIntegerOfType(in, ctx.lengthType(), ctx.bigEndian);
        }
        byte[] numChars = readBytes(in, length);
        /*
         * such strings causes asymmetry between serialization and deserialization. it
         * is possible to avoid this problem by using written-ahead length, however such
         * use case is rare so it is better prevent deserialization from such strings to
         * a numeric dataType explicitly rather than later cause errors that are hard to
         * detect.
         */
        if(numChars.length>1 && numChars[0]=='0') {
            throw new ExtendedConversionException(
                    ctx.enclosingEntityClass,ctx.name,
                    "streams contains numeric string with leading zero")
                    .withSiteAndOrdinal(StreamUtils.class, 20);
        }
        return numChars;
    }
    
    private static int read(InputStream in) throws IOException {
        int b = in.read();
        if(b==-1) {
            throw new EOFException();
        }
        return b;
    }
    
    //reverse count bits to the right of specified byte
    //not the whole byte
    static byte reverseNBits(byte src, int count) {
        byte ret = 0;
        int pos = 0;
        switch(count-- % 8) {
        case 0:
            ret |= ((src >> count--) & 1) << pos++;
        case 7:
            ret |= ((src >> count--) & 1) << pos++;
        case 6:
            ret |= ((src >> count--) & 1) << pos++;
        case 5:
            ret |= ((src >> count--) & 1) << pos++;
        case 4:
            ret |= ((src >> count--) & 1) << pos++;
        case 3:
            ret |= ((src >> count--) & 1) << pos++;
        case 2:
            ret |= ((src >> count--) & 1) << pos++;
        case 1:
            ret |= ((src >> count--) & 1) << pos++;
        }
        return ret;
    }
    
    private static final BigDecimal TWO = new BigDecimal("2");
    
    static double fixedFloatBytesToDouble(byte[] src, int intLimit, int fractionLimit, boolean unsigned) {
        return fixedFloatBytesToBigdecimal(src, intLimit, fractionLimit, unsigned).doubleValue();
    }
    
    static BigDecimal fixedFloatBytesToBigdecimal(byte[] src, int intLimit, int fractionLimit, boolean unsigned) {
        byte[] integer = Arrays.copyOf(src, intLimit);
        boolean positive = true;
        if(!unsigned && (integer[0] & 0x80) > 0){
            positive = false;
            integer[0] &= 0x7F;
        }
        
        BigInteger integralPart = new BigInteger(1, integer);
        
        int to = src.length - 1;
        while(to >= intLimit && src[to] == 0)--to;
        if(to < intLimit) {
            if(positive) {
                return new BigDecimal(integralPart);
            }
            return new BigDecimal(integralPart).negate();
        }
        
        BigInteger peseudoFractionalPart = new BigInteger(1, Arrays.copyOfRange(src, intLimit, to + 1));
        BigDecimal fractionalPart = new BigDecimal(peseudoFractionalPart).divide(
                                            TWO.pow((to - intLimit + 1)*8), MathContext.DECIMAL128);
        
        BigDecimal result = new BigDecimal(integralPart)
                                .add(fractionalPart, MathContext.DECIMAL128);
        if(positive) {
            return result;
        }
        return result.negate();
    }
    
    static byte[] bigDecimalToFixedFloatBytes(BigDecimal d, int intLimit, int fractionLimit) {
        return doubleToFixedFloatBytes(d.doubleValue(), intLimit, fractionLimit);
    }
    
    static byte[] doubleToFixedFloatBytes(double d, int intLimit, int fractionLimit) {
        if(Double.isNaN(d))
            throw new UnsatisfiedConstraintException("NaN is not supported")
                    .withSiteAndOrdinal(StreamUtils.class, 21);
        if(Double.isInfinite(d))
            throw new UnsatisfiedConstraintException("Infinites are not supported")
                    .withSiteAndOrdinal(StreamUtils.class, 22);
        
        byte[] ret = new byte[intLimit + fractionLimit];
        
        if(Double.compare(d, -0.0d) == 0) {
            ret[0] |= 0x80;
            return ret;
        }
        
        /*
         * BigDecimal(double) fails for cases like 0.1 when double literal introduces slight error in fractional part.
         * but calling BigDecimal(String) by converting the double value to String using toString also 
         * fails for cases like 2^-25 when the toString method cuts off trailing digits, introducing error in the final result
         * for an double value that should be represented accurately in binary format.
         */
        String s = new BigDecimal(d).toPlainString();
        int idx = s.indexOf('.');
        
        integerToBinary(s, idx, intLimit, ret);
        fractionToBinary(s, idx, fractionLimit, ret);
        
        return ret;
    }
    
    private static void integerToBinary(String s, int idx, int limit, byte[] out) {
        boolean negative = s.charAt(0) == '-';
        s = s.substring(negative ? 1 : 0, idx < 0 ? s.length() : idx);
        byte[] binary = new BigInteger(s).toByteArray();
        if(binary.length > limit)
            throw new UnsatisfiedConstraintException(
                    "integral part of this number cannot fit in "+limit+" bytes")
                    .withSiteAndOrdinal(StreamUtils.class, 23);
        System.arraycopy(binary, 0, out, limit - binary.length, binary.length);
        if(negative)
            out[0] |= 0x80;
    }
    
    private static void fractionToBinary(String s, int idx, int limit, byte[] out){
        if(idx < 0)
            return;
        int count = 0;
        int start = out.length - limit;
        limit *= 8;
        char[] num = s.substring(idx + 1).toCharArray();
        int to = num.length;
        while(!(to == 1 && num[to - 1] == '0') && count < limit) {
            to = multiplyBy2(num, to);
            if(to == (to = Math.abs(to))) {
                out[start + count/8] |= (1L << (7 - count % 8));
            }
            ++count;
        }
    }
    
    private static int multiplyBy2(char[] array, int to) {
        int carryover = 0;
        int k = to - 1;
        while( k >=0 && array[k] == '0')--k;
        to = k + 1;
        if(k < 0)
            return to;
        for(; k >= 0; --k) {
            int num = (array[k]-'0') * 2 + carryover;
            if(num >= 10) {
                carryover = 1;
                array[k] = (char) (num - 10 + '0');
            }else {
                carryover = 0;
                array[k] = (char) (num + '0');
            }
        }
        return carryover > 0 ? to : -to;
    }
}