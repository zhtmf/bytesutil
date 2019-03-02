package org.dzh.bytesutil;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.dzh.bytesutil.annotations.modifiers.Length;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.types.BCD;
import org.dzh.bytesutil.converters.Converter;
import org.dzh.bytesutil.converters.auxiliary.ClassInfo;
import org.dzh.bytesutil.converters.auxiliary.DataType;
import org.dzh.bytesutil.converters.auxiliary.FieldInfo;
import org.dzh.bytesutil.converters.auxiliary.MarkableInputStream;
import org.dzh.bytesutil.converters.auxiliary.Utils;
import org.dzh.bytesutil.converters.auxiliary.exceptions.ExtendedConversionException;
import org.dzh.bytesutil.converters.auxiliary.exceptions.UnsatisfiedConstraintException;

/**
 * <p>
 * Entrance of this library.
 * <p>
 * Users make their entity classes subclass of this base class to inherit 3
 * methods from it: {@link #serialize(OutputStream) serialize},
 * {@link #deserialize(InputStream) deserialize}, {@link #length() length} which
 * helps converting entity class to/from byte streams.
 * <p>
 * This class itself is thread-safe as it does not define any non-static fields.
 * <p>
 * It is declared as abstract without defining any abstract methods only to
 * remind users that it should not be used alone but subclassed.
 * 
 * @author dzh
 *
 */
public abstract class DataPacket {
	
	//thread-safe map of class info objects
	private static final ConcurrentHashMap<Class<?>,ClassInfo> 
		classInfoMap = new ConcurrentHashMap<>();
	
	/**
	 * <p>
	 * Serialize entity class into the specified output stream.
	 * <p>
	 * Non-final, non-static fields annotated with {@link Order} are processed while
	 * other fields are effectively ignored. Fields are processed according to their
	 * ascending order as specified by {@link Order#value()}.
	 * 
	 * @param dest
	 *            destination stream of serialization
	 * @throws ConversionException
	 *             If invalid input encountered during runtime.
	 * @throws IllegalArgumentException
	 *             If initial parsing of annotations on entity class or fields
	 *             failed, this exception should be eliminated during compile time
	 *             but not caught and handled during runtime.
	 * @throws NullPointerException
	 *             if <tt>dest</tt> is null.
	 */
	public void serialize(OutputStream dest) throws ConversionException, IllegalArgumentException{
		if(dest==null) {
			throw new NullPointerException();
		}
		
		//lazy initialization
		ClassInfo ci = getClassInfo();
		
		for(FieldInfo fi:ci.fieldInfoList()) {
			
			Object value = fi.get(this);
			if(value==null) {
				/*
				 * null values shall not be permitted as it may be impossible 
				 * to deserialize the byte sequence generated
				 * Note: this modification causes incompatibility with former releases
				 */
				throw new ExtendedConversionException(this.getClass(),fi.name,
						"this field is intended to be processed but its value is null")
						.withSiteAndOrdinal(DataPacket.class, 0);
			}
			
			try {
				@SuppressWarnings("unchecked")
				Converter<Object> cv = (Converter<Object>)fi.converter;
				cv.serialize(value, dest, fi, this);
			} catch(ConversionException e) {
				throw e;
			} catch (Exception e) {
				throw new ExtendedConversionException(this.getClass(),fi.name,e)
				.withSiteAndOrdinal(DataPacket.class, 4);
			}
		}
	}
	
	/**
	 * <p>
	 * Deserialize entity class from the specified input stream.
	 * <p>
	 * Non-final, non-static fields annotated with {@link Order} are processed while
	 * other fields are effectively ignored. Fields are processed according to their
	 * ascending order as specified by {@link Order#value()}.
	 * <p>
	 * Fields declared as subtypes of {@link DataPacket} should declare a no-arg
	 * constructor and that dataType should be accessible (not a non-static inner class
	 * or a private inner class).
	 * 
	 * @param src
	 *            the input stream
	 * @throws ConversionException
	 *             If invalid input encountered during runtime.
	 * @throws IllegalArgumentException
	 *             If initial parsing of annotations on entity class or fields
	 *             failed, this exception should be eliminated during compile time
	 *             but not caught and handled during runtime.
	 * @throws NullPointerException
	 *             if <tt>src</tt> is null.
	 */
	public void deserialize(InputStream src) throws ConversionException, IllegalArgumentException {
		if(src==null) {
			throw new NullPointerException();
		}
		deserialize0(new MarkableInputStream(src));
	}
	
