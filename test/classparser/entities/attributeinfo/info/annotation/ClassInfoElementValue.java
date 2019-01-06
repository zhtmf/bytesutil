package classparser.entities.attributeinfo.info.annotation;

import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Unsigned;
import org.dzh.bytesutil.annotations.types.SHORT;

@Unsigned
public class ClassInfoElementValue extends ElementValue{
	@Order(0)
	@SHORT
	public int classInfoIndex;
}
