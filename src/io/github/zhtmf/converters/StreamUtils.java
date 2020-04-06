package io.github.zhtmf.converters;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Arrays;

import io.github.zhtmf.ConversionException;
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
    
    public static void writeIntegerOfType(OutputStream os, int val, FieldInfo ctx) throws IOException{
        DataType type = ctx.lengthType();
        boolean bigEndian = ctx.bigEndian;
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
    
    public static int readIntegerOfType(InputStream in, FieldInfo ctx) throws IOException{
        int length = 0;
        boolean bigEndian = ctx.bigEndian;
        switch(ctx.lengthType()) {
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
        if((str.length()>>1) != ctx.bcdLength) {
            throw new UnsatisfiedConstraintException(String.format(
                    "length of string should be [%d] (double long as declared BCD value), but it was [%d]"
                        , ctx.bcdLength*2, str.length()))
                        .withSiteAndOrdinal(StreamUtils.class, 17);
        }
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
    
    public static final void serializeAsCHAR(String str, OutputStream dest, FieldInfo ctx, Object self)
            throws ConversionException, IOException {
        Charset cs = ctx.charsetForSerializingCHAR(self);
        byte[] bytes = str.getBytes(cs);
        byte[] ending = ctx.endsWith;
        int length = ctx.lengthForSerializingCHAR(self);
        if(length<0) {
            if(ending==null) {
                length = bytes.length;
                writeIntegerOfType(dest, length, ctx);
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
    
    public static final BigInteger deserializeBigIntegerAsCHAR(
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
        if((error = DataTypeOperations.of(type).checkRange(val, ctx))!=null) {
            throw new ExtendedConversionException(ctx.enclosingEntityClass, ctx.name, error)
                        .withSiteAndOrdinal(StreamUtils.class, 11);
        }
    }
    
    private static byte[] readBytesForDeserializingCHAR(
            InputStream in, FieldInfo ctx, Object self, DataType type) throws IOException, ConversionException {
        int length = ctx.lengthForDeserializingCHAR(self, in);
        if(length<0) {
            length = readIntegerOfType(in, ctx);
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
    private static final BigDecimal MAX = new BigDecimal(Double.MAX_VALUE);
    private static final BigDecimal MIN = new BigDecimal(-Double.MAX_VALUE);
    
    //---------- deserialize fixed point number ----------------
    
    static double fixedPointBytesToDouble(byte[] src, FieldInfo ctx) {
        BigDecimal result = fixedPointBytesToBigdecimal(src, ctx);
        if(result.compareTo(MAX) > 0) 
            throw new UnsatisfiedConstraintException(
                    "number overflows a Java double")
                    .withSiteAndOrdinal(StreamUtils.class, 24);
        if(result.compareTo(MIN) < 0) 
            throw new UnsatisfiedConstraintException(
                    "number underflows a Java double")
                    .withSiteAndOrdinal(StreamUtils.class, 25);
        return result.doubleValue();
    }
    
    static BigDecimal fixedPointBytesToBigdecimal(byte[] src, FieldInfo ctx) {
        
        boolean negative = ctx.signed && (src[0] & 0x80) > 0;
        
        BigInteger pseudoInteger = negative ? new BigInteger(src) : new BigInteger(1 ,src);
        int fractionLimit = ctx.fixedNumberLengths[1];
        
        return fractionLimit > 0 ? new BigDecimal(pseudoInteger).divide(TWO.pow(fractionLimit * 8)) : new BigDecimal(pseudoInteger);
    }
    
    //---------- serialize fixed point number ----------------
    
    static byte[] bigDecimalToFixedPointBytes(BigDecimal d, FieldInfo ctx) {
        
        int[] limits = ctx.fixedNumberLengths;
        int intLimit = limits[0];
        int fractionLimit = limits[1];
        
        String error = DataTypeOperations.FIXED.checkRange(d, ctx);
        if(error != null)
            throw new UnsatisfiedConstraintException(error)
                        .withSiteAndOrdinal(StreamUtils.class, 26);
        
        boolean negative = d.compareTo(BigDecimal.ZERO) < 0;
        d = d.abs();
        
        byte[] ret = new byte[intLimit + fractionLimit];
        
        String s = d.toPlainString();
        int idx = s.indexOf('.');
        
        integerToBinary(s, idx, intLimit, ret, ctx.signed);
        fractionToBinary(s, idx, fractionLimit, ret);
        
        if(negative) {
            //treating the array as a two's complement number 
            //and negate it
            int carryover = 1;
            for(int k = ret.length - 1; k >= 0; --k) {
                ret[k] = (byte) ~ret[k];
                if(ret[k] == (byte)0xFF && carryover == 1) {
                    ret[k] = 0;
                    carryover = 1;
                }else {
                    ret[k] += carryover;
                    carryover = 0;
                }
            }
        }
        
        return ret;
    }
    
    static byte[] doubleToFixedPointBytes(double d, FieldInfo ctx) {
        if(Double.isNaN(d))
            throw new UnsatisfiedConstraintException("NaN is not supported")
                    .withSiteAndOrdinal(StreamUtils.class, 21);
        if(Double.isInfinite(d))
            throw new UnsatisfiedConstraintException("Infinites are not supported")
                    .withSiteAndOrdinal(StreamUtils.class, 22);
        
        /*
         * BigDecimal(double) fails for cases like 0.1 when double literal introduces slight error in fractional part.
         * but calling BigDecimal(String) by converting the double value to String using toString also 
         * fails for cases like 2^-25 when the toString method cuts off trailing digits, introducing error in the final result
         * for an double value that originally can be represented accurately in binary format.
         */
        return bigDecimalToFixedPointBytes(new BigDecimal(d), ctx);
    }
    
    private static void integerToBinary(String s, int idx, int limit, byte[] out, boolean signed) {
        
        byte[] binary = new BigInteger(s.substring(0, idx < 0 ? s.length() : idx)).toByteArray();
        
        int length = binary.length;
        int from = 0;
        if(binary[0] == 0) {
            --length;
            from = 1;
        }
        
        System.arraycopy(binary, from, out, limit - length, length);
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
        //toPlainString will eliminate trailing zero
        //so theoretically k will not be reduced to less than zero
        while(k >=0 && array[k] == '0')--k;
        to = k + 1;
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