	private void deserialize0(MarkableInputStream _src) throws ConversionException {
		ClassInfo ci = getClassInfo();
		for(FieldInfo fi:ci.fieldInfoList()) {
			Object value = null;
			@SuppressWarnings("unchecked")
			Converter<Object> cv = (Converter<Object>)fi.converter;
			try {
				value = cv.deserialize(_src, fi, this);
			} catch(ConversionException e) {
				throw e;
			} catch (Exception e) {
				throw new ExtendedConversionException(this.getClass(),fi.name,e)
						.withSiteAndOrdinal(DataPacket.class, 14);
			}
			fi.set(this, value);
		}
	}

	/**
	 * Calculate the length in bytes of this object as if it was serialized to an
	 * output stream.
	 * <p>
	 * This is <b>NOT</b> a constant time operation as the actual length should and
	 * can only be calculated at runtime.
	 * 
	 * @throws IllegalArgumentException
	 *             If initial parsing failed or other preliminaries not satisfied.
	 *             Some errors may be better expressed by a
	 *             {@link ConversionException} but we use an
	 *             {@link IllegalArgumentException} to keep compatible with old
	 *             versions;
	 * 
	 * @return length in bytes
	 */
	public int length() throws IllegalArgumentException{
		ClassInfo ci = getClassInfo();
		int ret = 0;
		for(FieldInfo fi:ci.fieldInfoList()) {
			Object value = fi.get(this);
			if(value==null) {
				throw new UnsatisfiedConstraintException(
						fi.name + " is intended to be processed but its value is null")
						.withSiteAndOrdinal(DataPacket.class, 20);
						
			}
			if(fi.isEntity) {
				DataPacket dp = (DataPacket)value;
				ret += dp.length();
				continue;
				
			}
			int length = 0;
			if(fi.listComponentClass!=null) {
				length = Utils.lengthForList(fi, this);
				@SuppressWarnings("rawtypes")
				List lst = (List)value;
				if(length<0) {
					//write ahead
					//size of the write-ahead length should be considered
					//even the list itself is null or empty
					ret += fi.lengthType().size();
					//use the defined length rather than the actual list size
					length = lst.size();
				}
				if(fi.isEntityList) {
					for(int i=0;i<length;++i) {
						ret += ((DataPacket)lst.get(i)).length();
					}
					continue;
				}
			}else {
				length = 1;
			}
			DataType type = fi.dataType;
			switch(type) {
			case BCD:
				ret += ((BCD)fi.localAnnotation(BCD.class)).value() * length;
				break;
			case BYTE:
			case SHORT:
			case INT:
			case LONG:
				ret += type.size() * length;
				break;
			case CHAR:{
				int size = Utils.lengthForSerializingCHAR(fi, this);
				if(size>=0) {
					//explicitly declared size
					ret += size * length;
				}else {
					//dynamic length that is written to stream prior to serializing value
					//size should be retrieved inspecting the value itself
					//or in case of a list, inspecting values for EACH element
					size = 0;
					DataType lengthType = fi.annotation(Length.class).type();
					Charset cs = Utils.charsetForSerializingCHAR(fi, this);
					if(value instanceof List) {
						@SuppressWarnings("rawtypes")
						List lst = (List)value;
						for(int i=0;i<lst.size();++i) {
							value = lst.get(i);
							size += lengthType.size();
							size += value.toString().getBytes(cs).length;
						}
					}else if(value instanceof Date) {
						size += lengthType.size();
						size += Utils.getThreadLocalDateFormatter(fi.datePattern).format((Date)value).length();
					}else {
						size += lengthType.size();
						size += value.toString().getBytes(cs).length;
					}
					ret += size;
				}
				break;
			}
			case RAW:{
				int size = Utils.lengthForSerializingRAW(fi, this);
				if(size>=0) {
					ret += size * length;
				}else {
					size = 0;
					DataType lengthType = fi.annotation(Length.class).type();
					if(value instanceof List) {
						@SuppressWarnings("rawtypes")
						List lst = (List)value;
						for(int i=0;i<lst.size();++i) {
							value = lst.get(i);
							size += lengthType.size();
							size += Array.getLength(value);
						}
					}else {
						size += lengthType.size();
						size += Array.getLength(value);
					}
					ret += size;
				}
				break;
			}
			case USER_DEFINED:
				int size = Utils.lengthForSerializingUserDefinedType(fi, this);
				ret += size * length;
				break;
			}
		}
		return ret;
	}
	
	//lazy initialization
	private ClassInfo getClassInfo() {
		Class<?> self = this.getClass();
		ClassInfo ci = classInfoMap.get(self);
		if(ci==null) {
			//may suffer from duplicated creating
			//but the penalty is trivial 
			ci = new ClassInfo(self);
			classInfoMap.put(self, ci);
		}
		return ci;
	}
}
