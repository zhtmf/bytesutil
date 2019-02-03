package org.dzh.bytesutil.converters.auxiliary;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import org.dzh.bytesutil.ConversionException;
import org.dzh.bytesutil.annotations.types.CHAR;
import org.dzh.bytesutil.annotations.types.RAW;
import org.dzh.bytesutil.converters.auxiliary.exceptions.ExtendedConversionException;
import org.dzh.bytesutil.converters.auxiliary.exceptions.UnsatisfiedConstraintException;

public class Utils {
	
	public static Charset charsetForSerializingCHAR(FieldInfo ctx,Object self) {
		Charset cs = ctx.charset;
		if(cs==null) {
			cs = (Charset) ctx.charsetHandler.handleSerialize(ctx.name,self);
		}
		return cs;
	}
	
	public static Charset charsetForDeserializingCHAR(FieldInfo ctx,Object self, MarkableInputStream is) {
		Charset cs = ctx.charset;
		if(cs==null) {
			cs = ctx.charsetHandler.handleDeserialize(ctx.name,self,is);
		}
		return cs;
	}
	
	public static int lengthForSerializingCHAR(FieldInfo ctx,Object self) {
		int length = ctx.annotation(CHAR.class).value();
		if(length<0) {
			length = lengthForSerializingLength(ctx, self);
		}
		return length;
	}
	
	public static int lengthForDeserializingCHAR(FieldInfo ctx,Object self, MarkableInputStream bis) {
		int length = ctx.annotation(CHAR.class).value();
		if(length<0) {
			length = lengthForDeserializingLength(ctx,self,bis);
		}
		return length;
	}
	
	public static int lengthForSerializingRAW(FieldInfo ctx,Object self) {
		int length = ctx.annotation(RAW.class).value();
		if(length<0) {
			length = lengthForSerializingLength(ctx, self);
		}
		return length;
	}
	
	public static int lengthForDeserializingRAW(FieldInfo ctx,Object self, MarkableInputStream bis) {
		int length = ctx.annotation(RAW.class).value();
		if(length<0) {
			length = lengthForDeserializingLength(ctx,self,bis);
		}
		return length;
	}
	
	public static int lengthForSerializingLength(FieldInfo ctx,Object self) {
		int length = ctx.length;
		if(length<0 && ctx.lengthHandler!=null) {
			length = (Integer)ctx.lengthHandler.handleSerialize(ctx.name, self);
			if(length<0) {
				throw new IllegalArgumentException("should return non-negative value from length handler");
			}
		}
		return length;
	}
	
	public static int lengthForDeserializingLength(FieldInfo ctx,Object self, MarkableInputStream bis) {
		int length = ctx.length;
		if(length<0 && ctx.lengthHandler!=null) {
			length = (Integer)ctx.lengthHandler.handleDeserialize(ctx.name, self, bis);
			if(length<0) {
				throw new IllegalArgumentException("should return non-negative value from length handler");
			}
		}
		return length;
	}
	
	public static int lengthForSerializingListLength(FieldInfo ctx,Object self) {
		int length = ctx.listLength;
		if(length<0 && ctx.listLengthHandler!=null) {
			length = (Integer)ctx.listLengthHandler.handleSerialize(ctx.name, self);
			if(length<0) {
				throw new IllegalArgumentException("should return non-negative value from length handler");
			}
		}
		return length;
	}
	
	public static int lengthForList(FieldInfo fi,Object self) {
		/*
		 * ListLength first
		 * If the component type is not a dynamic-length data type, both listLength or Length may be present,
		 * if the component type is a dynamic-length data type, then listLenght must be present or an exception 
		 * will be thrown by ClassInfo
		 */
		int length = Utils.lengthForSerializingListLength(fi, self);
		if(length==-1)
			length = Utils.lengthForSerializingLength(fi, self);
		return length;
	}
	
	public static int lengthForDeserializingListLength(FieldInfo ctx,Object self, MarkableInputStream bis) {
		int length = ctx.listLength;
		if(length<0 && ctx.listLengthHandler!=null) {
			length = (Integer)ctx.listLengthHandler.handleDeserialize(ctx.name, self, bis);
			if(length<0) {
				throw new IllegalArgumentException("should return non-negative value from length handler");
			}
		}
		return length;
	}
	
