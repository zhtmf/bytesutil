package test.general.entities;

import java.io.InputStream;
import java.nio.charset.Charset;

import io.github.zhtmf.converters.auxiliary.ModifierHandler;

public class PropertyHandler2 extends ModifierHandler<Charset> {

    @Override
    public Charset handleDeserialize0(String fieldName,Object entity, InputStream is){
        MyEntity sb = (MyEntity)entity;
        return sb.a>0 ? Charset.forName("UTF-8") : Charset.forName("GBK");
    }

    @Override
    public Charset handleSerialize0(String fieldName,Object entity){
        MyEntity sb = (MyEntity)entity;
        return sb.a>0 ? Charset.forName("UTF-8") : Charset.forName("GBK");
    }

}
