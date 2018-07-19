package org.dzh.bytesutil;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
import org.dzh.bytesutil.converters.auxiliary.ClassInfo.FieldInfo;
import org.dzh.bytesutil.converters.auxiliary.Context;
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
				Context ctx = ci.contextOfField(fi.name);
				List<Object> value = (List<Object>) fi.get(this);
				/*
				 * validity check is done in ClassInfo
				 */
				int length = Utils.lengthForSerializingListLength(ctx, this);
				if(length<0) {
					length = Utils.lengthForSerializingLength(ctx, this);
				}
				if(length<0) {
					try {
						length = value.size();
						StreamUtils.writeIntegerOfType(dest, ctx.lengthType, value.size(), ctx.bigEndian);
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
							cv.serialize(value.get(i), fi.type, dest, ctx, this);
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
						cv.serialize(fi.get(this), fi.type, dest, ci.contextOfField(fi.name),this);
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
				Context ctx = ci.contextOfField(fi.name);
				
				int length = Utils.lengthForDeserializingListLength(ctx, this, _src);
				if(length<0) {
					length = Utils.lengthForDeserializingLength(ctx, this, _src);
				}
				if(length<0) {
					try {
						length = StreamUtils.readIntegerOfType(_src, ctx.lengthType, ctx.bigEndian);
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
							tmp.add(cv.deserialize(fi.type, _src, ctx, this));
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
						value = cv.deserialize(fi.type, _src, ci.contextOfField(fi.name),this);
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
	 * Calculate the length in bytes as if this entity has been serialized to an
	 * output stream.
	 * <p>
	 * This is <b>NOT</b> a constant time operation, as the actual length should and
	 * can only be calculated at runtime.
	 * 
	 * @return	length in bytes
	 */
	@SuppressWarnings("rawtypes")
	public int length(){
		ClassInfo ci = getClassInfo();
		int ret = 0;
		for(FieldInfo fi:ci.fieldInfoList()) {
			int length = fi.listComponentClass!=null ? ((List)fi.get(this)).size() : 1;
			if(length==0) {
				continue;
			}
			if(fi.type==null) {
				if(DataPacket.class.isAssignableFrom(fi.fieldClass)) {
					assert length==1;
					ret += ((DataPacket)fi.get(this)).length() * length;
				}else {
					assert DataPacket.class.isAssignableFrom(fi.listComponentClass);
					List lst = ((List)fi.get(this));
					for(int i=0;i<lst.size();++i) {
						ret += ((DataPacket)lst.get(i)).length();
					}
				}
			}else {
				switch(fi.type) {
				case BCD:
					ret += ((BCD)fi.annotations.get(BCD.class)).value() * length;
					break;
				case BYTE:
					ret += 1 * length;
					break;
				case SHORT:
					ret += 2 * length;
					break;
				case INT:
					ret += 4 * length;
					break;
				case CHAR:
					ret += Utils.lengthForSerializingCHAR(ci.contextOfField(fi.name), this) * length;
					break;
				case RAW:
					ret += Utils.lengthForSerializingRAW(ci.contextOfField(fi.name), this) * length;
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
