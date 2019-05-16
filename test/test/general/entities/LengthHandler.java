package test.general.entities;

import java.io.InputStream;

import org.dzh.bytesutil.converters.auxiliary.ModifierHandler;

public class LengthHandler extends ModifierHandler<Integer> {

    @Override
    public Integer handleDeserialize0(String fieldName,Object entity, InputStream is){
        MyEntity sb = (MyEntity)entity;
        if(fieldName.equals("entityList2")) {
            return sb.unusedLength;
        }else if(fieldName.equals("anotherBytes")) {
            return sb.bytes2Len;
        }
        return null;
    }

    @Override
    public Integer handleSerialize0(String fieldName,Object entity){
        MyEntity sb = (MyEntity)entity;
        if(fieldName.equals("entityList2")) {
            return sb.unusedLength;
        }else if(fieldName.equals("anotherBytes")) {
            return sb.bytes2Len;
        }
        return null;
    }

}
