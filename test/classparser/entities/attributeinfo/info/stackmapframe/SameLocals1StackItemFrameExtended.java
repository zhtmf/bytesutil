package classparser.entities.attributeinfo.info.stackmapframe;

import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Unsigned;
import org.dzh.bytesutil.annotations.types.SHORT;

import classparser.entities.attributeinfo.info.stackmapframe.verificationtypeinfo.VerificationTypeInfo;

@Unsigned
public class SameLocals1StackItemFrameExtended extends StackMapFrame{
	@Order(0)
	@SHORT
	public int offsetDelta;
	@Order(1)
	public VerificationTypeInfo stack;
}
