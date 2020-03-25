package io.github.zhtmf.converters;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class Converters {
    private Converters() {}
    //all built-in converters that are initialized on startup
    //this map is made unmodifiable to ensure thread safety
    public static final Map<Class<?>,Converter<?>> converters;
    public static final ListConverter listConverter = new ListConverter();
    public static final DataPacketConverter dataPacketConverter = new DataPacketConverter();
    public static final UserDefinedTypeConverter userDefinedTypeConverter = new UserDefinedTypeConverter();
    static {
        Map<Class<?>,Converter<?>> tmp = new HashMap<>();
        tmp.put(Byte.class, new ByteConverter());
        tmp.put(byte.class, tmp.get(Byte.class));
        tmp.put(Short.class, new ShortConverter());
        tmp.put(short.class, tmp.get(Short.class));
        tmp.put(Integer.class, new IntegerConverter());
        tmp.put(int.class, tmp.get(Integer.class));
        tmp.put(String.class, new StringConverter());
        tmp.put(Character.class, new CharConverter());
        tmp.put(char.class, tmp.get(Character.class));
        tmp.put(byte[].class, new ByteArrayConverter());
        tmp.put(int[].class, new IntArrayConverter());
        tmp.put(java.util.Date.class, new DateConverter());
        tmp.put(Long.class, new LongConverter());
        tmp.put(long.class, tmp.get(Long.class));
        tmp.put(BigInteger.class, new BigIntegerConverter());
        tmp.put(Boolean.class, new BooleanConverter());
        tmp.put(boolean.class, tmp.get(Boolean.class));
        tmp.put(Double.class, new DoubleConverter());
        tmp.put(double.class, tmp.get(Double.class));
        converters = Collections.unmodifiableMap(tmp);
    }
}
