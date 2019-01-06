package classparser.entities.attributeinfo.info;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.Length;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Unsigned;
import org.dzh.bytesutil.annotations.modifiers.Variant;
import org.dzh.bytesutil.annotations.types.RAW;
import org.dzh.bytesutil.annotations.types.SHORT;
import org.dzh.bytesutil.converters.auxiliary.DataType;
import org.dzh.bytesutil.converters.auxiliary.EntityHandler;

import classparser.entities.attributeinfo.AttributeInfo;
import classparser.entities.cpinfo.CpInfo;

@Unsigned
public class Code extends DataPacket{
	@Order(0)
	@SHORT
	public int maxStack;
	@Order(1)
	@SHORT
	public int maxLocals;
	@Order(2)
	@RAW
	@Length(type=DataType.INT)
	public byte[] code;
	@Order(3)
	@Length(type=DataType.SHORT)
	public List<ExceptionTable> exceptionTable;
	@Order(4)
	@Length(type=DataType.SHORT)
	@Variant(AttributeInfoHandler2.class)
	public List<AttributeInfo> attributes;
	
	private List<CpInfo> constantPool;
	public Code(List<CpInfo> constantPool) {
		this.constantPool = constantPool;
	}
	
	public static class AttributeInfoHandler2 extends EntityHandler{
		@Override
		public DataPacket handle0(String fieldName, Object entity, InputStream is) throws IOException {
			Code info = (Code)entity;
			return new AttributeInfo(Collections.unmodifiableList(info.constantPool));
		}
		
	}
}
