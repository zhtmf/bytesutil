package examples.classparser.entities.attributeinfo.info.annotation;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.BYTE;

public abstract class ElementValue extends DataPacket{
    @Order(0)
    @BYTE
    @Unsigned
    public int tag;
}
