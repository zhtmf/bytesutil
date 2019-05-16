package examples.classparser.entities.cpinfo.info;

import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Unsigned;
import org.dzh.bytesutil.annotations.types.INT;

@Unsigned
public class CONSTANT_Float_info extends DataPacket {
    @Order(0)
    @INT
    public long bytes;
}
