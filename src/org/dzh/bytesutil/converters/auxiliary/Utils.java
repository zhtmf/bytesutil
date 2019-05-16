package org.dzh.bytesutil.converters.auxiliary;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import org.dzh.bytesutil.ConversionException;
import org.dzh.bytesutil.annotations.types.BCD;
import org.dzh.bytesutil.annotations.types.CHAR;
import org.dzh.bytesutil.annotations.types.RAW;
import org.dzh.bytesutil.annotations.types.UserDefined;
import org.dzh.bytesutil.converters.auxiliary.exceptions.ExtendedConversionException;
import org.dzh.bytesutil.converters.auxiliary.exceptions.UnsatisfiedConstraintException;

public class Utils {
    
    private Utils() {}
    
    public static Charset charsetForSerializingCHAR(FieldInfo ctx,Object self) {
        Charset cs = ctx.charset;
        if(cs==null) {
            cs = (Charset) ctx.charsetHandler.handleSerialize(ctx.name,self);
        }
        return cs;
    }
    
    public static Charset charsetForDeserializingCHAR(FieldInfo ctx,Object self, MarkableInputStream is) {
        Charset cs = ctx.charset;
        if(cs==null) {
            cs = ctx.charsetHandler.handleDeserialize(ctx.name,self,is);
        }
        return cs;
    }
    
    public static int lengthForSerializingCHAR(FieldInfo ctx,Object self){
        int length = ctx.annotation(CHAR.class).value();
        if(length<0) {
            length = lengthForSerializingLength(ctx, self);
        }
        return length;
    }
    
    public static int lengthForDeserializingCHAR(FieldInfo ctx,Object self, MarkableInputStream bis){
        int length = ctx.annotation(CHAR.class).value();
        if(length<0) {
            length = lengthForDeserializingLength(ctx,self,bis);
        }
        return length;
    }
    
    public static int lengthForSerializingUserDefinedType(FieldInfo ctx,Object self){
        int length = ctx.annotation(UserDefined.class).length();
        if(length<0) {
            length = lengthForSerializingLength(ctx, self);
        }
        return length;
    }
    
    public static int lengthForDeserializingUserDefinedType(FieldInfo ctx,Object self, MarkableInputStream bis) {
        int length = ctx.annotation(UserDefined.class).length();
        if(length<0) {
            length = lengthForDeserializingLength(ctx,self,bis);
        }
        return length;
    }
    
    public static int lengthForSerializingRAW(FieldInfo ctx,Object self) {
        int length = ctx.annotation(RAW.class).value();
        if(length<0) {
            length = lengthForSerializingLength(ctx, self);
        }
        return length;
    }
    
    public static int lengthForDeserializingRAW(FieldInfo ctx,Object self, MarkableInputStream bis) {
        int length = ctx.annotation(RAW.class).value();
        if(length<0) {
            length = lengthForDeserializingLength(ctx,self,bis);
        }
        return length;
    }
    
    public static int lengthForSerializingLength(FieldInfo ctx,Object self) throws IllegalArgumentException {
        Integer length = ctx.length;
        if(length<0 && ctx.lengthHandler!=null) {
            length = ctx.lengthHandler.handleSerialize(ctx.name, self);
        }
        return length;
    }
    
    public static int lengthForDeserializingLength(FieldInfo ctx,Object self, MarkableInputStream bis) {
        Integer length = ctx.length;
        if(length<0 && ctx.lengthHandler!=null) {
            length = ctx.lengthHandler.handleDeserialize(ctx.name, self, bis);
        }
        return length;
    }
    
    public static int lengthForSerializingListLength(FieldInfo ctx,Object self){
        Integer length = ctx.listLength;
        if(length<0 && ctx.listLengthHandler!=null) {
            length = ctx.listLengthHandler.handleSerialize(ctx.name, self);
        }
        return length;
    }
    
