package examples.classparser.entities.attributeinfo.info.stackmapframe;

import java.io.InputStream;
import java.util.List;

import org.dzh.bytesutil.annotations.modifiers.Length;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Unsigned;
import org.dzh.bytesutil.annotations.types.SHORT;
import org.dzh.bytesutil.converters.auxiliary.ModifierHandler;

import examples.classparser.entities.attributeinfo.info.stackmapframe.verificationtypeinfo.VerificationTypeInfo;

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
