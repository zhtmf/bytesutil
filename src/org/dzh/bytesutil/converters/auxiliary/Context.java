package org.dzh.bytesutil.converters.auxiliary;

import java.lang.annotation.Annotation;
import java.nio.charset.Charset;

import org.dzh.bytesutil.DataType;
import org.dzh.bytesutil.annotations.modifiers.BigEndian;
import org.dzh.bytesutil.annotations.modifiers.CHARSET;
import org.dzh.bytesutil.annotations.modifiers.DatePattern;
import org.dzh.bytesutil.annotations.modifiers.Length;
import org.dzh.bytesutil.annotations.modifiers.LittleEndian;
import org.dzh.bytesutil.annotations.modifiers.Signed;
import org.dzh.bytesutil.annotations.modifiers.Unsigned;
import org.dzh.bytesutil.annotations.types.RAW;
import org.dzh.bytesutil.converters.auxiliary.ClassInfo.FieldInfo;

/**
 * Information of a specific Field, it is named <code>Context</code> in the
 * sense that it provides clues for conversion between Java types and binary
 * data types.
 * <p>
 * Unlike the internal class {@link FieldInfo}, it provides information from
 * annotations under <tt>annotations.modifiers</tt> package and is used by both
 * internal and custom converters.
 * 
 * @author dzh
 *
 */
public class Context {
	
	private ClassInfo base;
	
	/**
	 * Entity class that declares this field
	 */
	public final Class<?> enclosingEntityClass;
	
	/**
	 * Field name
	 */
	public final String name;
	/**
	 * whether this field is defined as little-endian
	 */
	public final boolean littleEndian;
	/**
	 * whether this field is defined as big-endian
	 */
	public final boolean bigEndian;
	/**
	 * whether this field is defined as signed
	 */
	public final boolean signed;
	/**
	 * whether this field is defined as unsigned
	 */
	public final boolean unsigned;
	/**
	 * Value of {@link Length} annotation.
	 * <p>
	 * If the annotation is absent, or it declares a dynamic length, value of this
	 * field will be -1.
	 */
	public final int length;
	/**
	 * Type of the {@link Length} value in the stream. For example some protocol
	 * defines length as unsigned byte, while others defines it as unsigned short
	 * etc.
	 */
	public final DataType lengthType;
	/**
	 * {@link ModifierHandler} object which is used to obtain the length value
	 * dynamically for this field, null if it is not defined in the {@link Length}
	 * annotation.
	 */
	public final ModifierHandler<Integer> lengthHandler;
	/**
	 * Charset of this field, null if not defined
	 */
	public final Charset charset;
	/**
	 * {@link ModifierHandler} object which is used to obtain Charset object dynamically 
	 * for this field, null if it is not defined in the {@link CHARSET}
	 * annotation.
	 */
	public final ModifierHandler<Charset> charsetHandler;
	/**
	 * Pattern string defined in {@link DatePattern} annotation, null if not present.
	 */
	public final String datePattern;
	
	Context(ClassInfo base, String name) {
		this.base = base;
		this.name = name;
		this.enclosingEntityClass = base.entityClass;
		{
			Annotation ret = mutualExclusive(BigEndian.class,LittleEndian.class);
			this.littleEndian = ret!=null && ret instanceof LittleEndian;
			this.bigEndian = !littleEndian;
		}
		{
			Annotation ret = mutualExclusive(Unsigned.class,Signed.class);
			this.signed = ret!=null && ret instanceof Signed;
			this.unsigned = !signed;
		}
		{
			CHARSET cs = annotation(CHARSET.class);
			if(cs==null) {
				charset = CHARSET.DEFAULT_CHARSET;
				charsetHandler = null;
			}else if( ! PlaceHolderHandler.class.isAssignableFrom(cs.handler())) {
				charset = null;
				try {
					charsetHandler = cs.handler().newInstance();
				} catch (InstantiationException | IllegalAccessException e) {
					throw new RuntimeException(
							String.format("ModifierHandler class [%s] cannot be initialized by no-arg contructor"
									, cs.handler()));
				}
			}else {
				try {
					charset = Charset.forName(cs.value());
				} catch (Exception e) {
					throw new RuntimeException(
							String.format("Charset name [%s] is illegal in field [%s]",cs.value(),name),e);
				}
				charsetHandler = null;
			}
		}
		{
			Length len = localAnnotation(Length.class);
			if(len==null) {
				if(localAnnotation(RAW.class)!=null) {
					throw new IllegalArgumentException(
							String.format("field [%s] is defined as RAW, but a Length annotation is not present on it",name));
				}
				length = -1;
				lengthHandler = null;
				lengthType = null;
			}else {
				this.length = len.value();
				if( ! PlaceHolderHandler.class.isAssignableFrom(len.handler())) {
					try {
						this.lengthHandler = len.handler().newInstance();
					} catch (InstantiationException | IllegalAccessException e) {
						throw new RuntimeException(
								String.format("ModifierHandler class [%s] cannot be initialized by no-arg contructor"
										, len.handler()));
					}
				}else {
					this.lengthHandler = null;
				}
				lengthType = len.type();
			}
		}
		{
			DatePattern df = localAnnotation(DatePattern.class);
			if(df==null) {
				this.datePattern = null;
			}else {
				this.datePattern = df.value();
			}
		}
	}
	
	/**
	 * Annotation of specific class for this field, if the same annotation is
	 * present on both field and class, the one on the field always take precedence.
	 * 
	 * @param annoCls
	 * @return null if it is not present both on the field or on the class
	 */
	public <T extends Annotation> T annotation(Class<T> annoCls) {
		T anno = localAnnotation(annoCls);
		if(anno!=null) {
			return anno;
		}
		return globalAnnotation(annoCls);
	}
	
	/**
	 * Annotation of specific class present on the field.
	 * 
	 * @param annoCls
	 * @return returns null if it is not present on the field, even it is present on
	 *         the class
	 */
	public <T extends Annotation> T localAnnotation(Class<T> annoCls) {
		return (T) base.annotationOfField(name, annoCls);
	}

	/**
	 * Annotation of specific class present on the class definition of this field.
	 * 
	 * @param annoCls
	 * @return	returns null if it is not present
	 */
	public <T extends Annotation> T globalAnnotation(Class<T> annoCls) {
		return base.globalAnnotation(annoCls);
	}
	
	private Annotation mutualExclusive(Class<? extends Annotation> def, Class<? extends Annotation> another) {
		Annotation local1 = localAnnotation(def);
		Annotation local2 = localAnnotation(another);
		if(local1!=null && local2!=null) {
			throw new IllegalArgumentException(
					String.format("[%s] and [%s] should not be both present on the same field declaration",def,another));
		}else if(local1!=null && local2==null) {
			return local1;
		}else if(local1==null && local2!=null) {
			return local2;
		}
		Annotation global1 = globalAnnotation(def);
		Annotation global2 = globalAnnotation(another);
		if(global1!=null && global2!=null) {
			throw new IllegalArgumentException(
					String.format("[%s] and [%s] should not be both present on the same class declaration",def,another));
		}else if(global1!=null && global2==null) {
			return global1;
		}else if(global1==null && global2!=null) {
			return global2;
		}
		return null;
	}
}
