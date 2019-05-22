package examples.javaclass.entities.attributeinfo.info;

import examples.javaclass.entities.attributeinfo.info.annotation.ElementValue;
import examples.javaclass.entities.attributeinfo.info.annotation.ElementValueHandler;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.modifiers.Variant;

@Unsigned
public class AnnotationDefault extends DataPacket{
    @Order(1)
    @Variant(ElementValueHandler.class)
    public ElementValue elementValue;
}
