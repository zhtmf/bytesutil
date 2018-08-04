package org.dzh.bytesutil.converters;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.dzh.bytesutil.annotations.types.BCD;
import org.dzh.bytesutil.converters.auxiliary.FieldInfo;
import org.dzh.bytesutil.converters.auxiliary.StreamUtils;
import org.dzh.bytesutil.converters.auxiliary.Utils;

public class StringConverter implements Converter<String> {

	@Override
	public void serialize(String value, OutputStream dest, FieldInfo ctx, Object self)
			throws IOException, UnsupportedOperationException {
		value = value == null ? "" : value;
		switch(ctx.type) {
		case CHAR:{
			
			Charset cs = ctx.charset;
			if(cs==null) {
				cs = (Charset) ctx.charsetHandler.handleSerialize(ctx.name,self);
			}
			byte[] bytes = value.getBytes(cs);
			byte[] bytes2 = null;
			
			int length = Utils.lengthForSerializingCHAR(ctx, self);
			if(length<0) {
				if(ctx.endsWith!=null) {
					//this string is of nondeterministic length
					//but will always ends with specific char in the stream
					bytes2 = ctx.endsWith.getBytes(cs);
				}else {
					//due to the pre-check in Context class, Length must be present at this point
					length = bytes.length;
					StreamUtils.writeIntegerOfType(dest, ctx.lengthType, length, ctx.bigEndian);
				}
				
			}else if(length!=bytes.length) {
				throw new IllegalArgumentException(
						String.format("encoded byte array length [%d] not equals with declared CHAR length [%d]"
									,bytes.length,length));
			}
			
			StreamUtils.writeBytes(dest, bytes);
			if(bytes2!=null) {
				StreamUtils.writeBytes(dest, bytes2);
			}
			break;
		}
		case BCD:{
			int length = ctx.localAnnotation(BCD.class).value();
			Utils.checkBCDLength(value, length);
			int[] values = new int[length*2];
			for(int i=0;i<values.length;++i) {
				char c = value.charAt(i);
				if(c<'0' || c>'9') {
					throw new IllegalArgumentException("only numeric characters are allowed in BCD");
				}
				values[i] = c-'0';
			}
			StreamUtils.writeBCD(dest, values);
			break;
		}
		default:
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public String deserialize(InputStream is, FieldInfo ctx, Object self)
			throws IOException, UnsupportedOperationException {
		switch(ctx.type) {
		case CHAR:{
			Charset cs = ctx.charset;
			if(cs==null) {
				cs = ctx.charsetHandler.handleDeserialize(
						ctx.name,self,(BufferedInputStream) is);
			}
			int length = Utils.lengthForDeserializingCHAR(ctx, self, (BufferedInputStream) is);
			if(length<0) {
				if(ctx.endsWith!=null) {
					//read the stream until specified end mark is seen
					String mark = ctx.endsWith;
					int lookback = 0;
					int pos = 0;
					char c = 0;
					/*
					 * the InputStreamReader internally maintains a buffer and it will read more bytes than actually need, 
					 * so we have to put all the bytes it reads back to the stream and skip bytes for 
					 * the string and the end mark again. So the remaining bytes unnecessarily read by the InputStreamReader
					 * can be seen and parsed by other codes.
					 */
					BufferedInputStream bis = (BufferedInputStream)is;
					bis.mark(Integer.MAX_VALUE);
					InputStreamReader isr = new InputStreamReader(is,cs);
					StringBuilder sb = new StringBuilder();
					while((c = (char) isr.read())!=Character.MAX_VALUE) {
						sb.append(c);
						if(c == mark.charAt(pos)) {
							++pos;
						}else {
							pos = 0;
						}
						lookback += bytesForChar(c,cs);
						if(pos == mark.length()) {
							bis.reset();
							while(lookback-->0) {
								bis.read();
							}
							return sb.substring(0,sb.length()-mark.length());
						}
					}
					throw new EOFException("end mark ["+mark+"] not met before end of stream");
				}else {
					length = StreamUtils.readIntegerOfType(is, ctx.lengthType, ctx.bigEndian);
				}
			}
			return new String(StreamUtils.readBytes(is, length),cs);
		}
		case BCD:{
			return StreamUtils.readStringBCD(is, ctx.localAnnotation(BCD.class).value());
		}
		default:
			throw new UnsupportedOperationException();
		}
	}
	
	private static int bytesForChar(char c,Charset cs) {
		return (c+"").getBytes(cs).length;
	}
}
