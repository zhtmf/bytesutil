package classparser.entities.attributeinfo.info;

import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Unsigned;
import org.dzh.bytesutil.annotations.modifiers.Variant;

import classparser.entities.attributeinfo.info.annotation.ElementValue;
import classparser.entities.attributeinfo.info.annotation.ElementValueHandler;

@Unsigned
public class AnnotationDefault extends DataPacket{
	@Order(1)
	@Variant(ElementValueHandler.class)
	public ElementValue elementValue;
}
