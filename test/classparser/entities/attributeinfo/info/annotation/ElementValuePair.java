package classparser.entities.attributeinfo.info.annotation;

import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Unsigned;
import org.dzh.bytesutil.annotations.modifiers.Variant;
import org.dzh.bytesutil.annotations.types.SHORT;

@Unsigned
public class ElementValuePair extends DataPacket{
	@Order(0)
	@SHORT
	public int elementNameIndex;
	@Order(1)
	@Variant(ElementValueHandler.class)
	public ElementValue elementValue;
}
