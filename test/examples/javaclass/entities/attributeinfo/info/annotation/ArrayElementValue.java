package examples.javaclass.entities.attributeinfo.info.annotation;

import java.util.List;

import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.modifiers.Variant;
import io.github.zhtmf.converters.auxiliary.DataType;

@Unsigned
public class ArrayElementValue extends ElementValue{
    @Order(0)
    @Length(type=DataType.SHORT)
    @Variant(ElementValueHandler.class)
    public List<ElementValue> values;
}
