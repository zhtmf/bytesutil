package org.dzh.bytesutil.converters;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import org.dzh.bytesutil.annotations.types.BCD;
import org.dzh.bytesutil.converters.auxiliary.FieldInfo;
import org.dzh.bytesutil.converters.auxiliary.MarkableStream;
import org.dzh.bytesutil.converters.auxiliary.StreamUtils;
import org.dzh.bytesutil.converters.auxiliary.Utils;

public class DateConverter implements Converter<Date>{
	
	@Override
	public void serialize(Date value, OutputStream dest, FieldInfo ctx, Object self)
			throws IOException, UnsupportedOperationException {
		if(value==null) {
			throw new IllegalArgumentException("java.util.Date value should not be null");
		}
		String datePattern = ctx.datePattern;
		if(datePattern==null) {
			throw new IllegalArgumentException("define a date pattern");
		}
		String str = getThreadLocalObject(datePattern).format(value);
		byte[] bytes = str.getBytes(StandardCharsets.ISO_8859_1);
		
		switch(ctx.type) {
		case BCD:
			Utils.checkBCDLength(str, ctx.annotation(BCD.class).value());
			int[] values = new int[str.length()];
			for(int i=0;i<str.length();++i) {
				char c = str.charAt(i);
				if(!(c>='0' && c<='9')) {
					throw new IllegalArgumentException("only numeric value is supported in bcd");
				}
				values[i] = c-'0';
			}
			StreamUtils.writeBCD(dest, values);
			break;
		case CHAR:
			
			int length = Utils.lengthForSerializingCHAR(ctx, self);
			if(length<0) {
				//due to the pre-check in Context class, Length must be present at this point
				length = bytes.length;
				StreamUtils.writeIntegerOfType(dest, ctx.lengthType(), length, ctx.bigEndian);
				
			}else if(length!=bytes.length) {
				throw new IllegalArgumentException(
						String.format("encoded byte array length [%d] not equals with declared CHAR length [%d]"
									,bytes.length,length));
			}
			
			StreamUtils.writeBytes(dest, bytes);
			
			break;
		default:
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public Date deserialize(MarkableStream is, FieldInfo ctx, Object self)
			throws IOException, UnsupportedOperationException {
		String datePattern = ctx.datePattern;
		if(datePattern==null) {
			throw new IllegalArgumentException("define a date pattern");
		}
		try {
			switch(ctx.type) {
			case BCD:
					return getThreadLocalObject(datePattern)
							.parse(StreamUtils.readStringBCD(
									is,ctx.annotation(BCD.class).value()));
				
			case CHAR:{
				int length = Utils.lengthForDeserializingCHAR(ctx, self, is);
				if(length<0) {
					length = StreamUtils.readIntegerOfType(is, ctx.lengthType(), ctx.bigEndian);
				}
				return getThreadLocalObject(datePattern)
						.parse(new String(
								StreamUtils.readBytes(
										is, length)
								,StandardCharsets.ISO_8859_1));
			}
			default:
				throw new UnsupportedOperationException();
			}
		} catch (ParseException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	private static final SimpleDateFormat getThreadLocalObject(String datePattern) {
		ThreadLocal<SimpleDateFormat> tl = formatterMap.get(datePattern);
		if (tl == null) {
			tl = new _TLFormatter(datePattern);
			formatterMap.put(datePattern, tl);
		}
		return tl.get();
	}
	
	private static final ConcurrentHashMap<String, ThreadLocal<SimpleDateFormat>> formatterMap = new ConcurrentHashMap<>();

	private static final class _TLFormatter extends ThreadLocal<SimpleDateFormat> {
		private String p;

		public _TLFormatter(String p) {
			this.p = p;
		}

		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat(p);
		}
	}
}
