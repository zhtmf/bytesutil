package examples.classparser.entities.attributeinfo.info;

import java.util.List;

import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.Length;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Unsigned;
import org.dzh.bytesutil.annotations.types.SHORT;
import org.dzh.bytesutil.converters.auxiliary.DataType;

@Unsigned
public class Exceptions extends DataPacket{
    @Order(1)
    @SHORT
    @Length(type=DataType.SHORT)
    //number_of_exceptions implicitly in this declaration
    public List<Integer> exceptionIndexTable;    
}
