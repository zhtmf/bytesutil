package examples.classparser.entities.cpinfo.info;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.SHORT;

@Unsigned
public class CONSTANT_Class_info extends DataPacket{
    @Order(0)
    @SHORT
    public int nameIndex;
}