	public static int[] checkAndConvertToBCD(long val, int bcdBytes) {
		if(val<0) {
			throw new IllegalArgumentException(String.format("negative number [%d] cannot be stored as BCD",val));
		}
		long copy = val;
		int[] values = new int[bcdBytes*2];
		int ptr = values.length-1;
		while(ptr>=0 && copy>0) {
			values[ptr--] = (int) (copy % 10);
			copy /= 10;
		}
		if(copy>0 || ptr>0) {
			throw new IllegalArgumentException(
					String.format("string format of number [%d] cannot fit in [%d]-byte BCD value", val, bcdBytes));
		}
		return values;
	}
	
	public static final void checkBCDLength(String src, int length) {
		if((src.length()>>1)!=length) {
			throw new IllegalArgumentException(String.format(
					"length of string should be [%d] (double long as declared BCD value), but it was [%d]", length*2, src.length()));
		}
	}
	
	public static final void checkRangeInContext(DataType type,long val,FieldInfo ctx) throws ConversionException {
		String error;
		if((error = type.checkRange(val, ctx.unsigned))!=null) {
			throw new ExtendedConversionException(ctx.enclosingEntityClass, ctx.name, error)
						.withSiteAndOrdinal(Utils.class, 1);
		}
	}
	
	public static final void serializeAsCHAR(String str, OutputStream dest, FieldInfo ctx, Object self)
			throws ConversionException, IOException {
		int length = Utils.lengthForSerializingCHAR(ctx, self);
		if(length<0) {
			length = str.length();
			StreamUtils.writeIntegerOfType(dest, ctx.lengthType(), length, ctx.bigEndian);
		}else if(length!=str.length()) {
			throw new ExtendedConversionException(ctx,
					String.format("length of string representation [%s] does not equals with declared CHAR length [%d]"
								,str,length))
						.withSiteAndOrdinal(Utils.class, 2);
		}
		StreamUtils.writeBytes(dest, str.getBytes(StandardCharsets.ISO_8859_1));
	}
	
	public static final void serializeAsCHAR(long val, OutputStream dest, FieldInfo ctx, Object self)
			throws ConversionException, IOException {
		if(val<0) {
			//implementation choice
			throw new ExtendedConversionException(ctx,"negative number should not be converted to CHAR")
						.withSiteAndOrdinal(Utils.class, 0);
		}
		String str = Long.toString(val);
		serializeAsCHAR(str,dest,ctx,self);
	}
	
	public static final long deserializeAsCHAR(
			MarkableInputStream is, FieldInfo ctx, Object self, DataType type)
			throws IOException, ConversionException {
		int length = Utils.lengthForDeserializingCHAR(ctx, self, is);
		if(length<0) {
			length = StreamUtils.readIntegerOfType(is, ctx.lengthType(), ctx.bigEndian);
		}
		long ret = 0;
		try {
			byte[] numChars = StreamUtils.readBytes(is, length);
			/*
			 * such strings causes asymmetry between serialization and deserialization. it
			 * is possible to avoid this problem by using written-ahead length, however such
			 * use case is rare so it is better prevent deserialization from such strings to
			 * a numeric type explicitly rather than later cause errors that are hard to
			 * detect.
			 */
			if(numChars.length>1 && numChars[0]=='0') {
				throw new IllegalArgumentException("streams contains numeric string that contains leading zero");
			}
			for(byte b:numChars) {
				if(!(b>='0' && b<='9')) {
					throw new IllegalArgumentException("streams contains non-numeric character");
				}
				ret = ret*10 + (b-'0');
				if(ret<0) {
					throw new IllegalArgumentException("numeric string overflows:"+Arrays.toString(numChars));
				}
			}
		} catch (IllegalArgumentException e) {
			throw new ExtendedConversionException(ctx, e.getMessage())
					.withSiteAndOrdinal(Utils.class, 3);
		}
		if(type!=null) {
			Utils.checkRangeInContext(type, ret, ctx);
		}
		return ret;
	}
	
	public static final SimpleDateFormat getThreadLocalDateFormatter(String datePattern) {
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
			//lenient in former versions
			SimpleDateFormat ret = new SimpleDateFormat(p);
			ret.setLenient(false);
			return ret;
		}
	}
		
	static UnsatisfiedConstraintException forContext(Class<?> entity, String field, String error) {
		StringBuilder ret = new StringBuilder();
		if(entity!=null) {
			ret.append("Entity:"+entity);
		}
		if(field!=null) {
			if(ret.length()>0) {
				ret.append(", ");
			}
			ret.append("Field:").append(field);
		}
		if(ret.length()>0) {
			ret.append(", ");
		}
		ret.append("Error:").append(error);
		return new UnsatisfiedConstraintException(ret.toString());
	}
}
