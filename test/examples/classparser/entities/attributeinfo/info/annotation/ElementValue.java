package examples.classparser.entities.attributeinfo.info.annotation;

import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Unsigned;
import org.dzh.bytesutil.annotations.types.BYTE;

public abstract class ElementValue extends DataPacket{
	@Order(0)
	@BYTE
	@Unsigned
	public int tag;
}
