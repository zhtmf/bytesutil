package examples.classparser.entities.attributeinfo.info;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.SHORT;

@Unsigned
public class ExceptionTable extends DataPacket{
    @Order(0)
    @SHORT
    public int startPc;
    @Order(1)
    @SHORT
    public int endPc;
    @Order(2)
    @SHORT
    public int handlerPc;
    @Order(3)
    @SHORT
    public int catchType;
}
