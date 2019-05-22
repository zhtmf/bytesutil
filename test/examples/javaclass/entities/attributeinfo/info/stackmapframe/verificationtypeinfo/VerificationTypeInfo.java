package examples.javaclass.entities.attributeinfo.info.stackmapframe.verificationtypeinfo;

import java.io.InputStream;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.BYTE;
import io.github.zhtmf.annotations.types.RAW;
import io.github.zhtmf.converters.auxiliary.ModifierHandler;

@Unsigned
public class VerificationTypeInfo extends DataPacket{
    @Order(0)
    @BYTE
    public VerificationTypeInfoTag tag;
    @Order(1)
    @RAW
    @Length(handler=IndexHandler.class)
    public int[] optionalIndex;
    
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
