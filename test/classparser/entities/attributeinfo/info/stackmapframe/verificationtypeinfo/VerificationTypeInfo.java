package classparser.entities.attributeinfo.info.stackmapframe.verificationtypeinfo;

import java.io.InputStream;

import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.Length;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Unsigned;
import org.dzh.bytesutil.annotations.types.BYTE;
import org.dzh.bytesutil.annotations.types.RAW;
import org.dzh.bytesutil.converters.auxiliary.ModifierHandler;

@Unsigned
public class VerificationTypeInfo extends DataPacket{
	@Order(0)
	@BYTE
	public VerificationTypeInfoTag tag;
	@Order(1)
	@RAW
	@Length(handler=IndexHandler.class)
	public byte[] optionalIndex;
	
	public static class IndexHandler extends ModifierHandler<Integer>{

		@Override
		public Integer handleDeserialize0(String fieldName, Object entity, InputStream is) {
			VerificationTypeInfo info = (VerificationTypeInfo)entity;
			switch(info.tag) {
			case ITEM_Object:
			case ITEM_Uninitialized:
				return 2;
			default:
				return 0;
			}
		}

		@Override
		public Integer handleSerialize0(String fieldName, Object entity) {
			VerificationTypeInfo info = (VerificationTypeInfo)entity;
			switch(info.tag) {
			case ITEM_Object:
			case ITEM_Uninitialized:
				return 2;
			default:
				return 0;
			}
		}
		
	}
}
