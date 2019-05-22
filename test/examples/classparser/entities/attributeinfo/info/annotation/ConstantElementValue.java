package examples.classparser.entities.attributeinfo.info.annotation;

import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.SHORT;

@Unsigned
public class ConstantElementValue extends ElementValue{
    @Order(0)
    @SHORT
    public int constantValueIndex;
}
