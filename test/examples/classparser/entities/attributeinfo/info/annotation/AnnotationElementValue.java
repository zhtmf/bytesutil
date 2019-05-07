package examples.classparser.entities.attributeinfo.info.annotation;

import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Unsigned;

@Unsigned
public class AnnotationElementValue extends ElementValue{
	@Order(0)
	public Annotation annotationValue;
}
