package examples.classparser.entities.attributeinfo.info;

import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Unsigned;
import org.dzh.bytesutil.annotations.types.SHORT;

public class ConstantValue extends DataPacket{
	@Unsigned
	@SHORT
	@Order(0)
	public int constantValueIndex;
}
