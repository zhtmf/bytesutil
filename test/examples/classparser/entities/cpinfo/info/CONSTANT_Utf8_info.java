package examples.classparser.entities.cpinfo.info;

import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.CHARSET;
import org.dzh.bytesutil.annotations.modifiers.Length;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Unsigned;
import org.dzh.bytesutil.annotations.types.CHAR;
import org.dzh.bytesutil.converters.auxiliary.DataType;

@Unsigned
@CHARSET("UTF-8")
public class CONSTANT_Utf8_info extends DataPacket {
	@Order(0)
	@CHAR
	@Length(type=DataType.SHORT)
	public String bytes;
	@Override
	public String toString() {
		return "CONSTANT_Utf8_info [bytes=" + bytes + "]";
	}
}
