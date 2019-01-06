package classparser.entities.cpinfo.info;

import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Unsigned;
import org.dzh.bytesutil.annotations.types.RAW;

@Unsigned
public class CONSTANT_Double_info extends DataPacket {
	@Order(0)
	@RAW(8)
	//combined high-bytes and low-bytes
	public byte[] bytes;
}
