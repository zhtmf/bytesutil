package classparser.entities.attributeinfo.info.stackmapframe;

import org.dzh.bytesutil.annotations.modifiers.Order;

import classparser.entities.attributeinfo.info.stackmapframe.verificationtypeinfo.VerificationTypeInfo;

public class SameLocals1StackItemFrame extends StackMapFrame{
	@Order(0)
	public VerificationTypeInfo stack;
}
