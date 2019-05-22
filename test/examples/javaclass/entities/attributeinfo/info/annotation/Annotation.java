package examples.javaclass.entities.attributeinfo.info.annotation;

import java.util.List;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.SHORT;
import io.github.zhtmf.converters.auxiliary.DataType;

@Unsigned
public class Annotation extends DataPacket{
    @Order(0)
    @SHORT
    public int typeIndex;
    @Order(1)
    @Length(type=DataType.SHORT)
    public List<ElementValuePair> elementValuePairs;
}
