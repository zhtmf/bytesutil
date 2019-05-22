package examples.classparser.entities.attributeinfo.info.stackmapframe;

import examples.classparser.entities.attributeinfo.info.stackmapframe.verificationtypeinfo.VerificationTypeInfo;
import io.github.zhtmf.annotations.modifiers.Order;

public class SameLocals1StackItemFrame extends StackMapFrame{
    @Order(0)
    public VerificationTypeInfo stack;
}
