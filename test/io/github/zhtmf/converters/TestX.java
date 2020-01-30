package io.github.zhtmf.converters;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import io.github.zhtmf.MyEntity;
import io.github.zhtmf.converters.auxiliary.ModifierHandler;

public class TestX {
    
private MyEntity entity = new MyEntity();
    
    @Before
    public void setValues() {
        entity.a = 120;
        entity.b = 110;
        entity.c = -1000;
        entity.d = 255;
        entity.e = 65535;
        entity.f = Integer.MIN_VALUE;
        entity.z = -100;
        entity.str = "abcdef";
        entity.str2 = "啊啊啊";
        entity.bcd = "20180909";
        entity.status = 'Y';
        entity.status2 = 'N';
        entity.sub = new MyEntity.SubEntity(30, "0123456789abcde");
        entity.strList = Arrays.asList("1234","2456","haha");
        entity.list3 = Arrays.asList("1234","abcd","defg","hijk","lmno");
        entity.subEntityList = Arrays.asList(new MyEntity.SubEntity(-3142, "0123456789abcde"),new MyEntity.SubEntity(5000,"0123456789fffff"));
        entity.unusedLength = 0;
        entity.entityList2 = new ArrayList<MyEntity.SubEntity>();
        entity.bytes = new byte[] {0x1,0x2};
        entity.byteList = Arrays.asList(new byte[] {0x1,0x2,0x5},new byte[]{0x3,0x4,0x6});
        entity.bytes2Len = 5;
        entity.anotherBytes = new byte[] {0x1,0x2,0x5,0x1,0x2};
        entity.date = new Date(0);
        entity.date2 = new Date(0); //milliseconds different?
        entity.veryLong = ((long)Integer.MAX_VALUE)*2;
        MyEntity.Sub2 s2 = new MyEntity.Sub2();
        s2.type = 2;
        s2.time = "19990101";
        s2.str1 = "123456";
        s2.str2 = "A";
        s2.str3 = "MF";
        s2.type2 = 1;
        s2.str4 = "hahahahaha";
        entity.variantEntity = s2;
        MyEntity.Sub1 s1 = new MyEntity.Sub1();
        s1.type = 1;
        s1.time = "20000202";
        s1.field1 = -350;
        s1.field2 = 30000;
        entity.anotherEntity = s1;
        
        MyEntity.WeirdEntity we = new MyEntity.WeirdEntity();
        we.char1 = "abcdef";
        we.char2 = "hahahahahaha";
        we.char3 = we.char1;
        we.bytearray1 = new byte[] {1,2,3,4,5};
        we.bytearray2 = new byte[] {1};
        we.bytearray3 = new byte[] {11,22,33,44};
        we.bytearray4 = we.bytearray1;
        entity.we = we;
    }
    
    public static class Handler extends ModifierHandler<Charset>{

        @Override
        public Charset handleDeserialize0(String fieldName, Object entity, InputStream in) throws IOException {
            MyEntity entity1 = (MyEntity)entity;
            return entity1.a>0 ? Charset.forName("UTF-8") : Charset.forName("GBK");
        }

        @Override
        public Charset handleSerialize0(String fieldName, Object entity) {
            MyEntity entity1 = (MyEntity)entity;
            return entity1.a>0 ? Charset.forName("UTF-8") : Charset.forName("GBK");
        }
        
    }
    
    @Test
    public void test3() throws Exception{
        {
            ModifierHandler<Charset> mod = new ScriptModifierHandler<Charset>("entity.a>0 ? 'UTF-8' : 'GBK'","entity.a>0 ? 'UTF-8' : 'GBK'",Charset.class) {
            };
            long st = System.currentTimeMillis();
            for(int i=0;i<10000;++i) {
                mod.handleSerialize0("abc", entity);
            }
            System.out.println(System.currentTimeMillis() - st);
        }
        {
            ModifierHandler<Charset> mod = new Handler();
            long st = System.currentTimeMillis();
            for(int i=0;i<10000;++i) {
                mod.handleSerialize0("abc", entity);
            }
            System.out.println(System.currentTimeMillis() - st);
        }
    }
}
