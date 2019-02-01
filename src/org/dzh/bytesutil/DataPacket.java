package org.dzh.bytesutil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.dzh.bytesutil.annotations.modifiers.Length;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.types.BCD;
import org.dzh.bytesutil.converters.ByteArrayConverter;
import org.dzh.bytesutil.converters.ByteConverter;
import org.dzh.bytesutil.converters.CharConverter;
import org.dzh.bytesutil.converters.Converter;
import org.dzh.bytesutil.converters.DateConverter;
import org.dzh.bytesutil.converters.IntArrayConverter;
import org.dzh.bytesutil.converters.IntegerConverter;
import org.dzh.bytesutil.converters.LongConverter;
import org.dzh.bytesutil.converters.ShortConverter;
import org.dzh.bytesutil.converters.StringConverter;
import org.dzh.bytesutil.converters.auxiliary.ClassInfo;
import org.dzh.bytesutil.converters.auxiliary.DataType;
import org.dzh.bytesutil.converters.auxiliary.FieldInfo;
import org.dzh.bytesutil.converters.auxiliary.MarkableInputStream;
import org.dzh.bytesutil.converters.auxiliary.StreamUtils;
import org.dzh.bytesutil.converters.auxiliary.Utils;
import org.dzh.bytesutil.converters.auxiliary.exceptions.ExtendedConversionException;
import org.dzh.bytesutil.converters.auxiliary.exceptions.UnsatisfiedConstraintException;

/**
 * <p>
 * Entrance of this library.
 * <p>
 * Users make their entity classes subclass of this base class to inherit 3 methods
 * from it: {@link #serialize(OutputStream) serialize},
 * {@link #deserialize(InputStream) deserialize}, {@link #length() length} which
 * helps converting entity class to/from byte streams.
 * <p>
 * This class itself is thread-safe as it does not define any non-static
 * fields.
 * <p>
 * It does not define any abstract methods but declared as abstract to remind
 * users that it should not be used alone but subclassed.
 * 
 * @author dzh
 *
 */
public abstract class DataPacket {
	
	//thread-safe map of class info objects
	private static final ConcurrentHashMap<Class<?>,ClassInfo> 
		classInfoMap = new ConcurrentHashMap<>();
	
