package examples.classparser.entities.attributeinfo.info;

import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Unsigned;
import org.dzh.bytesutil.annotations.types.SHORT;

@Unsigned
public class Classes extends DataPacket{
	@Order(0)
	@SHORT
	public int innerClassInfoIndex;
	@Order(1)
	@SHORT
	public int outerClassInfoIndex;
	@Order(2)
	@SHORT
	public int innerNameIndex;
	@Order(3)
	@SHORT
	public int innerClassAccessFlags;
}
