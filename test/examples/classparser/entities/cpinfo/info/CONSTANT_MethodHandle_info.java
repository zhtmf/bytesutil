package examples.classparser.entities.cpinfo.info;

import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Unsigned;
import org.dzh.bytesutil.annotations.types.BYTE;
import org.dzh.bytesutil.annotations.types.SHORT;

@Unsigned
public class CONSTANT_MethodHandle_info extends DataPacket {
    @Order(0)
    @BYTE
    public ReferenceKind kind;
    @Order(1)
    @SHORT
    public int referenceIndex;
}
