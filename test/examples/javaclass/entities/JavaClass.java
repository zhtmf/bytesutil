package examples.javaclass.entities;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import examples.javaclass.entities.attributeinfo.AttributeInfo;
import examples.javaclass.entities.cpinfo.CpInfo;
import examples.javaclass.entities.cpinfo.info.CONSTANT_Class_info;
import examples.javaclass.entities.cpinfo.info.CONSTANT_Utf8_info;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.BigEndian;
import io.github.zhtmf.annotations.modifiers.CHARSET;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.modifiers.Variant;
import io.github.zhtmf.annotations.types.INT;
import io.github.zhtmf.annotations.types.SHORT;
import io.github.zhtmf.converters.auxiliary.DataType;
import io.github.zhtmf.converters.auxiliary.EntityHandler;
import io.github.zhtmf.converters.auxiliary.ModifierHandler;

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
        StringBuilder sb = new StringBuilder();
        if(constantPool==null) {
            constantPool = new ArrayList<>();
        }
        CONSTANT_Class_info classInfo = (CONSTANT_Class_info)constantPool.get(thisClass-1).info;
        CONSTANT_Utf8_info utf8 = (CONSTANT_Utf8_info) constantPool.get(classInfo.nameIndex-1).info;
        String className = utf8.bytes.replace('/', '.');
        sb.append("JavaClass:"+className+"\n");
        sb.append("magic:"+Long.toHexString(magic));sb.append("\n");
        sb.append("minorVersion:"+minorVersion);sb.append("\n");
        sb.append("majorVersion:"+majorVersion);sb.append("\n");
        sb.append("===CONSTANTPOOL=====");sb.append("\n");
        sb.append(printConstantPool());
        sb.append("accessFlags:"+Long.toBinaryString(accessFlags));sb.append("\n");
        sb.append("thisClass:"+thisClass);sb.append("\n");
        sb.append("superClass:"+superClass);sb.append("\n");
        sb.append("===INTERFACES===");sb.append("\n");
        sb.append(printInterfaces());
        sb.append("===FIELDS===");sb.append("\n");
        sb.append(printFields());
        sb.append("===METHODS===");sb.append("\n");
        sb.append(printMethods());
        sb.append("===ATTRIBUTES===");sb.append("\n");
        sb.append(printAttributes());
        return sb.toString();
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
    
    private String printMethods() {
        StringBuilder sb = new StringBuilder();
        if(methods!=null) {
            for(MethodInfo info:methods) {
                sb.append(info).append("\n");
            }
        }
        return sb.toString();
    }
    
    private String printFields() {
        StringBuilder sb = new StringBuilder();
        if(fields!=null) {
            for(FieldInfo info:fields) {
                sb.append(info).append("\n");
            }
        }
        return sb.toString();
    }
    
    private String printInterfaces() {
        StringBuilder sb = new StringBuilder();
        if(interfaces!=null) {
            for(Integer index:interfaces) {
                sb.append("#").append(index).append("\n");
            }
        }
        return sb.toString();
    }
    
    private String printConstantPool() {
        StringBuilder sb = new StringBuilder();
        int counter = 1;
        if(constantPool!=null) {
            for(CpInfo info:constantPool) {
                sb.append(counter+++" "+info.toString()).append("\n");
            }
        }
        return sb.toString();
    }
}
