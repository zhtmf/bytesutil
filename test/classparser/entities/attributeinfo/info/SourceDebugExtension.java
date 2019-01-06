package classparser.entities.attributeinfo.info;

import java.io.InputStream;

import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.Length;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Unsigned;
import org.dzh.bytesutil.annotations.types.SHORT;
import org.dzh.bytesutil.converters.auxiliary.ModifierHandler;

@Unsigned
public class SourceDebugExtension extends DataPacket{
	public int attributeLength;
	@Order(0)
	@SHORT
	@Length
	public byte[] debugExtension;
	
	public SourceDebugExtension(int attributeLength) {
		this.attributeLength = attributeLength;
	}
	
	public static final class DebugExtensionHandler extends ModifierHandler<Integer>{
		@Override
		public Integer handleDeserialize0(String fieldName, Object entity, InputStream is) {
			return ((SourceDebugExtension)entity).attributeLength;
		}
		@Override
		public Integer handleSerialize0(String fieldName, Object entity) {
			return ((SourceDebugExtension)entity).attributeLength;
		}
	}
}
