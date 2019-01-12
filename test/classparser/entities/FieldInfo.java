package classparser.entities;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.Length;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Unsigned;
import org.dzh.bytesutil.annotations.modifiers.Variant;
import org.dzh.bytesutil.annotations.types.SHORT;
import org.dzh.bytesutil.converters.auxiliary.DataType;
import org.dzh.bytesutil.converters.auxiliary.EntityHandler;

import classparser.entities.attributeinfo.AttributeInfo;
import classparser.entities.cpinfo.CpInfo;
import classparser.entities.cpinfo.info.CONSTANT_Utf8_info;

@Unsigned
public class FieldInfo extends DataPacket{
	@Order(0)
	@SHORT
	public int accessFlags;
	@Order(1)
	@SHORT
	public long nameIndex;
	@Order(2)
	@SHORT
	public long descriptorIndex;
	@Order(3)
	@Length(type=DataType.SHORT)
	@Variant(AttributeInfoHandler.class)
	public List<AttributeInfo> attributes;
	
	private List<CpInfo> constantPool;
	public FieldInfo(List<CpInfo> constantPool) {
		this.constantPool = constantPool;
	}
	
	public static class AttributeInfoHandler extends EntityHandler{
		@Override
		public DataPacket handle0(String fieldName, Object entity, InputStream is) throws IOException {
			FieldInfo info = (FieldInfo)entity;
			return new AttributeInfo(Collections.unmodifiableList(info.constantPool));
		}
		
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		String name = ((CONSTANT_Utf8_info)constantPool.get((int)nameIndex-1).info).bytes;
		sb.append(">>"+name+"\n");
		sb.append("accessFlags:"+Long.toBinaryString(accessFlags));sb.append("\n");
		sb.append("descriptorIndex:").append(descriptorIndex);sb.append("\n");
		sb.append("attributes:");sb.append("\n");
		sb.append(attributes);
		return sb.toString();
	}
}
