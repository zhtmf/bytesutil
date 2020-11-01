package io.github.zhtmf.converters.auxiliary;

import org.junit.Assert;
import org.junit.Test;

import io.github.zhtmf.converters.auxiliary.ModifierHandler.OffsetAccess;

public class TestMakeJacocoHappy {
    @Test
    public void test0() throws Exception {
        OffsetAccess access = new OffsetAccess() {
            
            @Override
            public int offset() {
                return 1;
            }

            @Override
            public Object context() {
                return null;
            }
        };
        if(ModifierHandler.access!=null) {
            ModifierHandler.setAccess(access);
            Assert.assertTrue(access != ModifierHandler.access);
        }else {
            ModifierHandler.setAccess(access);
            Assert.assertTrue(access == ModifierHandler.access);
            ModifierHandler.setAccess(new OffsetAccess() {
                
                @Override
                public int offset() {
                    return 0;
                }

                @Override
                public Object context() {
                    return null;
                }
            });
            Assert.assertTrue(access == ModifierHandler.access);
        }
    }
}