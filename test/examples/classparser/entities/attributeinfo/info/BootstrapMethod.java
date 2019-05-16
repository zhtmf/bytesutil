package examples.classparser.entities.attributeinfo.info;

import java.util.List;

import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.Length;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Unsigned;
import org.dzh.bytesutil.annotations.types.SHORT;
import org.dzh.bytesutil.converters.auxiliary.DataType;

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
