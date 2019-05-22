package examples.javaclass.entities.attributeinfo.info;

import java.util.List;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.converters.auxiliary.DataType;

@Unsigned
public class LocalVariableTypeTable extends DataPacket{
    @Order(0)
    @Length(type=DataType.SHORT)
    public List<LocalVariableType> localVariableTypeTable;
}
