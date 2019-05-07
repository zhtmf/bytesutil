package examples.classparser.entities.attributeinfo.info;

import java.util.List;

import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.Length;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.converters.auxiliary.DataType;

public class BootstrapMethods extends DataPacket{
	@Order(0)
	@Length(type=DataType.SHORT)
	List<BootstrapMethod> bootstrapMethods;
}