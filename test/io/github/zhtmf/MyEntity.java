package io.github.zhtmf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import io.github.zhtmf.annotations.modifiers.BigEndian;
import io.github.zhtmf.annotations.modifiers.CHARSET;
import io.github.zhtmf.annotations.modifiers.DatePattern;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.LittleEndian;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Script;
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
import io.github.zhtmf.converters.auxiliary.ModifierHandler;

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
    @CHARSET(scripts = @Script("entity.a>0 ? 'UTF-8' : 'GBK'"))
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
    @CHARSET(scripts = @Script("entity.a>0 ? 'UTF-8' : 'GBK'"))
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
    
    public static class EntityHandler1 extends EntityHandler{

        @Override
        public DataPacket handle0(String fieldName, Object entity, InputStream is) throws IOException {
            //dataType
            int b = is.read();
            if(b==1) {
                return new Sub1();
            }else if(b==2) {
                return new Sub2();
            }
            throw new Error("unknown b value:"+b);
        }
    }
    
    public static class LengthHandler extends ModifierHandler<Integer> {

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
    
    public static class Base extends DataPacket{
        @Order(0)
        @BYTE
        @Unsigned
        public int type;
        @Order(1)
        @CHAR(8)
        public String time;
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((time == null) ? 0 : time.hashCode());
            result = prime * result + type;
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Base other = (Base) obj;
            if (time == null) {
                if (other.time != null)
                    return false;
            } else if (!time.equals(other.time))
                return false;
            if (type != other.type)
                return false;
            return true;
        }
    }
    
    public static class Sub1 extends Base {
        @Order(0)
        @INT
        @Signed
        public int field1;
        @Order(1)
        @SHORT
        public int field2;
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + field1;
            result = prime * result + field2;
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!super.equals(obj))
                return false;
            if (getClass() != obj.getClass())
                return false;
            Sub1 other = (Sub1) obj;
            if (field1 != other.field1)
                return false;
            if (field2 != other.field2)
                return false;
            return true;
        }
    }
    
    public static class Sub2 extends Base {
        @Order(0)
        @CHAR(6)
        public String str1;
        @Order(1)
        @CHAR(1)
        public String str2;
        @Order(2)
        @CHAR(2)
        public String str3;
        @Order(3)
        @BYTE
        @Unsigned
        public int type2;
        @Order(4)
        @CHAR(10)
        @CHARSET(scripts = @Script("entity.type2==0 ? 'UTF-8' : 'GBK'"))
        public String str4;
        
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + ((str1 == null) ? 0 : str1.hashCode());
            result = prime * result + ((str2 == null) ? 0 : str2.hashCode());
            result = prime * result + ((str3 == null) ? 0 : str3.hashCode());
            result = prime * result + ((str4 == null) ? 0 : str4.hashCode());
            result = prime * result + type2;
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!super.equals(obj))
                return false;
            if (getClass() != obj.getClass())
                return false;
            Sub2 other = (Sub2) obj;
            if (str1 == null) {
                if (other.str1 != null)
                    return false;
            } else if (!str1.equals(other.str1))
                return false;
            if (str2 == null) {
                if (other.str2 != null)
                    return false;
            } else if (!str2.equals(other.str2))
                return false;
            if (str3 == null) {
                if (other.str3 != null)
                    return false;
            } else if (!str3.equals(other.str3))
                return false;
            if (str4 == null) {
                if (other.str4 != null)
                    return false;
            } else if (!str4.equals(other.str4))
                return false;
            if (type2 != other.type2)
                return false;
            return true;
        }
    }
    
    public static class SubEntity extends DataPacket{
        
        public byte carryOver;
        
        @Order(0)
        @INT
        @Signed
        public int integerA;
        
        @Order(1)
        @CHAR(15)
        @CHARSET("GBK")
        public String strB;
        
        public SubEntity(int i, float j) {
            //no-op
        }

        public SubEntity(int integerA, String strB) {
            this.integerA = integerA;
            this.strB = strB;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + integerA;
            result = prime * result + ((strB == null) ? 0 : strB.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            SubEntity other = (SubEntity) obj;
            if (integerA != other.integerA)
                return false;
            if (strB == null) {
                if (other.strB != null)
                    return false;
            } else if (!strB.equals(other.strB))
                return false;
            return true;
        }
    }
    
    @BigEndian
    public static class WeirdEntity extends DataPacket{

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
}