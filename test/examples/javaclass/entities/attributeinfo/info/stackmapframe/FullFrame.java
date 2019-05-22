package examples.javaclass.entities.attributeinfo.info.stackmapframe;

import java.util.List;

import examples.javaclass.entities.attributeinfo.info.stackmapframe.verificationtypeinfo.VerificationTypeInfo;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.SHORT;
import io.github.zhtmf.converters.auxiliary.DataType;

@Unsigned
public class FullFrame extends StackMapFrame{
    @Order(0)
    @SHORT
    public int offsetDelta;
    @Order(1)
    @Length(type=DataType.SHORT)
    //number_of_locals implicitly in this declaration
    public List<VerificationTypeInfo> locals;
    @Order(2)
    @Length(type=DataType.SHORT)
    //number_of_stack_items implicitly in this declaration
    public List<VerificationTypeInfo> stack;
}
