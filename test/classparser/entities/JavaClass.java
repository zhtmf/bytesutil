package classparser.entities;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.BigEndian;
import org.dzh.bytesutil.annotations.modifiers.CHARSET;
import org.dzh.bytesutil.annotations.modifiers.Length;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Unsigned;
import org.dzh.bytesutil.annotations.modifiers.Variant;
import org.dzh.bytesutil.annotations.types.INT;
import org.dzh.bytesutil.annotations.types.SHORT;
import org.dzh.bytesutil.converters.auxiliary.DataType;
import org.dzh.bytesutil.converters.auxiliary.EntityHandler;
import org.dzh.bytesutil.converters.auxiliary.ModifierHandler;

import classparser.entities.attributeinfo.AttributeInfo;
import classparser.entities.cpinfo.CpInfo;

@Unsigned
@CHARSET("UTF-8")
@BigEndian
public class JavaClass extends DataPacket{
	@Order(-1)
	@INT
	public long magic;
	@Order(0)
	@SHORT
	public int minorVersion;
	@Order(1)
	@SHORT
	public int majorVersion;
	@Order(2)
	@SHORT
	public int constantPoolCount;
	//The constant_pool table is indexed from 1 to constant_pool_count-1.
	@Order(3)
	@Length(handler=ConstantPoolHandler.class)
	public List<CpInfo> constantPool;
	@Order(4)
	@SHORT
	public int accessFlags;
	@Order(5)
	@SHORT
	public int thisClass;
	@Order(6)
	@SHORT
	public int superClass;
	//interfaces_count implicitly in this declaration
	@Order(7)
	@Length(type=DataType.SHORT)
	@SHORT
	public List<Integer> interfaces;
	//fields_count implicitly in this declaration
	@Order(8)
	@Length(type=DataType.SHORT)
	@Variant(FieldHandler.class)
	public List<FieldInfo> fields;
	//methods_count implicitly in this declaration
	@Order(9)
	@Length(type=DataType.SHORT)
	@Variant(MethodInfoHandler.class)
	public List<MethodInfo> methods;
	//attributes_count implicitly in this declaration
	@Order(10)
	@Length(type=DataType.SHORT)
	@Variant(AttributeInfoHandler3.class)
	public List<AttributeInfo> attributes;
	
	public static class ConstantPoolHandler extends ModifierHandler<Integer>{

		@Override
		public Integer handleDeserialize0(String fieldName, Object entity, InputStream is) {
			return ((JavaClass)entity).constantPoolCount -1;
		}

		@Override
		public Integer handleSerialize0(String fieldName, Object entity) {
			return ((JavaClass)entity).constantPoolCount -1;
		}
		
	}
	
	public static class FieldHandler extends EntityHandler{
		@Override
		public DataPacket handle0(String fieldName, Object entity, InputStream is) throws IOException {
			JavaClass parent = (JavaClass)entity;
			return new FieldInfo(Collections.unmodifiableList(parent.constantPool));
		}
	}
	
	public static class MethodInfoHandler extends EntityHandler{
		@Override
		public DataPacket handle0(String fieldName, Object entity, InputStream is) throws IOException {
			JavaClass parent = (JavaClass)entity;
			return new MethodInfo(Collections.unmodifiableList(parent.constantPool));
		}
	}
	
	public static class AttributeInfoHandler3 extends EntityHandler{
		@Override
		public DataPacket handle0(String fieldName, Object entity, InputStream is) throws IOException {
			JavaClass info = (JavaClass)entity;
			return new AttributeInfo(Collections.unmodifiableList(info.constantPool));
		}
		
	}
	
	@Override
	public String toString() {
		return "JavaClass [magic=" + magic + ", minorVersion=" + minorVersion + ", majorVersion=" + majorVersion
				+ ", constantPool=" + printConstantPool() + ", accessFlags=" + accessFlags + ", thisClass=" + thisClass
				+ ", superClass=" + superClass + ", interfaces=" + interfaces + ", fields=" + fields + ", methods="
				+ methods + ", attributes=" + attributes + "]";
	}
	
	private String printConstantPool() {
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		int counter = 1;
		for(CpInfo info:constantPool) {
			sb.append(counter+++" "+info.toString()).append("\n");
		}
		return sb.toString();
	}
}
