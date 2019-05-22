package examples.classparser.entities.attributeinfo.info;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.SHORT;

@Unsigned
public class Classes extends DataPacket{
    @Order(0)
    @SHORT
    public int innerClassInfoIndex;
    @Order(1)
    @SHORT
    public int outerClassInfoIndex;
    @Order(2)
    @SHORT
    public int innerNameIndex;
    @Order(3)
    @SHORT
    public int innerClassAccessFlags;
}
