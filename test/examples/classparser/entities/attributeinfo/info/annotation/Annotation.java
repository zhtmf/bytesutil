package examples.classparser.entities.attributeinfo.info.annotation;

import java.util.List;

import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.Length;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Unsigned;
import org.dzh.bytesutil.annotations.types.SHORT;
import org.dzh.bytesutil.converters.auxiliary.DataType;

@Unsigned
public class Annotation extends DataPacket{
	@Order(0)
	@SHORT
	public int typeIndex;
	@Order(1)
	@Length(type=DataType.SHORT)
	public List<ElementValuePair> elementValuePairs;
}
