package org.dzh.bytesutil.converters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import org.dzh.bytesutil.DataType;
import org.dzh.bytesutil.annotations.types.BCD;
import org.dzh.bytesutil.annotations.types.CHAR;
import org.dzh.bytesutil.converters.auxiliary.Context;
import org.dzh.bytesutil.converters.auxiliary.StreamUtils;

public class DateConverter implements Converter<Date>{
	
	@Override
	public void serialize(Date value, DataType target, OutputStream dest, Context ctx, Object self)
			throws IOException, UnsupportedOperationException {
		if(value==null) {
			throw new IllegalArgumentException("java.util.Date value should not be null");
		}
		String datePattern = ctx.datePattern;
		if(datePattern==null) {
			throw new IllegalArgumentException("define a date pattern");
		}
		String str = getThreadLocalObject(datePattern).format(value);
		switch(target) {
		case BCD:
			if(ctx.annotation(BCD.class).value()!=str.length()/2) {
				throw new IllegalArgumentException(String.format(
						"defined BCD length [%d] should be double the length of formatted string [%s]"
						,ctx.annotation(BCD.class).value(),str));
			}
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
			if(ctx.annotation(CHAR.class).value()!=str.length()) {
				throw new IllegalArgumentException(String.format(
						"defined CHAR length [%d] is different from length of formatted string [%s]"
						,ctx.annotation(CHAR.class).value(),str));
			}
			StreamUtils.writeBytes(dest, str.getBytes(StandardCharsets.ISO_8859_1));
			break;
		default:
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public Date deserialize(DataType src, InputStream is, Context ctx, Object self)
			throws IOException, UnsupportedOperationException {
		String datePattern = ctx.datePattern;
		if(datePattern==null) {
			throw new IllegalArgumentException("define a date pattern");
		}
		try {
			switch(src) {
			case BCD:
					return getThreadLocalObject(datePattern)
							.parse(StreamUtils.readStringBCD(
									is,ctx.annotation(BCD.class).value()));
				
			case CHAR:
					return getThreadLocalObject(datePattern)
							.parse(new String(
									StreamUtils.readBytes(
											is, ctx.annotation(CHAR.class).value())
									,StandardCharsets.ISO_8859_1));
			default:
				throw new UnsupportedOperationException();
			}
		} catch (ParseException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	private SimpleDateFormat getThreadLocalObject(String datePattern) {
		ThreadLocal<SimpleDateFormat> tl = formatterMap.get(datePattern);
		if (tl == null) {
			tl = new _TLFormatter(datePattern);
			formatterMap.put(datePattern, tl);
		}
		return tl.get();
	}
	
	private ConcurrentHashMap<String, ThreadLocal<SimpleDateFormat>> formatterMap = new ConcurrentHashMap<>();

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
