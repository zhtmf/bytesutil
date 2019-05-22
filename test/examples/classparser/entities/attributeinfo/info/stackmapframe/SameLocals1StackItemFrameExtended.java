package examples.classparser.entities.attributeinfo.info.stackmapframe;

import examples.classparser.entities.attributeinfo.info.stackmapframe.verificationtypeinfo.VerificationTypeInfo;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.SHORT;

@Unsigned
public class SameLocals1StackItemFrameExtended extends StackMapFrame{
    @Order(0)
    @SHORT
    public int offsetDelta;
    @Order(1)
    public VerificationTypeInfo stack;
}
