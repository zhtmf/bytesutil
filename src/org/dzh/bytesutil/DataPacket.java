package org.dzh.bytesutil;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
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
import org.dzh.bytesutil.converters.auxiliary.StreamUtils;
import org.dzh.bytesutil.converters.auxiliary.Utils;

/**
 * <p>
 * Entrace of this library.
 * <p> 
 * Make your entity classes subclass of this class and use the
 * {@link #serialize(OutputStream)} and {@link #deserialize(InputStream)} method
 * to serialize entities into or restore them from streams.
 * <p>
 * This class itself is thread-safe, as it does not define any non-static
 * fields. For the same reason, it does not define (and cannot define)
 * reasonable {@link #equals(Object)} and {@link #hashCode()} method, so you
 * should define them in your subclasses.
 * 
 * @author dzh
 *
 */
public class DataPacket {
	
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
	 * Unlike JDK serialization or other serialization methods, serialization
	 * performed by this library does not write any additional information but just
	 * serialize fields into the stream one by one, in the order specified by
	 * {@link Order} annotations and according to rules defined by annotation
	 * classes under <tt>annotations</tt> package.
	 * <p>
	 * Fields can also be specified by no annotation classes under
	 * <tt>annotations</tt> package but defined of type {@link DataPacket}, under
	 * this situation, it should not be null (initialized manually) when this method
	 * is called or its class should defines a no-arg constructor.
	 * 
	 * @param dest
	 *            destination stream of serialization
	 * @throws NullPointerException
	 *             if <tt>dest</tt> is null.
	 */
	@SuppressWarnings("unchecked")
	public void serialize(OutputStream dest) throws ConversionException{
		if(dest==null) {
			throw new NullPointerException("destination stream should not be null");
		}
		
		//lazy initialization
		ClassInfo ci = getClassInfo();
		
		for(FieldInfo fi:ci.fieldInfoList()) {
			//this field is an entity
			if(fi.isEntity) {
				Object obj = fi.get(this);
				if(obj==null) {
					try {
						obj = fi.fieldClass.newInstance();
					} catch (InstantiationException | IllegalAccessException e) {
						throw new ConversionException(
								this.getClass(),fi.name,
								String.format("field  value is null and instance of "
										+ " entity class [%s] cannot be created by calling no-arg constructor"
										, fi.fieldClass),e);
					}
				}
				((DataPacket)obj).serialize(dest);
			//this field is a list
			}else if(fi.listComponentClass!=null) {
				List<Object> value = (List<Object>) fi.get(this);
				value = value == null ? Collections.emptyList() : value;
				/*
				 * validity check is done in ClassInfo
				 */
				int length = Utils.lengthForSerializingListLength(fi, this);
				if(length<0) {
					length = Utils.lengthForSerializingLength(fi, this);
				}
				if(length<0) {
					try {
						length = value.size();
						StreamUtils.writeIntegerOfType(dest, fi.lengthType, value.size(), fi.bigEndian);
					} catch (IOException e) {
						throw new ConversionException(this.getClass(),fi.name,e);
					}
				}
				
				//serialize objects in the list
				//do not use the actual length of the list but the length obtained above
				if(length!=value.size()) {
					throw new ConversionException(
							this.getClass(),fi.name,
							String.format(
									"defined list length [%d] is not the same as length [%d] of list value"
									,length,value.size()));
				}
				Converter<Object> cv = (Converter<Object>) converters.get(fi.listComponentClass);
				if(cv!=null) {
					//class of list elements is pre-defined data types
					for(int i=0;i<length;++i) {
						try {
							cv.serialize(value.get(i), dest, fi, this);
						} catch (UnsupportedOperationException e) {
							throw new ConversionException(
									this.getClass(),fi.name,
									"Unsupported conversion to "+fi.type);
						} catch (Exception e) {
							throw new ConversionException(this.getClass(),fi.name,e);
						}
					}
				}else if(DataPacket.class.isAssignableFrom(fi.listComponentClass)){
					//class of list elements is another Data
					for(int i=0;i<length;++i) {
						((DataPacket)value.get(i)).serialize(dest);
					}
				}else {
					throw new ConversionException(
								this.getClass(),fi.name,
								String.format("component class [%s] is not supported"
										, fi.listComponentClass));
				}
			//a plain field
			}else {
				Converter<Object> cv = (Converter<Object>) converters.get(fi.fieldClass);
				if(cv==null) {
					throw new ConversionException(
							this.getClass(),fi.name,
							String.format("class [%s] is not supported", fi.fieldClass));
				}else {
					try {
						cv.serialize(fi.get(this), dest, fi,this);
					} catch (UnsupportedOperationException e) {
						throw new ConversionException(
								this.getClass(),fi.name,
								"Unsupported conversion to "+fi.type,e);
					} catch (Exception e) {
						throw new ConversionException(this.getClass(),fi.name,e);
					}
				}
			}
		}
	}
	
