package examples.javaclass.entities.cpinfo.info;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.INT;

@Unsigned
public class CONSTANT_Integer_info extends DataPacket {
    @Order(0)
    @INT
    public long bytes;
    @Override
    public String toString() {
        return "CONSTANT_Integer_info [bytes=" + bytes + "]";
    }
}