    public static int lengthForList(FieldInfo fi,Object self){
        /*
         * ListLength first
         * If the component dataType is not a dynamic-length data dataType, both listLength or Length may be present,
         * if the component dataType is a dynamic-length data dataType, then listLenght must be present or an exception 
         * will be thrown by ClassInfo
         */
        int length = Utils.lengthForSerializingListLength(fi, self);
        if(length==-1)
            length = Utils.lengthForSerializingLength(fi, self);
        return length;
    }
    
    public static int lengthForDeserializingListLength(FieldInfo ctx,Object self, MarkableInputStream bis){
        Integer length = ctx.listLength;
        if(length<0 && ctx.listLengthHandler!=null) {
            length = ctx.listLengthHandler.handleDeserialize(ctx.name, self, bis);
        }
        return length;
    }
    
    public static int[] checkAndConvertToBCD(long val, int bcdBytes) {
        if(val<0) {
            throw new UnsatisfiedConstraintException(String.format("negative number [%d] cannot be stored as BCD",val))
                        .withSiteAndOrdinal(Utils.class, 5);
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
                        .withSiteAndOrdinal(Utils.class, 6);
        }
        return values;
    }
    
    public static final void checkBCDLength(String src, int length) {
        if((src.length()>>1)!=length) {
            throw new UnsatisfiedConstraintException(String.format(
                    "length of string should be [%d] (double long as declared BCD value), but it was [%d]", length*2, src.length()))
                        .withSiteAndOrdinal(Utils.class, 7);
        }
    }
    
    public static final void checkRangeInContext(DataType type,long val,FieldInfo ctx) throws ConversionException {
        String error;
        if((error = type.checkRange(val, ctx.unsigned))!=null) {
            throw new ExtendedConversionException(ctx.enclosingEntityClass, ctx.name, error)
                        .withSiteAndOrdinal(Utils.class, 1);
        }
    }
    
    public static final void serializeBCD(String str, OutputStream dest, FieldInfo ctx, Object self) 
            throws ConversionException, IOException {
        Utils.checkBCDLength(str, ctx.annotation(BCD.class).value());
        int len = str.length();
        int[] values = new int[len];
        for(int i=0;i<len;++i) {
            char c = str.charAt(i);
            if(!(c>='0' && c<='9')) {
                throw new ExtendedConversionException(ctx,
                        "only numeric value is supported in bcd")
                            .withSiteAndOrdinal(Utils.class, 4);
            }
            values[i] = c-'0';
        }
        StreamUtils.writeBCD(dest, values);
    }
    
    public static final void serializeAsCHAR(String str, OutputStream dest, FieldInfo ctx, Object self)
            throws ConversionException, IOException {
        Charset cs = Utils.charsetForSerializingCHAR(ctx, self);
        byte[] bytes = str.getBytes(cs);
        byte[] ending = ctx.endsWith;
        int length = Utils.lengthForSerializingCHAR(ctx, self);
        if(length<0) {
            if(ending==null) {
                length = bytes.length;
                StreamUtils.writeIntegerOfType(dest, ctx.lengthType(), length, ctx.bigEndian);
                StreamUtils.writeBytes(dest, bytes);
            }else {
                StreamUtils.writeBytes(dest, bytes);
                StreamUtils.writeBytes(dest, ending);
            }
        }else if(length!=bytes.length) {
            throw new ExtendedConversionException(ctx,
                    String.format("length of string representation [%s] does not equals with declared CHAR length [%d]"
                                ,str,length))
                        .withSiteAndOrdinal(Utils.class, 2);
        }else {
            StreamUtils.writeBytes(dest, bytes);
        }
    }
    
    public static final void serializeAsCHAR(long val, OutputStream dest, FieldInfo ctx, Object self)
            throws ConversionException, IOException {
        if(val<0) {
            //implementation choice
            throw new ExtendedConversionException(ctx,"negative number should not be converted to CHAR")
                        .withSiteAndOrdinal(Utils.class, 0);
        }
        String str = Long.toString(val);
        serializeAsCHAR(str,dest,ctx,self);
    }
    
