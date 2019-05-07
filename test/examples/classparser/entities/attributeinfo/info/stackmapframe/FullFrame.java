package examples.classparser.entities.attributeinfo.info.stackmapframe;

import java.util.List;

import org.dzh.bytesutil.annotations.modifiers.Length;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Unsigned;
import org.dzh.bytesutil.annotations.types.SHORT;
import org.dzh.bytesutil.converters.auxiliary.DataType;

import examples.classparser.entities.attributeinfo.info.stackmapframe.verificationtypeinfo.VerificationTypeInfo;

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
