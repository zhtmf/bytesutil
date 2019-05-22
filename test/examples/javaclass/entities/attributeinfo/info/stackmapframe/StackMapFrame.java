package examples.javaclass.entities.attributeinfo.info.stackmapframe;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.BYTE;

@Unsigned
public class StackMapFrame extends DataPacket{
    @Order(0)
    @BYTE
    public int frameType;
}