    public static final void serializeAsCHAR(BigInteger val, OutputStream dest, FieldInfo ctx, Object self)
            throws ConversionException, IOException {
        if(val.compareTo(BigInteger.ZERO)<0) {
            //implementation choice
            throw new ExtendedConversionException(ctx,"negative number should not be converted to CHAR")
                        .withSiteAndOrdinal(Utils.class, 8);
        }
        String str = val.toString();
        serializeAsCHAR(str,dest,ctx,self);
    }
    
    public static final long deserializeAsCHAR(
            MarkableInputStream is, FieldInfo ctx, Object self, DataType type)
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
            throw new ExtendedConversionException(ctx, error)
                    .withSiteAndOrdinal(Utils.class, 3);
        }
        if(type!=null) {
            Utils.checkRangeInContext(type, ret, ctx);
        }
        return ret;
    }
    
    public static final BigInteger deserializeAsBigCHAR(
            MarkableInputStream is, FieldInfo ctx, Object self, DataType type)
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
            throw new ExtendedConversionException(ctx, error)
                    .withSiteAndOrdinal(Utils.class, 9);
        }
        //no need to check the range here
        //as we do not permit negative sign in the character streams
        //and BigInteger never overflows a BigInteger except it overflows the memory
        return ret2==null ? BigInteger.valueOf(ret) : ret2;
    }
    
    private static byte[] readBytesForDeserializingCHAR(
            MarkableInputStream is, FieldInfo ctx, Object self, DataType type) throws IOException, ConversionException {
        int length = Utils.lengthForDeserializingCHAR(ctx, self, is);
        if(length<0) {
            length = StreamUtils.readIntegerOfType(is, ctx.lengthType(), ctx.bigEndian);
        }
        byte[] numChars = StreamUtils.readBytes(is, length);
        /*
         * such strings causes asymmetry between serialization and deserialization. it
         * is possible to avoid this problem by using written-ahead length, however such
         * use case is rare so it is better prevent deserialization from such strings to
         * a numeric dataType explicitly rather than later cause errors that are hard to
         * detect.
         */
        if(numChars.length>1 && numChars[0]=='0') {
            throw new ExtendedConversionException(ctx, "streams contains numeric string with leading zero")
                    .withSiteAndOrdinal(Utils.class, 10);
        }
        return numChars;
    }
    
    public static final SimpleDateFormat getThreadLocalDateFormatter(String datePattern) {
        ThreadLocal<SimpleDateFormat> tl = formatterMap.get(datePattern);
        if (tl == null) {
            tl = new _TLFormatter(datePattern);
            formatterMap.put(datePattern, tl);
        }
        return tl.get();
    }
    
    private static final ConcurrentHashMap<String, ThreadLocal<SimpleDateFormat>> formatterMap = new ConcurrentHashMap<>();

    private static final class _TLFormatter extends ThreadLocal<SimpleDateFormat> {
        private String p;

        public _TLFormatter(String p) {
            this.p = p;
        }

        @Override
        protected SimpleDateFormat initialValue() {
            //lenient in former versions
            SimpleDateFormat ret = new SimpleDateFormat(p);
            ret.setLenient(false);
            return ret;
        }
    }
        
    static UnsatisfiedConstraintException forContext(Class<?> entity, String field, String error) {
        StringBuilder ret = new StringBuilder();
        if(entity!=null) {
            ret.append("Entity:"+entity);
        }
        if(field!=null) {
            if(ret.length()>0) {
                ret.append(", ");
            }
            ret.append("Field:").append(field);
        }
        if(ret.length()>0) {
            ret.append(", ");
        }
        ret.append("Error:").append(error);
        return new UnsatisfiedConstraintException(ret.toString());
    }
}
