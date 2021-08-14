package io.github.zhtmf.annotations.modifiers;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.github.zhtmf.converters.auxiliary.EntityHandler;

/**
 * An auxiliary annotation used to mark fields whose value should be carried
 * over from other objects.
 * <p>
 * Because this library adopts the idea which separates different parts of a
 * binary protocol into different Java classes (entity classes), then if there
 * are some common data referenced through out the whole procedure they have to
 * be passed between classes via {@link EntityHandler}s. Sometimes users have to
 * declare {@link EntityHandler}s solely for this purpose, which is quite
 * cumbersome.
 * <p>
 * This annotation is used to automate this procedure. Here we reference to
 * classes that have one or more field or methods annotated with this annotation
 * as "Child" classes and Classes containing fields typed with Child classes and
 * data to be passed to them as "Parent" classes. This annotation automatically
 * copy data from Parent classes to Child classes during deserializing Child
 * classes by directly copying values between fields or by invoking methods.
 * <p>
 * For example, the following code:
 * 
 * <pre>
 * public class Parent extends DataPacket {
 * 	&#64;Order(0)
 * 	&#64;Char(3)
 * 	&#64;Length(4)
 * 	public List&lt;String&gt; names;
 * 
 * 	&#64;Order(1)
 * 	&#64;Variant(ChildHandler.class)
 * 	public Child childObject;
 * 
 * 	public static final class ChildHandler extends EntityHandler {
 * 		public DataPacket handle0(String fieldName, Object entity, InputStream is) throws IOException {
 * 			Child child = new Child();
 * 			child.names = ((Parent) entity).names;
 * 			return child;
 * 		}
 * 	}
 * }
 * 
 * public class Child extends DataPacket {
 * 	public List&lt;String&gt; names;
 * 	// may have other fields specific to Child
 * }
 * </pre>
 * <p>
 * becomes the following after using this annotation:
 * 
 * <pre>
 * public class Parent extends DataPacket {
 * 	&#64;Order(0)
 * 	&#64;Char(3)
 * 	&#64;Length(4)
 * 	public List&lt;String&gt; names;
 * 
 * 	&#64;Order(1)
 * 	public Child childObject;
 * }
 * 
 * public class Child extends DataPacket {
 * 	&#64;Injectable
 * 	// receives value from Parent.names
 * 	public List&lt;String&gt; names;
 * 	// may have other fields specific to Child
 * }
 * </pre>
 * 
 * <p>
 * When used on fields:
 * <ul>
 * <li>Static fields are ignored.</li>
 * <li>Type check is done at <b>runtime</b>.</li>
 * <li>Inherited fields will be processed if they are also annotated with this
 * annotation.</li>
 * <li>Fields in Child class and Parent class should have same name, otherwise
 * an alias for field in Parent class should be given by {@link #value() value}
 * property.</li>
 * </ul>
 * <p>
 * When used on methods:
 * <ul>
 * <li>Static methods are ignored.</li>
 * <li>Methods should have only one parameter, however type check is done at
 * <b>runtime</b>.</li>
 * <li>Annotated methods does not need to have the signature of a
 * <code>Setter</code>. If they have, their corresponding property names (such as
 * <code>authorName</code> is the property name of
 * <code>void setAuthorName(...)</code> ) and if they dont't have, their
 * original names are used to lookup the corresponding field in Parent object. If
 * this is not desired, an alias from {@link #value() value} can be used to
 * override it.</li>
 * </ul>
 * <p>
 * Whether the corresponding field exists in Parent object is checked at
 * <b>runtime</b>. Because at compile time this library does not know how the
 * annotated Child class will be used.
 * <p>
 * As this annotation is about fields that are not part of the binary protocol
 * but some common data shared between entity objects. So an exception will be
 * thrown if a field is simultaneously annotated with this annotation and
 * {@link Order};
 * 
 * @author dzh
 */
@Retention(RUNTIME)
@Target({ FIELD, METHOD })
public @interface Injectable {
	
	/**
	 * An alias used to override the inferred name in Parent class, if this property
	 * returns a String that is not empty.
	 * 
	 * @return	an alias used to override the inferred name in Parent class.
	 */
	String value() default "";
}
