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
    public List<AttributeInfo> attributes;
    
    @Injectable
    private List<CpInfo> constantPool;
    
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
