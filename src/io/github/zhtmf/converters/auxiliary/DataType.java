package io.github.zhtmf.converters.auxiliary;

import java.lang.annotation.Annotation;
import java.math.BigInteger;

import io.github.zhtmf.annotations.types.UserDefined;

public enum DataType{
    BYTE {
        @Override
        public Class<? extends Annotation> annotationClassOfThisType() {
            return io.github.zhtmf.annotations.types.BYTE.class;
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        Class<?> mappedEnumFieldClass() {
            return Long.class;
        }

        @Override
        boolean supports(Class<?> javaType) {
            return javaType == byte.class
                || javaType == short.class
                || javaType == int.class
                || javaType == long.class
                || javaType == boolean.class
                || javaType == Byte.class
                || javaType == Short.class
                || javaType == Integer.class
                || javaType == Long.class
                || javaType == Boolean.class
                /*
                 * checking for validity of this enum class 
                 * is done in EnumFieldInfo, not here.
                 */
                || javaType.isEnum();
        }
    },
    SHORT {
        @Override
        public Class<? extends Annotation> annotationClassOfThisType() {
            return io.github.zhtmf.annotations.types.SHORT.class;
        }

        @Override
        public int size() {
            return 2;
        }
        
        @Override
        Class<?> mappedEnumFieldClass() {
            return Long.class;
        }

        @Override
        boolean supports(Class<?> javaType) {
            return javaType == short.class
                || javaType == int.class
                || javaType == long.class
                || javaType == Short.class
                || javaType == Integer.class
                || javaType == Long.class
                || javaType.isEnum();
        }
    },
    INT {
        @Override
        public Class<? extends Annotation> annotationClassOfThisType() {
            return io.github.zhtmf.annotations.types.INT.class;
        }

        @Override
        public int size() {
            return 4;
        }
        
        @Override
        Class<?> mappedEnumFieldClass() {
            return Long.class;
        }

        @Override
        boolean supports(Class<?> javaType) {
            return javaType == int.class
                || javaType == long.class
                || javaType == Integer.class
                || javaType == Long.class
                || javaType == java.util.Date.class
                || javaType.isEnum();
        }
    },
    INT3{
        @Override
        public Class<? extends Annotation> annotationClassOfThisType() {
            return io.github.zhtmf.annotations.types.INT3.class;
        }
        @Override
        public int size() {
            return 3;
        }
        @Override
        boolean supports(Class<?> javaType) {
            return javaType == int.class
                || javaType == long.class
                || javaType == Integer.class
                || javaType == Long.class
                || javaType.isEnum();
        }
    },
    INT5{
        @Override
        public Class<? extends Annotation> annotationClassOfThisType() {
            return io.github.zhtmf.annotations.types.INT5.class;
        }
        @Override
        public int size() {
            return 5;
        }
        @Override
        boolean supports(Class<?> javaType) {
            return javaType == long.class
                || javaType == Long.class
                || javaType.isEnum();
        }
    },
    INT6{
        @Override
        public Class<? extends Annotation> annotationClassOfThisType() {
            return io.github.zhtmf.annotations.types.INT6.class;
        }
        @Override
        public int size() {
            return 6;
        }
        @Override
        boolean supports(Class<?> javaType) {
            return javaType == long.class
                || javaType == Long.class
                || javaType.isEnum();
        }
    },
    INT7{
        @Override
        public Class<? extends Annotation> annotationClassOfThisType() {
            return io.github.zhtmf.annotations.types.INT7.class;
        }
        @Override
        public int size() {
            return 7;
        }
        @Override
        boolean supports(Class<?> javaType) {
            return javaType == long.class
                || javaType == Long.class
                || javaType.isEnum();
        }
    },
    BCD {
        @Override
        public Class<? extends Annotation> annotationClassOfThisType() {
            return io.github.zhtmf.annotations.types.BCD.class;
        }

        @Override
        boolean supports(Class<?> javaType) {
            return javaType == byte.class
                || javaType == short.class
                || javaType == int.class
                || javaType == long.class
                || javaType == Byte.class
                || javaType == Short.class 
                || javaType == Integer.class
                || javaType == Long.class
                || javaType == String.class
                || javaType == java.util.Date.class
                ;
        }
    },
    RAW{

        @Override
        public Class<? extends Annotation> annotationClassOfThisType() {
            return io.github.zhtmf.annotations.types.RAW.class;
        }
        
        @Override
        boolean supports(Class<?> javaType) {
            return javaType == byte[].class
                || javaType == int[].class
                ;
        }
    },
    CHAR{

        @Override
        public Class<? extends Annotation> annotationClassOfThisType() {
            return io.github.zhtmf.annotations.types.CHAR.class;
        }
        
        @Override
        Class<?> mappedEnumFieldClass() {
            return String.class;
        }
        
        @Override
        boolean supports(Class<?> javaType) {
            return javaType == byte.class
                    || javaType == short.class
                    || javaType == int.class
                    || javaType == long.class
                    || javaType == Byte.class
                    || javaType == Short.class
                    || javaType == Integer.class
                    || javaType == Long.class
                    || javaType == String.class
                    || javaType == char.class
                    || javaType == Character.class
                    || javaType == java.util.Date.class
                    || javaType == java.math.BigInteger.class
                    || javaType.isEnum();
        }
    }
    ,
    LONG{
        @Override
        public Class<? extends Annotation> annotationClassOfThisType() {
            return io.github.zhtmf.annotations.types.LONG.class;
        }

        @Override
        boolean supports(Class<?> javaType) {
            return javaType == long.class
                || javaType == Long.class
                || javaType == java.util.Date.class
                || javaType == java.math.BigInteger.class
                || javaType.isEnum();
        }
        @Override
        public int size() {
            return 8;
        }
        @Override
        Class<?> mappedEnumFieldClass() {
            return Long.class;
        }
        @Override
        public String checkRange(long val, boolean unsigned) {
            //always expect no error
            //as java long dataType cannot store an unsigned 64-bit value
            return null;
        }
        @Override
        public String checkRange(BigInteger val, boolean unsigned) {
            if(unsigned) {
                return (val.compareTo(BigInteger.ZERO)>=0 && val.compareTo(UNSIGNED_LONG_MAX)<=0) ? null : 
                    String.format("val [%s] cannot be stored as unsigned 8-byte integer value",val.toString());
            }
            return (val.compareTo(SIGNED_LONG_MIN)>=0 && val.compareTo(SIGNED_LONG_MAX)<=0) ? null : 
                String.format("val [%s] cannot be stored as signed 8-byte integer value",val.toString());
        }
        private final BigInteger SIGNED_LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);
        private final BigInteger SIGNED_LONG_MIN = BigInteger.valueOf(Long.MIN_VALUE);
        private final BigInteger UNSIGNED_LONG_MAX = new BigInteger(Long.MAX_VALUE+"").multiply(new BigInteger("2"));
    }
    ,USER_DEFINED{

        @Override
        public Class<? extends Annotation> annotationClassOfThisType() {
            return UserDefined.class;
        }

        @Override
        boolean supports(Class<?> javaType) {
            //always true for user-defined types
            return true;
        }
        
    }
    ;
    Class<?> mappedEnumFieldClass(){throw new UnsupportedOperationException();}
    abstract public Class<? extends Annotation> annotationClassOfThisType();
    public int size() {throw new UnsupportedOperationException();}
    public String checkRange(long val, boolean unsigned) {
        int sz = size()-1;
        if(unsigned) {
            if(val<0 || val>UNSIGNED_MAXIMUMS[sz]) {
                return "val " + val +" cannot be stored as unsigned "+(sz+1)+"-byte integer value";
            }
        }else {
            if(val<SIGNED_MINIMUMS[sz] || val>SIGNED_MAXIMUMS[sz]) {
                return "val " + val + " cannot be stored as signed "+(sz+1)+"-byte integer value";
            }
        }
        return null;
    }
    public String checkRange(BigInteger val, boolean unsigned) {throw new UnsupportedOperationException();}
    abstract boolean supports(Class<?> javaType);
    //used by stream utils
    static final long[] SIGNED_MAXIMUMS = {
       Byte.MAX_VALUE,Short.MAX_VALUE,(0x00ffffff+1)/2-1,
       Integer.MAX_VALUE,(0x00ffffffffffL+1)/2-1,
       (0x00ffffffffffffL+1)/2-1,(0x00ffffffffffffffL+1)/2-1,Long.MAX_VALUE
    };
    private static final long[] SIGNED_MINIMUMS = {
        ((long)SIGNED_MAXIMUMS[0]+1)*-1,
        ((long)SIGNED_MAXIMUMS[1]+1)*-1,
        ((long)SIGNED_MAXIMUMS[2]+1)*-1,
        ((long)SIGNED_MAXIMUMS[3]+1)*-1,
        ((long)SIGNED_MAXIMUMS[4]+1)*-1,
        ((long)SIGNED_MAXIMUMS[5]+1)*-1,
        ((long)SIGNED_MAXIMUMS[6]+1)*-1,
        ((long)SIGNED_MAXIMUMS[7]+1)*-1,
     };
    private static final long[] UNSIGNED_MAXIMUMS = {
       ((long)SIGNED_MAXIMUMS[0])*2+1,
       ((long)SIGNED_MAXIMUMS[1])*2+1,
       ((long)SIGNED_MAXIMUMS[2])*2+1,
       ((long)SIGNED_MAXIMUMS[3])*2+1,
       ((long)SIGNED_MAXIMUMS[4])*2+1,
       ((long)SIGNED_MAXIMUMS[5])*2+1,
       ((long)SIGNED_MAXIMUMS[6])*2+1,
       //unavailable left blank
    };
}