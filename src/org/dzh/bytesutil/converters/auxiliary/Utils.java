package org.dzh.bytesutil.converters.auxiliary;

import java.nio.charset.Charset;

import org.dzh.bytesutil.annotations.types.CHAR;
import org.dzh.bytesutil.annotations.types.RAW;

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
	
	/**
	 * @param val
	 * @param sizeClass	not data type but rather use number classes in Java to denote size of a number in bytes.
	 * @param unsigned
	 */
	public static void checkRange(long val, Class<? extends Number> sizeClass, boolean unsigned) {
		if(sizeClass == Byte.class) {
			if(unsigned) {
				if(val<0 || val>((long)Byte.MAX_VALUE*2+1)) {
					throw new IllegalArgumentException(String.format("val [%d] cannot be stored as unsigned 1-byte integer value",val));
				}
			}else {
				if(val<Byte.MIN_VALUE || val>Byte.MAX_VALUE) {
					throw new IllegalArgumentException(String.format("val [%d] cannot be stored as signed 1-byte integer value",val));
				}
			}
		}else if(sizeClass == Short.class) {
			if(unsigned) {
				if(val<0 || val>((long)Short.MAX_VALUE*2+1)) {
					throw new IllegalArgumentException(String.format("val [%d] cannot be stored as unsigned 2-byte integer value",val));
				}
			}else {
				if(val<Short.MIN_VALUE || val>Short.MAX_VALUE) {
					throw new IllegalArgumentException(String.format("val [%d] cannot be stored as signed 2-byte integer value",val));
				}
			}
		}else if(sizeClass == Integer.class) {
			if(unsigned) {
				if(val<0 || val>((long)Integer.MAX_VALUE*2+1)) {
					throw new IllegalArgumentException(String.format("val [%d] cannot be stored as unsigned 4-byte integer value",val));
				}
			}else {
				if(val<Integer.MIN_VALUE || val>Integer.MAX_VALUE) {
					throw new IllegalArgumentException(String.format("val [%d] cannot be stored as signed 4-byte integer value",val));
				}
			}
		}else if(sizeClass == Long.class) {
			if(unsigned && val<0) {
				throw new IllegalArgumentException(String.format("val [%d] cannot be stored as unsigned 8-byte integer value",val));
			}
			return;
		}
	}
	
	static IllegalArgumentException forContext(Class<?> entity, String field, String error) {
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
		return new IllegalArgumentException(ret.toString());
	}
}
