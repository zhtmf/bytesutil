package io.github.zhtmf.entities;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.BigEndian;
import io.github.zhtmf.annotations.modifiers.CHARSET;
import io.github.zhtmf.annotations.modifiers.DatePattern;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.LittleEndian;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Signed;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.modifiers.Variant;
import io.github.zhtmf.annotations.types.BCD;
import io.github.zhtmf.annotations.types.BYTE;
import io.github.zhtmf.annotations.types.CHAR;
import io.github.zhtmf.annotations.types.INT;
import io.github.zhtmf.annotations.types.RAW;
import io.github.zhtmf.annotations.types.SHORT;
import io.github.zhtmf.converters.auxiliary.EntityHandler;

@LittleEndian
@Unsigned
public class MyEntity extends DataPacket{
    @BYTE
    @Signed
    @Order(0)
    public byte a;
    @BYTE
    @Signed
    @Order(1)
    public byte b;
    @SHORT
    @Signed
    @BigEndian
    @Order(2)
    public short c;
    
    @BYTE
    @BigEndian
    @Order(3)
    public int d;
    
    @SHORT
    @Order(4)
    public int e;
    
    @INT
    @Signed
    @Order(5)
    public int f;
    
    @BYTE
    @Signed
    @Order(Order.LAST)
    public byte z;
    
    @CHAR(6)
    @Order(6)
    public String str;
    
    @CHAR(6)
    @Order(7)
    @CHARSET("GB2312")
    public String str2;
    
    @BCD(4)
    @Order(8)
    public String bcd;
    
    @CHAR(1)
    @Order(9)
    public char status;
    
    @CHAR(1)
    @Order(10)
    public Character status2;
    
    @Order(11)
    @Variant(SubEntityHandler.class)
    public SubEntity sub;
    
    @Order(12)
    @Variant(EntityHandler1.class)
    public Base variantEntity;
    
    @Order(13)
    @Variant(EntityHandler1.class)
    public Base anotherEntity;
    
    @Order(14)
    @CHAR(4)
    @Length
    @CHARSET(handler=PropertyHandler2.class)
    public List<String> strList;
    
    @Order(15)
    @Length
    @Variant(SubEntityHandler.class)
    public List<SubEntity> subEntityList;
    
    public static final class SubEntityHandler extends EntityHandler{

        @Override
        public DataPacket handle0(String fieldName, Object entity, InputStream is) throws IOException {
            SubEntity ret = new SubEntity(3, 4.0f);
            ret.carryOver = ((MyEntity)entity).a;
            return ret;
        }
        
    }
    
    @Order(16)
    @CHAR(4)
    @Length(5)
    @CHARSET(handler=PropertyHandler2.class)
    public List<String> list3;
    
    @Order(17)
    @BYTE
    @Unsigned
    public int unusedLength;
    
    @Order(18)
    @Length(handler=LengthHandler.class)
    public List<SubEntity> entityList2;
    
    @Order(19)
    @RAW
    @Length
    public byte[] bytes;
    
    @Order(20)
    @RAW(3)
    @Length
    public List<byte[]> byteList;
    
    @Order(21)
    @BYTE
    @Unsigned
    public int bytes2Len;
    
    @Order(22)
    @RAW
    @Length(handler=LengthHandler.class)
    public byte[] anotherBytes;
    
    @Order(23)
    @BCD(7)
    @DatePattern("yyyyMMddHHmmss")
    public Date date;
    
    @Order(24)
    @CHAR(14)
    @DatePattern("yyyyMMddHHmmss")
    public Date date2;
    
    @Order(25)
    @INT
    public long veryLong;
    
    @Order(26)
    public WeirdEntity we;
    
}