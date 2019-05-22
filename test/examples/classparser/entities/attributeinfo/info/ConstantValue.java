package examples.classparser.entities.attributeinfo.info;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.SHORT;

public class ConstantValue extends DataPacket{
    @Unsigned
    @SHORT
    @Order(0)
    public int constantValueIndex;
}
