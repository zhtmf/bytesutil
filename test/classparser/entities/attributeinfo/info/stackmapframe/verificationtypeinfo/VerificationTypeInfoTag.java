package classparser.entities.attributeinfo.info.stackmapframe.verificationtypeinfo;

import org.dzh.bytesutil.annotations.enums.NumericEnum;

public enum VerificationTypeInfoTag implements NumericEnum{
	ITEM_Top(0),
	ITEM_Integer(1),
	ITEM_Float(2),
	ITEM_Long(4),
	ITEM_Double(3),
	ITEM_Null(5),
	ITEM_UninitializedThis(6),
	ITEM_Object(7),
	ITEM_Uninitialized(8);
	private int tag;
	private VerificationTypeInfoTag(int tag) {
		this.tag = tag;
	}
	@Override
	public long getValue() {
		return tag;
	}
}
