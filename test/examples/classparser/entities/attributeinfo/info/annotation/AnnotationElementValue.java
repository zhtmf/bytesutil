package examples.classparser.entities.attributeinfo.info.annotation;

import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;

@Unsigned
public class AnnotationElementValue extends ElementValue{
    @Order(0)
    public Annotation annotationValue;
}
