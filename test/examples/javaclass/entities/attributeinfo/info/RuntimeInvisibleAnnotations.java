package examples.javaclass.entities.attributeinfo.info;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Order;

public class RuntimeInvisibleAnnotations extends DataPacket{
    @Order(0)
    public AnnotationList annotations;
}
