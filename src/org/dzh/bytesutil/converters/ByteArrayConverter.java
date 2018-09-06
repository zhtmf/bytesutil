package org.dzh.bytesutil.converters;

import java.io.IOException;
import java.io.OutputStream;

import org.dzh.bytesutil.converters.auxiliary.FieldInfo;
import org.dzh.bytesutil.converters.auxiliary.MarkableInputStream;
import org.dzh.bytesutil.converters.auxiliary.StreamUtils;
import org.dzh.bytesutil.converters.auxiliary.Utils;

/**
 * Converters that handles serialization and deserialization of <tt>byte[].class</tt>
 * 
 * @author dzh
 */
public class ByteArrayConverter implements Converter<byte[]>{
	
	@Override
	public void serialize(byte[] value, OutputStream dest, FieldInfo fi, Object self)
			throws IOException,UnsupportedOperationException {
		value = value == null ? new byte[0] : value;
		switch(fi.type) {
		case RAW:
			int length = Utils.lengthForSerializingRAW(fi, self);
			if(length<0) {
				StreamUtils.writeIntegerOfType(dest, fi.lengthType(), value.length, fi.bigEndian);
			}else if(length!=value.length) {
				throw new RuntimeException(
						String.format("field [%s] is defined as a byte array,"
								+ " but the defined length [%d] is not the same as length [%d] of the list"
								, fi.name,length,value.length));
			}
			StreamUtils.writeBytes(dest, value);
			break;
		default:
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public byte[] deserialize(MarkableInputStream is, FieldInfo fi, Object self)
			throws IOException,UnsupportedOperationException {
		switch(fi.type) {
		case RAW:
			int length = Utils.lengthForDeserializingRAW(fi, self, is);
			if(length<0) {
				length = StreamUtils.readIntegerOfType(is, fi.lengthType(), fi.bigEndian);
			}
			return StreamUtils.readBytes(is, length);
		default:
			throw new UnsupportedOperationException();
		}	
	}

}
