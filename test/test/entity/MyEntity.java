package test.entity;

import java.util.Date;
import java.util.List;

import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.BigEndian;
import org.dzh.bytesutil.annotations.modifiers.CHARSET;
import org.dzh.bytesutil.annotations.modifiers.DatePattern;
import org.dzh.bytesutil.annotations.modifiers.Length;
import org.dzh.bytesutil.annotations.modifiers.LittleEndian;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Signed;
import org.dzh.bytesutil.annotations.modifiers.Unsigned;
import org.dzh.bytesutil.annotations.modifiers.Variant;
import org.dzh.bytesutil.annotations.types.BCD;
import org.dzh.bytesutil.annotations.types.BYTE;
import org.dzh.bytesutil.annotations.types.CHAR;
import org.dzh.bytesutil.annotations.types.INT;
import org.dzh.bytesutil.annotations.types.RAW;
import org.dzh.bytesutil.annotations.types.SHORT;

import test.hierarchy.Base;
import test.hierarchy.EntityHandler1;
import test.hierarchy.LengthHandler;
import test.hierarchy.PropertyHandler2;

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
	public List<SubEntity> subEntityList;
	
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