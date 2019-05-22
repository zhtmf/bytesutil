package examples.classparser.entities.cpinfo.info;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.BYTE;
import io.github.zhtmf.annotations.types.SHORT;

@Unsigned
public class CONSTANT_MethodHandle_info extends DataPacket {
    @Order(0)
    @BYTE
    public ReferenceKind kind;
    @Order(1)
    @SHORT
    public int referenceIndex;
}
