package examples.javaclass.entities.attributeinfo.info;

import java.io.InputStream;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.SHORT;
import io.github.zhtmf.converters.auxiliary.ModifierHandler;

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
