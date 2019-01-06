package classparser.entities.attributeinfo;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Unsigned;
import org.dzh.bytesutil.annotations.modifiers.Variant;
import org.dzh.bytesutil.annotations.types.INT;
import org.dzh.bytesutil.annotations.types.SHORT;
import org.dzh.bytesutil.converters.auxiliary.EntityHandler;

import classparser.entities.attributeinfo.info.AnnotationDefault;
import classparser.entities.attributeinfo.info.BootstrapMethods;
import classparser.entities.attributeinfo.info.Code;
import classparser.entities.attributeinfo.info.ConstantValue;
import classparser.entities.attributeinfo.info.DEPRECATED;
import classparser.entities.attributeinfo.info.EnclosingMethod;
import classparser.entities.attributeinfo.info.Exceptions;
import classparser.entities.attributeinfo.info.InnerClasses;
import classparser.entities.attributeinfo.info.LineNumberTable;
import classparser.entities.attributeinfo.info.LocalVariableTable;
import classparser.entities.attributeinfo.info.LocalVariableTypeTable;
import classparser.entities.attributeinfo.info.RuntimeInvisibleAnnotations;
import classparser.entities.attributeinfo.info.RuntimeInvisibleParameterAnnotations;
import classparser.entities.attributeinfo.info.RuntimeVisibleAnnotations;
import classparser.entities.attributeinfo.info.RuntimeVisibleParameterAnnotations;
import classparser.entities.attributeinfo.info.Signature;
import classparser.entities.attributeinfo.info.SourceDebugExtension;
import classparser.entities.attributeinfo.info.SourceFile;
import classparser.entities.attributeinfo.info.StackMapTable;
import classparser.entities.attributeinfo.info.Synthetic;
import classparser.entities.cpinfo.CpInfo;
import classparser.entities.cpinfo.info.CONSTANT_Utf8_info;

@Unsigned
public class AttributeInfo extends DataPacket{
	@Order(1)
	@SHORT
	public long attributeNameIndex;
	@Order(2)
	@INT
	public long attributeLength;
	@Order(3)
	@Variant(InfoHandler.class)
	public DataPacket info;
	
	private List<CpInfo> constantPool;
	public AttributeInfo(List<CpInfo> constantPool) {
		this.constantPool = constantPool;
	}
	
	public static class InfoHandler extends EntityHandler{

		@Override
		public DataPacket handle0(String fieldName, Object entity, InputStream is) throws IOException {
			AttributeInfo parent = (AttributeInfo)entity;
			int index = (int)parent.attributeNameIndex;
			//The constant_pool table is indexed from 1 to constant_pool_count-1.
			CpInfo relatedConstantInfo = parent.constantPool.get(index-1);
			if( ! (relatedConstantInfo.info instanceof CONSTANT_Utf8_info)) {
				throw new Error("constant info at index "+parent.attributeNameIndex+" is not a UTF-8 constant");
			}
			String name = ((CONSTANT_Utf8_info)relatedConstantInfo.info).bytes;
			switch(name) {
			case "ConstantValue":
				return new ConstantValue();
			case "Code":
				return new Code(parent.constantPool);
			case "StackMapTable":
				return new StackMapTable();
			case "Exceptions":
				return new Exceptions();
			case "InnerClasses":
				return new InnerClasses();
			case "EnclosingMethod":
				return new EnclosingMethod();
			case "Signature":
				return new Signature();
			case "Synthetic":
				return new Synthetic();
			case "SourceFile":
				return new SourceFile();
			case "SourceDebugExtension":
				return new SourceDebugExtension((int) parent.attributeLength);
			case "LineNumberTable":
				return new LineNumberTable();
			case "LocalVariableTable":
				return new LocalVariableTable();
			case "LocalVariableTypeTable":
				return new LocalVariableTypeTable();
			case "Deprecated":
				return new DEPRECATED();
			case "RuntimeVisibleAnnotations":
				return new RuntimeVisibleAnnotations();
			case "RuntimeInvisibleAnnotations":
				return new RuntimeInvisibleAnnotations();
			case "RuntimeVisibleParameterAnnotations":
				return new RuntimeVisibleParameterAnnotations();
			case "RuntimeInvisibleParameterAnnotations":
				return new RuntimeInvisibleParameterAnnotations();
			case "AnnotationDefault":
				return new AnnotationDefault();
			case "BootstrapMethods":
				return new BootstrapMethods();
			default:
				throw new Error("invalid Attribute name:"+name);
			}
		}	
	}
}
