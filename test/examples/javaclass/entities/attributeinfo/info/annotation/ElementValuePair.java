package examples.javaclass.entities.attributeinfo.info.annotation;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.modifiers.Variant;
import io.github.zhtmf.annotations.types.SHORT;

@Unsigned
public class ElementValuePair extends DataPacket{
    @Order(0)
    @SHORT
    public int elementNameIndex;
    @Order(1)
    @Variant(ElementValueHandler.class)
    public ElementValue elementValue;
}
