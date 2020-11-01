package io.github.zhtmf.annotations.modifiers;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.github.zhtmf.converters.auxiliary.ListTerminationHandler;
import io.github.zhtmf.converters.auxiliary.ModifierHandler;

/**
 * Similar with {@link EndsWith}, it extends the concept of dynamic list length
 * further that under some situations length of a list is neither static nor
 * calculated but depends on external conditions at runtime.
 * <p>
 * It enables using a modified {@link ModifierHandler} to encapsulate additional
 * logic during deserialization of a list. Users can refer to external resources
 * or even modify the list itself within this handler.
 * <p>
 * The handler is "modified" in the sense that its overloaded
 * {@link ListTerminationHandler#handleDeserialize0(String, Object, java.io.InputStream, java.util.List)
 * handleDeserialize0} method possesses an additional parameter which is the
 * list in question.
 * <p>
 * The handler is called before every element is deserialized and added to the
 * list, which means during the first call the list object passed as the last
 * argument is empty.
 * <p>
 * The list is not assigned to the corresponding field before the
 * deserialization process of the field is finished. It should be treated as a
 * temporary object by user codes.
 * <p>
 * Another property {@link #value() value} of this annotation, similar with
 * {@link EndsWith#value()} represents another typical situation that
 * termination of a list is marked by special sequence of bytes in the stream.
 * It is implemented by a <tt>ListTerminationHandler</tt> under the hood.
 * <p>
 * {@link #value() Value} and {@link #handler() handler} should not be both left
 * unassigned. And only one of {@link Length}, {@link ListLength} and this
 * annotation can be used to annotate a list field.
 * 
 * @author dzh
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface ListEndsWith {
    /**
     * The sequence of bytes which marks termination of the annotated list if it is
     * found in the stream.
     * 
     * @return the marker array, the default value (an empty value) disables this
     *         feature.
     */
    byte[] value() default {};

    /**
     * The custom {@link ListTerminationHandler} (a subclass of
     * <tt>ModifierHandler</tt>) which encapsulates additional logic during
     * deserialization of the annotated list.
     * 
     * @return class object of the custom {@link ListTerminationHandler}, the
     *         default value (a special package private class) disables this
     *         feature.
     */
    Class<? extends ListTerminationHandler> handler() default PlaceHolderHandler.DefaultListTerminationHandler.class;
}
