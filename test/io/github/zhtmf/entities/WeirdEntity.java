package io.github.zhtmf.entities;

import java.io.InputStream;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.BigEndian;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.types.CHAR;
import io.github.zhtmf.annotations.types.RAW;
import io.github.zhtmf.converters.auxiliary.ModifierHandler;

@BigEndian
public class WeirdEntity extends DataPacket{

    @Order(0)
    @CHAR
    @Length
    public String char1;
    
    @Order(1)
    @CHAR
    @Length(12)
    public String char2;
    
    @Order(2)
    @CHAR
    @Length(handler=Handler.class)
    public String char3;
    
    @Order(3)
    @RAW(5)
    public byte[] bytearray1;
    
    @Order(4)
    @RAW
    @Length
    public byte[] bytearray2;
    
    @Order(5)
    @RAW
    @Length(4)
    public byte[] bytearray3;
    
    @Order(6)
    @RAW
    @Length(handler=Handler2.class)
    public byte[] bytearray4;
    
    public static class Handler extends ModifierHandler<Integer>{

        @Override
        public Integer handleDeserialize0(String fieldName, Object entity, InputStream is) {
            WeirdEntity ae = (WeirdEntity)entity;
            return ae.char1.length();
        }

        @Override
        public Integer handleSerialize0(String fieldName, Object entity) {
            WeirdEntity ae = (WeirdEntity)entity;
            return ae.char1.length();
        }
        
    }
    
    public static class Handler2 extends ModifierHandler<Integer>{

        @Override
        public Integer handleDeserialize0(String fieldName, Object entity, InputStream is) {
            WeirdEntity ae = (WeirdEntity)entity;
            return ae.bytearray1.length;
        }

        @Override
        public Integer handleSerialize0(String fieldName, Object entity) {
            WeirdEntity ae = (WeirdEntity)entity;
            return ae.bytearray1.length;
        }
        
    }
}
