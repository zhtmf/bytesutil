package io.github.zhtmf.converters;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Arrays;

import io.github.zhtmf.ConversionException;
import io.github.zhtmf.annotations.types.BCD;
import io.github.zhtmf.converters.auxiliary.DataType;
import io.github.zhtmf.converters.auxiliary.exceptions.ExtendedConversionException;
import io.github.zhtmf.converters.auxiliary.exceptions.UnsatisfiedConstraintException;
import io.github.zhtmf.converters.auxiliary.exceptions.UnsatisfiedIOException;

class StreamUtils {
    
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
        os.write(raw);
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
        default:throw new Error("cannot happen");
        }
    }
    
    //---------------------------------
    
    public static int readByte(InputStream is,boolean signed) throws IOException{
        return signed ? (byte)read(is) : read(is);
    }
    
    public static int readShort(InputStream is,boolean signed, boolean bigEndian) throws IOException{
        int ret = 0;
        int b1 = read(is);
        int b2 = read(is);
        if(bigEndian) {
            ret = ((b1<<8) | b2);
        }else {
            ret = ((b2<<8) | b1);
        }
        return signed ? (short)ret : ret;
    }
    
    public static long readInt(InputStream is,boolean signed, boolean bigEndian) throws IOException{
        int ret = 0;
        if(bigEndian) {
            ret |= (read(is) <<24);
            ret |= (read(is) << 16);
            ret |= (read(is) << 8);
            ret |= read(is);
        }else {
            ret |= read(is);
            ret |= (read(is) << 8);
            ret |= (read(is) << 16);
            ret |= (read(is)<<24);
        }
        long tmp = (((long)ret) & 0xFFFFFFFFL);
        return signed ? (int)tmp : tmp;
    }
    
    public static int readInt3(InputStream is,boolean signed, boolean bigEndian) throws IOException{
        int ret = 0;
        if(bigEndian) {
            ret |= (read(is) << 16);
            ret |= (read(is) << 8);
            ret |= read(is);
        }else {
            ret |= read(is);
            ret |= (read(is) <<  8);
            ret |= (read(is) << 16);
        }
        if(signed && ((ret & 0b10000000_00000000_00000000) !=0)) {
            ret |= 0b11111111_00000000_00000000_00000000;
        }
        return ret;
    }
    
    public static long readInt5(InputStream is,boolean signed, boolean bigEndian) throws IOException{
        long ret = 0;
        if(bigEndian) {
            ret |= (((long)read(is)) << 32);
            ret |= (((long)read(is)) << 24);
            ret |= (((long)read(is)) << 16);
            ret |= (((long)read(is)) << 8);
            ret |= ((long)read(is));
        }else {
            ret |= ((long)read(is));
            ret |= (((long)read(is)) <<  8);
            ret |= (((long)read(is)) << 16);
            ret |= (((long)read(is)) << 24);
            ret |= (((long)read(is)) << 32);
        }
        if(signed && ((ret & 0b10000000_00000000_00000000_00000000_00000000L) !=0)) {
            ret |= 0b11111111_11111111_11111111_00000000_00000000_00000000_00000000_00000000L;
        }
        return ret;
    }
    
    public static long readInt6(InputStream is,boolean signed, boolean bigEndian) throws IOException{
        long ret = 0;
        if(bigEndian) {
            ret |= (((long)read(is)) << 40);
            ret |= (((long)read(is)) << 32);
            ret |= (((long)read(is)) << 24);
            ret |= (((long)read(is)) << 16);
            ret |= (((long)read(is)) << 8);
            ret |= ((long)read(is));
        }else {
            ret |= ((long)read(is));
            ret |= (((long)read(is)) <<  8);
            ret |= (((long)read(is)) << 16);
            ret |= (((long)read(is)) << 24);
            ret |= (((long)read(is)) << 32);
            ret |= (((long)read(is)) << 40);
        }
        if(signed && ((ret & 0b10000000_00000000_00000000_00000000_00000000_00000000L) !=0)) {
            ret |= 0b11111111_11111111_00000000_00000000_00000000_00000000_00000000_00000000L;
        }
        return ret;
    }
    
    public static long readInt7(InputStream is,boolean signed, boolean bigEndian) throws IOException{
        long ret = 0;
        if(bigEndian) {
            ret |= (((long)read(is)) << 48);
            ret |= (((long)read(is)) << 40);
            ret |= (((long)read(is)) << 32);
            ret |= (((long)read(is)) << 24);
            ret |= (((long)read(is)) << 16);
            ret |= (((long)read(is)) << 8);
            ret |= ((long)read(is));
        }else {
            ret |= ((long)read(is));
            ret |= (((long)read(is)) <<  8);
            ret |= (((long)read(is)) << 16);
            ret |= (((long)read(is)) << 24);
            ret |= (((long)read(is)) << 32);
            ret |= (((long)read(is)) << 40);
            ret |= (((long)read(is)) << 48);
        }
        if(signed && ((ret & 0b10000000_00000000_00000000_00000000_00000000_00000000_00000000L) !=0)) {
            ret |= 0b11111111_00000000_00000000_00000000_00000000_00000000_00000000_00000000L;
        }
        return ret;
    }
    
    public static long readLong(InputStream is, boolean bigEndian) throws IOException{
        long ret = 0;
        if(bigEndian) {
            ret |= (((long)read(is)) << 56);
            ret |= (((long)read(is)) << 48);
            ret |= (((long)read(is)) << 40);
            ret |= (((long)read(is)) << 32);
            ret |= (((long)read(is)) << 24);
            ret |= (((long)read(is)) << 16);
            ret |= (((long)read(is)) << 8);
            ret |= ((long)read(is));
        }else {
            ret |= ((long)read(is));
            ret |= (((long)read(is)) << 8);
            ret |= (((long)read(is)) << 16);
            ret |= (((long)read(is)) << 24);
            ret |= (((long)read(is)) << 32);
            ret |= (((long)read(is)) << 40);
            ret |= (((long)read(is)) << 48);
            ret |= (((long)read(is)) << 56);
        }
        return ret;
    }
    
    public static BigInteger readUnsignedLong(InputStream is, boolean bigEndian) throws IOException{
        byte[] array = readBytes(is, 8);
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
    
    public static final String readStringBCD(InputStream is, int len) throws IOException {
        byte[] arr = readBytes(is, len);
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<arr.length;++i) {
            sb.append((char)(((arr[i] >> 4) & 0x0F)+'0'));
            sb.append((char)((arr[i] & 0x0F)+'0'));
        }
        return sb.toString();
    }
    public static final long readIntegerBCD(InputStream is, int len) throws IOException {
        byte[] arr = readBytes(is, len);
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
    public static byte[] readBytes(InputStream is, int numBytes) throws IOException{
        byte[] arr = new byte[numBytes];
        int len = 0;
        while(len<arr.length) {
            int l = is.read(arr,len,arr.length-len);
            if(l==-1) {
                throw new EOFException();
            }
            len += l;
        }
        return arr;
    }
    
    public static int readIntegerOfType(InputStream src, DataType type, boolean bigEndian) throws IOException{
        int length = 0;
        switch(type) {
        case BYTE:
            length = readByte(src, false);
            break;
        case SHORT:
            length = readShort(src, false, bigEndian);
            break;
        case INT:
            long _length = readInt(src, false, bigEndian);
            String error;
            //array or list length in Java cannot exceed signed 32-bit integer
            if((error = DataTypeOperations.INT.checkRange(_length, false))!=null) {
                throw new UnsatisfiedIOException(error)
                    .withSiteAndOrdinal(StreamUtils.class, 3);
            }
            length = (int)_length;
            break;
        default:throw new Error("cannot happen");
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
            InputStream is, FieldInfo ctx, Object self, DataType type)
            throws IOException, ConversionException {
        long ret = 0;
        String error = null;
        byte[] numChars = readBytesForDeserializingCHAR(is, ctx, self, type);
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
    
    public static final BigInteger deserializeAsBigCHAR(
            InputStream is, FieldInfo ctx, Object self, DataType type)
            throws IOException, ConversionException {
        long ret = 0;
        BigInteger ret2 = null;
        String error = null;
        byte[] numChars = readBytesForDeserializingCHAR(is, ctx, self, type);
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
            InputStream is, FieldInfo ctx, Object self, DataType type) throws IOException, ConversionException {
        int length = ctx.lengthForDeserializingCHAR(self, is);
        if(length<0) {
            length = readIntegerOfType(is, ctx.lengthType(), ctx.bigEndian);
        }
        byte[] numChars = readBytes(is, length);
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
    
    private static int read(InputStream bis) throws IOException {
        int b = bis.read();
        if(b==-1) {
            throw new EOFException();
        }
        return b;
    }
}