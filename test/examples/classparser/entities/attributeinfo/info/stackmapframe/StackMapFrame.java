package examples.classparser.entities.attributeinfo.info.stackmapframe;

import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Unsigned;
import org.dzh.bytesutil.annotations.types.BYTE;

@Unsigned
public class StackMapFrame extends DataPacket{
    @Order(0)
    @BYTE
    public int frameType;
}
