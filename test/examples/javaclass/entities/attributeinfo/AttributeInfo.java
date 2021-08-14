package examples.javaclass.entities.attributeinfo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import examples.javaclass.entities.attributeinfo.info.AnnotationDefault;
import examples.javaclass.entities.attributeinfo.info.BootstrapMethods;
import examples.javaclass.entities.attributeinfo.info.Code;
import examples.javaclass.entities.attributeinfo.info.ConstantValue;
import examples.javaclass.entities.attributeinfo.info.DEPRECATED;
import examples.javaclass.entities.attributeinfo.info.EnclosingMethod;
import examples.javaclass.entities.attributeinfo.info.Exceptions;
import examples.javaclass.entities.attributeinfo.info.InnerClasses;
import examples.javaclass.entities.attributeinfo.info.LineNumberTable;
import examples.javaclass.entities.attributeinfo.info.LocalVariableTable;
import examples.javaclass.entities.attributeinfo.info.LocalVariableTypeTable;
import examples.javaclass.entities.attributeinfo.info.RuntimeInvisibleAnnotations;
import examples.javaclass.entities.attributeinfo.info.RuntimeInvisibleParameterAnnotations;
import examples.javaclass.entities.attributeinfo.info.RuntimeVisibleAnnotations;
import examples.javaclass.entities.attributeinfo.info.RuntimeVisibleParameterAnnotations;
import examples.javaclass.entities.attributeinfo.info.Signature;
import examples.javaclass.entities.attributeinfo.info.SourceDebugExtension;
import examples.javaclass.entities.attributeinfo.info.SourceFile;
import examples.javaclass.entities.attributeinfo.info.StackMapTable;
import examples.javaclass.entities.attributeinfo.info.Synthetic;
import examples.javaclass.entities.cpinfo.CpInfo;
import examples.javaclass.entities.cpinfo.info.CONSTANT_Utf8_info;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Injectable;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.modifiers.Variant;
import io.github.zhtmf.annotations.types.INT;
import io.github.zhtmf.annotations.types.SHORT;
import io.github.zhtmf.converters.auxiliary.EntityHandler;

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
    
    @Injectable
    public void setConstantPool(List<CpInfo> constantPool) {
		this.constantPool = Collections.unmodifiableList(constantPool);
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
                return new Code();
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
    
    @Override
    public String toString() {
        int index = (int)this.attributeNameIndex;
        //The constant_pool table is indexed from 1 to constant_pool_count-1.
        CpInfo relatedConstantInfo = this.constantPool.get(index-1);
        String name = ((CONSTANT_Utf8_info)relatedConstantInfo.info).bytes;
        return "Attribute "+name+": \n"+info;
    }
}
