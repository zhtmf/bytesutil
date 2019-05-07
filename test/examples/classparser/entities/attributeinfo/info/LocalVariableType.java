package examples.classparser.entities.attributeinfo.info;

import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Unsigned;
import org.dzh.bytesutil.annotations.types.SHORT;

@Unsigned
public class LocalVariableType extends DataPacket{
	@Order(0)
	@SHORT
	public int startPc;
	@Order(1)
	@SHORT
	public int length;
	@Order(2)
	@SHORT
	public int nameIndex;
	@Order(3)
	@SHORT
	public int signatureIndex;
	@Order(4)
	@SHORT
	public int index;
}
