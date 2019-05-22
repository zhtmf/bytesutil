package examples.javaclass.entities.attributeinfo.info.stackmapframe;

import java.io.InputStream;
import java.util.List;

import examples.javaclass.entities.attributeinfo.info.stackmapframe.verificationtypeinfo.VerificationTypeInfo;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.SHORT;
import io.github.zhtmf.converters.auxiliary.ModifierHandler;

@Unsigned
public class AppendFrame extends StackMapFrame{
    @Order(0)
    @SHORT
    public int offsetDelta;
    @Order(1)
    @Length(handler=LocalsLengthHandler.class)
    public List<VerificationTypeInfo> locals;
    
    public static class LocalsLengthHandler extends ModifierHandler<Integer>{

        @Override
        public Integer handleDeserialize0(String fieldName, Object entity, InputStream is) {
            AppendFrame frame = (AppendFrame)entity;
            return frame.frameType - 251;
        }

        @Override
        public Integer handleSerialize0(String fieldName, Object entity) {
            AppendFrame frame = (AppendFrame)entity;
            return frame.frameType - 251;
        }
        
    }
}
