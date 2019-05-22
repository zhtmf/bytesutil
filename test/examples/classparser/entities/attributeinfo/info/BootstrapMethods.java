package examples.classparser.entities.attributeinfo.info;

import java.util.List;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.converters.auxiliary.DataType;

public class BootstrapMethods extends DataPacket{
    @Order(0)
    @Length(type=DataType.SHORT)
    List<BootstrapMethod> bootstrapMethods;
}
