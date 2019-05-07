package examples.classparser.entities.attributeinfo.info;

import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.Order;

public class RuntimeInvisibleAnnotations extends DataPacket{
	@Order(0)
	public AnnotationList annotations;
}
