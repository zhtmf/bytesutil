package io.github.zhtmf.converters;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

import io.github.zhtmf.annotations.types.UserDefined;
import io.github.zhtmf.converters.auxiliary.DataType;

enum DataTypeOperations{
    BYTE {
        @Override
        Class<? extends Annotation> annotationClassOfThisType() {
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
        Class<? extends Annotation> annotationClassOfThisType() {
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
        Class<? extends Annotation> annotationClassOfThisType() {
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
        Class<? extends Annotation> annotationClassOfThisType() {
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
                ;
        }
    },
    INT5{
        @Override
        Class<? extends Annotation> annotationClassOfThisType() {
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
                ;
        }
    },
    INT6{
        @Override
        Class<? extends Annotation> annotationClassOfThisType() {
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
                ;
        }
    },
    INT7{
        @Override
        Class<? extends Annotation> annotationClassOfThisType() {
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
                ;
        }
    },
    BCD {
        @Override
        Class<? extends Annotation> annotationClassOfThisType() {
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
        Class<? extends Annotation> annotationClassOfThisType() {
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
        Class<? extends Annotation> annotationClassOfThisType() {
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
        Class<? extends Annotation> annotationClassOfThisType() {
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
        public String checkRange(long val, FieldInfo ctx) {
            //always expect no error
            //as java long dataType cannot store an unsigned 64-bit value
            return null;
        }
        public String checkRange(long val, boolean unsigned) {
            return null;
        }
        @Override
        public String checkRange(BigInteger val, FieldInfo ctx) {
            if(ctx.unsigned) {
                return (val.compareTo(BigInteger.ZERO)>=0 && val.compareTo(UNSIGNED_LONG_MAX)<=0) ? null : 
                    String.format("value [%s] cannot be stored as unsigned 8-byte integer value",val.toString());
            }
            return (val.compareTo(SIGNED_LONG_MIN)>=0 && val.compareTo(SIGNED_LONG_MAX)<=0) ? null : 
                String.format("value [%s] cannot be stored as signed 8-byte integer value",val.toString());
        }
        private final BigInteger SIGNED_LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);
        private final BigInteger SIGNED_LONG_MIN = BigInteger.valueOf(Long.MIN_VALUE);
        private final BigInteger UNSIGNED_LONG_MAX = new BigInteger(Long.MAX_VALUE+"").multiply(new BigInteger("2"));
    }
    ,USER_DEFINED{

        @Override
        Class<? extends Annotation> annotationClassOfThisType() {
            return UserDefined.class;
        }

        @Override
        boolean supports(Class<?> javaType) {
            //always true for user-defined types
            return true;
        }
        
    }
    ,BIT{

        @Override
        Class<? extends Annotation> annotationClassOfThisType() {
            return io.github.zhtmf.annotations.types.Bit.class;
        }

        @Override
        boolean supports(Class<?> javaType) {
            return javaType == boolean.class
                    || javaType == Boolean.class
                    || javaType == java.lang.Byte.class
                    || javaType == byte.class
                    || javaType.isEnum()
                    ;
        }
        
        @Override
        Class<?> mappedEnumFieldClass() {
            return Byte.class;
        }
        
        @Override
        public String checkRange(long val, FieldInfo ctx) {
            return (val >=0 && val <= BITS_INT_MAXIMUMS[ctx.bitCount]) ? null : 
                String.format("value [%s] cannot be stored as signed %s-bit integer value",val, ctx.bitCount);
        }
    }
    ,FIXED{

        @Override
        Class<? extends Annotation> annotationClassOfThisType() {
            return io.github.zhtmf.annotations.types.Fixed.class;
        }

        @Override
        boolean supports(Class<?> javaType) {
           return javaType == double.class
                || javaType == Double.class
                || javaType == BigDecimal.class
                ;
        }
        
        @Override
        public String checkRange(BigDecimal d, FieldInfo ctx) {
            boolean signed = ctx.signed;
            int m = ctx.fixedNumberLengths[0] * 8;
            int n = ctx.fixedNumberLengths[1] * 8;
            BigDecimal min = signed ? TWO.pow(m - 1).negate() : BigDecimal.ZERO;
            BigDecimal subtrahend = n >= BASE2_NEGATIVE_EXPONENT.length 
                            ? BigDecimal.ONE.divide(TWO.pow(n), MathContext.DECIMAL128)
                            : BASE2_NEGATIVE_EXPONENT[n];
            BigDecimal max = signed ? TWO.pow(m - 1).subtract(subtrahend, MathContext.DECIMAL128) 
                                    : TWO.pow(m).subtract(subtrahend, MathContext.DECIMAL128) ;
            if(d.compareTo(min) < 0 || d.compareTo(max) > 0) {
                return "this number overflows valid fixed point range [" + min + ", " + max + "]";
            }
            
            return null;
        }
    }
    ,FLOAT{
    	@Override
        Class<? extends Annotation> annotationClassOfThisType() {
            return io.github.zhtmf.annotations.types.FLOAT.class;
        }

        @Override
        boolean supports(Class<?> javaType) {
           return javaType == float.class
                || javaType == Float.class
                || javaType == double.class
                || javaType == Double.class
                ;
        }
    }
    ,DOUBLE{
    	@Override
        Class<? extends Annotation> annotationClassOfThisType() {
            return io.github.zhtmf.annotations.types.DOUBLE.class;
        }

        @Override
        boolean supports(Class<?> javaType) {
           return  javaType == double.class
                || javaType == Double.class
                ;
        }
    }
    ;
    
    Class<?> mappedEnumFieldClass(){throw new UnsupportedOperationException();}
    abstract Class<? extends Annotation> annotationClassOfThisType();
    abstract boolean supports(Class<?> javaType);
    
    public int size() {throw new UnsupportedOperationException();}
    
    public String checkRange(long val, FieldInfo ctx) {
        return checkRange(val, ctx.unsigned);
    }
    
    //this method is solely used by checking length values
    //which directly checking an unsigned/signed value regardless of signed-ness of the original FieldInfo
    public String checkRange(long val, boolean unsigned) {
        int sz = size()-1;
        if(unsigned) {
            if(val<0 || val>UNSIGNED_MAXIMUMS[sz]) {
                return "value " + val +" cannot be stored as unsigned "+(sz+1)+"-byte integer value";
            }
        }else {
            if(val<SIGNED_MINIMUMS[sz] || val>SIGNED_MAXIMUMS[sz]) {
                return "value " + val + " cannot be stored as signed "+(sz+1)+"-byte integer value";
            }
        }
        return null;
    }
    
    public String checkRange(BigDecimal val, FieldInfo ctx) {throw new UnsupportedOperationException();}
    
    public String checkRange(BigInteger val, FieldInfo ctx) {throw new UnsupportedOperationException();}
    
    public static DataTypeOperations of(DataType type) {
        return DataTypeOperations.valueOf(type.name());
    }
    
    //###########number limits###########
    
    private static final BigDecimal TWO = new BigDecimal("2");
    
    private static final long[] SIGNED_MAXIMUMS = {
       Byte.MAX_VALUE,
       Short.MAX_VALUE,
       (long) (pow(2, 3*8)/2-1),
       Integer.MAX_VALUE,
       (long) (pow(2, 5*8)/2-1),
       (long) (pow(2, 6*8)/2-1),
       (long) (pow(2, 7*8)/2-1),
       Long.MAX_VALUE
    };
    private static final long[] SIGNED_MINIMUMS = {
        -(long) (pow(2, 1*8)/2),
        -(long) (pow(2, 2*8)/2),
        -(long) (pow(2, 3*8)/2),
        -(long) (pow(2, 4*8)/2),
        -(long) (pow(2, 5*8)/2),
        -(long) (pow(2, 6*8)/2),
        -(long) (pow(2, 7*8)/2),
        Long.MIN_VALUE,
     };
    private static final long[] UNSIGNED_MAXIMUMS = {
         (long) (pow(2, 1*8)-1),
         (long) (pow(2, 2*8)-1),
         (long) (pow(2, 3*8)-1),
         (long) (pow(2, 4*8)-1),
         (long) (pow(2, 5*8)-1),
         (long) (pow(2, 6*8)-1),
         (long) (pow(2, 7*8)-1),
       //unavailable values are left blank
    };
    private static final long[] BITS_INT_MAXIMUMS = {
            -1,
            pow(2,1)-1,
            pow(2,2)-1,
            pow(2,3)-1,
            pow(2,4)-1,
            pow(2,5)-1,
            pow(2,6)-1,
            pow(2,7)-1,
            pow(2,8)-1,
    };
    
    private static final BigDecimal[] BASE2_NEGATIVE_EXPONENT = new BigDecimal[64];
    static {
        BASE2_NEGATIVE_EXPONENT[0] = BigDecimal.ONE;
        for(int p = 1;p<BASE2_NEGATIVE_EXPONENT.length;++p) {
            BASE2_NEGATIVE_EXPONENT[p] = BigDecimal.ONE.divide(
                    TWO.pow(p), MathContext.DECIMAL128);
        }
    }
    
    private static long pow(long base ,int power) {
        long tmp = base;
        while(power-->1) {
            base = base*tmp;
        }
        return base;
    }
}