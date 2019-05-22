package examples.classparser.entities.attributeinfo.info;

import java.util.List;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.SHORT;
import io.github.zhtmf.converters.auxiliary.DataType;

@Unsigned
public class BootstrapMethod extends DataPacket{
    @Order(0)
    @SHORT
    public int bootstrapMethodRef;
    @Order(1)
    @SHORT
    @Length(type=DataType.SHORT)
    public List<Integer> bootstrapArguments;
}
