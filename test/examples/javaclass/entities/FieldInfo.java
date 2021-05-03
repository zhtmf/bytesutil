package examples.javaclass.entities;

import java.util.List;

import examples.javaclass.entities.attributeinfo.AttributeInfo;
import examples.javaclass.entities.cpinfo.CpInfo;
import examples.javaclass.entities.cpinfo.info.CONSTANT_Utf8_info;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Injectable;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.SHORT;
import io.github.zhtmf.converters.auxiliary.DataType;

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
    public List<AttributeInfo> attributes;
    
    @Injectable
    private List<CpInfo> constantPool;
    
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
