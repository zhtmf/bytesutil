package io.github.zhtmf.entities;

import java.io.InputStream;
import java.nio.charset.Charset;

import io.github.zhtmf.converters.auxiliary.ModifierHandler;

public class PropertyHandler1 extends ModifierHandler<Charset> {

    @Override
    public Charset handleDeserialize0(String fieldName,Object entity, InputStream is){
        Sub2 sb = (Sub2)entity;
        return sb.type2==0 ? Charset.forName("UTF-8") : Charset.forName("GBK");
    }

    @Override
    public Charset handleSerialize0(String fieldName,Object entity){
        Sub2 sb = (Sub2)entity;
        return sb.type2==0 ? Charset.forName("UTF-8") : Charset.forName("GBK");
    }

}
