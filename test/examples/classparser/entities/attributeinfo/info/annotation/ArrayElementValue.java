package examples.classparser.entities.attributeinfo.info.annotation;

import java.util.List;

import org.dzh.bytesutil.annotations.modifiers.Length;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Unsigned;
import org.dzh.bytesutil.annotations.modifiers.Variant;
import org.dzh.bytesutil.converters.auxiliary.DataType;

@Unsigned
public class ArrayElementValue extends ElementValue{
    @Order(0)
    @Length(type=DataType.SHORT)
    @Variant(ElementValueHandler.class)
    public List<ElementValue> values;
}
