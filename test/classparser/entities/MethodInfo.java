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
public class MethodInfo extends DataPacket{
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
	public MethodInfo(List<CpInfo> constantPool) {
		this.constantPool = constantPool;
	}
	
	public static class AttributeInfoHandler extends EntityHandler{
		@Override
		public DataPacket handle0(String fieldName, Object entity, InputStream is) throws IOException {
			MethodInfo info = (MethodInfo)entity;
			return new AttributeInfo(Collections.unmodifiableList(info.constantPool));
		}
		
	}
	
	@Override
	public String toString() {
		int index = (int)this.nameIndex;
		CpInfo relatedConstantInfo = this.constantPool.get(index-1);
		String name = ((CONSTANT_Utf8_info)relatedConstantInfo.info).bytes;
		return "Method "+name+":\n"+printAttributes();
	}
	
	private String printAttributes() {
		StringBuilder sb = new StringBuilder();
		if(attributes!=null) {
			for(AttributeInfo info:attributes) {
				sb.append(info).append("\n");
			}
		}
		return sb.toString();
	}
}
