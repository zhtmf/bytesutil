package io.github.zhtmf.converters;

import java.lang.reflect.Constructor;

import org.junit.Test;

public class MakeJacocoHappy {
    @Test
    public void test0() throws Exception {
        @SuppressWarnings("rawtypes")
        Constructor c = Converters.class.getDeclaredConstructor();
        c.setAccessible(true);
        c.newInstance();
    }
}
