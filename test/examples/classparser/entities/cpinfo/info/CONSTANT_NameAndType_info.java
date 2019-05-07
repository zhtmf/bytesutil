package examples.classparser.entities.cpinfo.info;

import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Unsigned;
import org.dzh.bytesutil.annotations.types.SHORT;

@Unsigned
public class CONSTANT_NameAndType_info extends DataPacket {
	@Order(0)
	@SHORT
	public int nameIndex;
	@Order(1)
	@SHORT
	public int descriptorIndex;
}