	/**
	 * <p>
	 * Deserialize entity class from the specified input stream.
	 * <p>
	 * Fields are restored from the stream one by one, in the order specified by
	 * {@link Order} annotations and according to rules defined by annotation
	 * classes under <tt>annotations</tt> package.
	 * <p>
	 * Fields can also be specified by no annotation classes under
	 * <tt>annotations</tt> package but defined of type {@link DataPacket}, under
	 * this situation, it is either non-null (initialized manually) when this method
	 * is called or class of it defines a no-arg constructor.
	 * 
	 * @param src
	 * @throws IOException
	 * @throws NullPointerException
	 *             if <tt>src</tt> is null.
	 */
	@SuppressWarnings({ "unchecked", "resource" })
	public void deserialize(InputStream src) throws ConversionException {
		if(src==null) {
			throw new NullPointerException("source input stream should not be null");
		}
		/*
		 * BufferedInputStream should not be nested or the stream will unexpectedly reach its end.
		 * The src will be a BufferedInputStream if this method is called recursively, if it is not one 
		 * from the very beginning.
		 */
		BufferedInputStream _src = (BufferedInputStream) (
				src instanceof BufferedInputStream ? src : new BufferedInputStream(src));
		
		ClassInfo ci = getClassInfo();
		for(FieldInfo fi:ci.fieldInfoList()) {
			Object value = null;
			//this field is defined as a Data and it is a base class,
			//the actual object should be obtained from a custom handler
			if(fi.variantEntityHandler!=null) {
				DataPacket conditionalObject =
						(DataPacket) fi.variantEntityHandler.handleDeserialize(fi.name,this,_src);
				conditionalObject.deserialize(_src);
				value = conditionalObject;
			//this field is defined as a Data
			}else if(fi.isEntity) {
				DataPacket object = ((DataPacket)fi.get(this));
				if(object==null) {
					try {
						object = (DataPacket) fi.fieldClass.newInstance();
					} catch (InstantiationException | IllegalAccessException e) {
						throw new ConversionException(
								this.getClass(),fi.name,
								String.format("field value is null and"
										+ " instance of the entity class [%s] cannot be created by"
										+ " calling no-arg constructor"
										, fi.name, fi.fieldClass),e);
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
						length = StreamUtils.readIntegerOfType(_src, fi.lengthType, fi.bigEndian);
					} catch (IOException e) {
						throw new ConversionException(this.getClass(),fi.name,e);
					}
				}
				
				Converter<Object> cv = (Converter<Object>) converters.get(fi.listComponentClass);
				//component class is a pre-defined data type
				if(cv!=null) {
					List<Object> tmp = new ArrayList<>(length);
					while(length-->0) {
						try {
							tmp.add(cv.deserialize(_src, fi, this));
						} catch (UnsupportedOperationException e) {
							throw new ConversionException(
									this.getClass(),fi.name,
									"Unsupported conversion to "+fi.type,e);
						} catch (Exception e) {
							throw new ConversionException(this.getClass(),fi.name,e);
						}
					}
					value = tmp;
				//component class is a Data
				}else if(fi.isEntityList){
					List<Object> tmp = new ArrayList<>(length);
					while(length-->0) {
						try {
							DataPacket object = (DataPacket) fi.listComponentClass.newInstance();
							object.deserialize(_src);
							tmp.add(object);
						} catch (InstantiationException | IllegalAccessException e) {
							throw new ConversionException(
									this.getClass(),fi.name,
									String.format(
									"instance of component class [%s] cannot be created by calling no-arg constructor"
									, fi.listComponentClass),e);
						}
					}
					value = tmp;
				}else {
					throw new ConversionException(
							this.getClass(),fi.name,
							String.format(
							"component class [%s] is not supported"
							, fi.listComponentClass));
				}
				
			//a plain field
			}else {
				Converter<Object> cv = (Converter<Object>) converters.get(fi.fieldClass);
				if(cv==null) {
					throw new RuntimeException(fi.fieldClass+" not supported");
				}else {
					try {
						value = cv.deserialize(_src, fi,this);
					} catch (UnsupportedOperationException e) {
						throw new ConversionException(
								this.getClass(),fi.name,
								"Unsupported conversion to "+fi.type);
					} catch (Exception e) {
						throw new ConversionException(this.getClass(),fi.name,e);
					}
				}
			}
			
			fi.set(this, value);
		}
	}
	
	/**
	 * Calculate the length in bytes of this entity as if it was serialized to an
	 * output stream.
	 * <p>
	 * This is <b>NOT</b> a constant time operation as the actual length should and
	 * can only be calculated at runtime.
	 * 
	 * @return length in bytes
	 */
	@SuppressWarnings({ "rawtypes"})
	public int length(){
		ClassInfo ci = getClassInfo();
		int ret = 0;
		for(FieldInfo fi:ci.fieldInfoList()) {
			if(fi.isEntity) {
				DataPacket dp = (DataPacket)fi.get(this);
				if(dp!=null) {
					ret += dp.length();
				}
				continue;
			}
			int length = 0;
			if(fi.listComponentClass!=null) {
				List lst = (List)fi.get(this);
				if(lst!=null) {
					length = lst.size();
					if(fi.isEntityList) {
						for(int i=0;i<length;++i) {
							ret += ((DataPacket)lst.get(i)).length();
						}
						continue;
					}
				}
			}else {
				length = 1;
			}
			if(length==0) {
				continue;
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
				if(size<0) {
					//dynamic length that is written to stream prior to serializing value
					//get the actual value and calculate its length
					Object val = fi.get(this);
					size = val == null ? 0 : val.toString().length();
					//other types have been checked by FieldInfo class
					ret += fi.annotation(Length.class).type().size();
				}
				//length of individual CHAR * size of (maybe) list
				ret += size * length;
				break;
			}
			case RAW:{
				int size = Utils.lengthForSerializingRAW(fi, this);
				if(size<0) {
					Object val = fi.get(this);
					size = val==null ? 0 : Array.getLength(val);
					//other types have been checked by FieldInfo class
					ret += fi.annotation(Length.class).type().size();
				}
				//length of individual byte array * size of (maybe) list
				ret += size * length;
				break;
			}
			default:
				throw new UnsupportedOperationException();
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
