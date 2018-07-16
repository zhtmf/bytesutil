package test.entity;

import java.io.InputStream;

import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.BigEndian;
import org.dzh.bytesutil.annotations.modifiers.Length;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.types.CHAR;
import org.dzh.bytesutil.converters.auxiliary.ModifierHandler;

@BigEndian
public class WeirdEntity extends DataPacket{

	@Order(0)
	@CHAR
	@Length
	public String char1;
	
	@Order(1)
	@CHAR
	@Length(12)
	public String char2;
	
	@Order(2)
	@CHAR
	@Length(handler=Handler.class)
	public String char3;
	
	public static class Handler extends ModifierHandler<Integer>{

		@Override
		public Integer handleDeserialize0(String fieldName, Object entity, InputStream is) {
			WeirdEntity ae = (WeirdEntity)entity;
			return ae.char1.length();
		}

		@Override
		public Integer handleSerialize0(String fieldName, Object entity) {
			WeirdEntity ae = (WeirdEntity)entity;
			return ae.char1.length();
		}
		
	}
}