	//all built-in converters that are initialized on startup
	//this map is made unmodifiable to ensure thread safety
	private static final Map<Class<?>,Converter<?>> converters;
	static {
		Map<Class<?>,Converter<?>> tmp = new HashMap<>();
		tmp.put(Byte.class, new ByteConverter());
		tmp.put(byte.class, tmp.get(Byte.class));
		tmp.put(Short.class, new ShortConverter());
		tmp.put(short.class, tmp.get(Short.class));
		tmp.put(Integer.class, new IntegerConverter());
		tmp.put(int.class, tmp.get(Integer.class));
		tmp.put(String.class, new StringConverter());
		tmp.put(Character.class, new CharConverter());
		tmp.put(char.class, tmp.get(Character.class));
		tmp.put(byte[].class, new ByteArrayConverter());
		tmp.put(int[].class, new IntArrayConverter());
		tmp.put(java.util.Date.class, new DateConverter());
		tmp.put(Long.class, new LongConverter());
		tmp.put(long.class, tmp.get(Long.class));
		converters = Collections.unmodifiableMap(tmp);
	}

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
				 * to deserialize the byte sequence generated using the same scheme
				 * Note: this modification causes incompatibility with former versions
				 */
				throw new ExtendedConversionException(this.getClass(),fi.name,
						"this field is intended to be processed but its value is null")
						.withSiteAndOrdinal(DataPacket.class, 0);
			}
			
			//this field is an entity
			if(fi.isEntity) {
				((DataPacket)value).serialize(dest);
			//this field is a list
			}else if(fi.listComponentClass!=null) {
				@SuppressWarnings("unchecked")
				List<Object> listValue = (List<Object>)value;
				//validity check is done in ClassInfo
				int length = Utils.lengthForList(fi, this);
				if(length<0) {
					length = listValue.size();
					try {
						StreamUtils.writeIntegerOfType(dest, fi.lengthType(), listValue.size(), fi.bigEndian);
					} catch (IOException e) {
						throw new ExtendedConversionException(this.getClass(),fi.name,e)
							.withSiteAndOrdinal(DataPacket.class, 1);
					}
				}
				
				//serialize objects in the list
				//do not use the actual length of the list but the length obtained above
				if(length!=listValue.size()) {
					throw new ExtendedConversionException(
							this.getClass(),fi.name,
							String.format(
									"defined list length [%d] is not the same as length [%d] of list value"
									,length,listValue.size()))
								.withSiteAndOrdinal(DataPacket.class, 2);
				}
				@SuppressWarnings("unchecked")
				Converter<Object> cv = (Converter<Object>) converters.get(fi.listComponentClass);
				if(cv!=null) {
					//class of list elements is pre-defined data types
					try {
						for(int i=0;i<length;++i) {
							Object elem = listValue.get(i);
							if(elem==null) {
								throw new ExtendedConversionException(
										this.getClass(),fi.name,
										"list contains null value")
										.withSiteAndOrdinal(DataPacket.class, -1);
							}
							cv.serialize(elem, dest, fi, this);
						}
					} catch (UnsupportedOperationException e) {
						throw new ExtendedConversionException(
								this.getClass(),fi.name,
								"Unsupported conversion to "+fi.type)
							.withSiteAndOrdinal(DataPacket.class, 3);
					} catch(ConversionException e) {
						throw e;
					} catch (Exception e) {
						throw new ExtendedConversionException(this.getClass(),fi.name,e)
						.withSiteAndOrdinal(DataPacket.class, 4);
					}
				}else if(fi.isEntityList){
					//class of list elements is another DataPacket
					for(int i=0;i<length;++i) {
						Object elem = listValue.get(i);
						if(elem==null) {
							throw new ExtendedConversionException(
									this.getClass(),fi.name,
									"list contains null value")
									.withSiteAndOrdinal(DataPacket.class, -2);
						}
						((DataPacket)elem).serialize(dest);
					}
				}else {
					throw new ExtendedConversionException(
								this.getClass(),fi.name,
								String.format("component class [%s] is not supported"
										, fi.listComponentClass))
							.withSiteAndOrdinal(DataPacket.class, 5);
				}
			//a plain field
			}else {
				@SuppressWarnings("unchecked")
				Converter<Object> cv = (Converter<Object>) converters.get(fi.getFieldType());
				if(cv==null) {
					throw new ExtendedConversionException(
							this.getClass(),fi.name,
							String.format("class [%s] is not supported", fi.getFieldType()))
								.withSiteAndOrdinal(DataPacket.class, 7);
				}else {
					try {
						cv.serialize(value, dest, fi,this);
					} catch (UnsupportedOperationException e) {
						throw new ExtendedConversionException(
								this.getClass(),fi.name,
								"Unsupported conversion to "+fi.type,e)
								.withSiteAndOrdinal(DataPacket.class, 8);
					} catch(ConversionException e) {
						throw e;
					} catch (Exception e) {
						throw new ExtendedConversionException(this.getClass(),fi.name,e)
								.withSiteAndOrdinal(DataPacket.class, 9);
					}
				}
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
	 * constructor and that type should be accessible (not a non-static inner class
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
	
	@SuppressWarnings({ "unchecked"})
	private void deserialize0(MarkableInputStream _src) throws ConversionException {
		ClassInfo ci = getClassInfo();
		for(FieldInfo fi:ci.fieldInfoList()) {
			Object value = null;
			//this field is defined as a DataPacket
			if(fi.isEntity) {
				DataPacket object = ((DataPacket)fi.get(this));
				if(object==null) {
					try {
						object = fi.entityCreator.handleDeserialize(fi.name, this, _src);
					} catch (Exception e) {
						throw new ExtendedConversionException(
								this.getClass(),fi.name,
								String.format("field value is null and"
										+ " instance of the entity class [%s] cannot be created"
										, fi.name, fi.getFieldType()),e)
								.withSiteAndOrdinal(DataPacket.class, 11);
					}
				}
				object.deserialize(_src);
				value = object;
			//this field is defined as a list
			}else if(fi.listComponentClass!=null) {
				
				int length = Utils.lengthForDeserializingListLength(fi, this, _src);
				if(length<0) {
					length = Utils.lengthForDeserializingLength(fi, this, _src);
				}
				if(length<0) {
					try {
						length = StreamUtils.readIntegerOfType(_src, fi.lengthType(), fi.bigEndian);
					} catch (IOException e) {
						throw new ExtendedConversionException(this.getClass(),fi.name,e)
								.withSiteAndOrdinal(DataPacket.class, 12);
					}
				}
				
				Converter<Object> cv = (Converter<Object>) converters.get(fi.listComponentClass);
				List<Object> tmp = null;
				//component class is a pre-defined data type
				if(cv!=null) {
					try {
						tmp = new ArrayList<>(length);
						while(length-->0) {
							tmp.add(cv.deserialize(_src, fi, this));
						}
					} catch (UnsupportedOperationException e) {
						throw new ExtendedConversionException(
								this.getClass(),fi.name,
								"Unsupported conversion to "+fi.type,e)
								.withSiteAndOrdinal(DataPacket.class, 13);
					} catch(ConversionException e) {
						throw e;
					} catch (Exception e) {
						throw new ExtendedConversionException(this.getClass(),fi.name,e)
								.withSiteAndOrdinal(DataPacket.class, 14);
					}
					value = tmp;
				//component class is subclass of DataPacket
				}else if(fi.isEntityList){
					try {
						tmp = new ArrayList<>(length);
						while(length-->0) {
							DataPacket object = (DataPacket) fi.entityCreator.handleDeserialize(fi.name, this, _src);
							object.deserialize(_src);
							tmp.add(object);
						}
					} catch (Exception e) {
						throw new ExtendedConversionException(
								this.getClass(),fi.name,
								String.format(
								"instance of component class [%s] cannot be created by calling no-arg constructor"
								, fi.listComponentClass),e)
							.withSiteAndOrdinal(DataPacket.class, 15);
					}
					value = tmp;
				}else {
					throw new ExtendedConversionException(
							this.getClass(),fi.name,
							String.format(
							"component class [%s] is not supported"
							, fi.listComponentClass))
							.withSiteAndOrdinal(DataPacket.class, 16);
				}
				
			//a plain field
			}else {
				Converter<Object> cv = (Converter<Object>) converters.get(fi.getFieldType());
				if(cv==null) {
					throw new ExtendedConversionException(this.getClass(),fi.name,fi.getFieldType()+" not supported")
					.withSiteAndOrdinal(DataPacket.class, 17);
				}else {
					try {
						value = cv.deserialize(_src, fi,this);
					} catch (UnsupportedOperationException e) {
						throw new ExtendedConversionException(
								this.getClass(),fi.name,
								"Unsupported conversion to "+fi.type)
						.withSiteAndOrdinal(DataPacket.class, 18);
					} catch(ConversionException e) {
						throw e;
					} catch (Exception e) {
						throw new ExtendedConversionException(this.getClass(),fi.name,e)
						.withSiteAndOrdinal(DataPacket.class, 19);
					}
				}
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
						fi.name + " is intended to be processed but its value is null"
						, DataPacket.class, 20);
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
			DataType type = fi.type;
			switch(type) {
			case BCD:
				ret += ((BCD)fi.localAnnotation(BCD.class)).value() * length;
				break;
			case BYTE:
			case SHORT:
			case INT:
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
