package examples.classparser.entities.attributeinfo.info;

import java.util.List;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.SHORT;
import io.github.zhtmf.converters.auxiliary.DataType;

@Unsigned
public class Exceptions extends DataPacket{
    @Order(1)
    @SHORT
    @Length(type=DataType.SHORT)
    //number_of_exceptions implicitly in this declaration
    public List<Integer> exceptionIndexTable;    
}
