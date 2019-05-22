package examples.classparser.entities;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import examples.classparser.entities.attributeinfo.AttributeInfo;
import examples.classparser.entities.cpinfo.CpInfo;
import examples.classparser.entities.cpinfo.info.CONSTANT_Utf8_info;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.modifiers.Variant;
import io.github.zhtmf.annotations.types.SHORT;
import io.github.zhtmf.converters.auxiliary.DataType;
import io.github.zhtmf.converters.auxiliary.EntityHandler;

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
