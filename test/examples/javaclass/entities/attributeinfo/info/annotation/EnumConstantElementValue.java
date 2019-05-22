package examples.javaclass.entities.attributeinfo.info.annotation;

import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.SHORT;

@Unsigned
public class EnumConstantElementValue extends ElementValue{
    @Order(0)
    @SHORT
    public int typeNameIndex;
    @Order(1)
    @SHORT
    public int constNameIndex;
}
