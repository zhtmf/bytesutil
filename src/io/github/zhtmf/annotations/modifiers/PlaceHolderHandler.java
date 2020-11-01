package io.github.zhtmf.annotations.modifiers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

import io.github.zhtmf.converters.auxiliary.ListTerminationHandler;
import io.github.zhtmf.converters.auxiliary.ModifierHandler;

/**
 * Used as a default place holder value for annotation properties of
 * {@link ModifierHandler} type as default value of annotation properties
 * cannot be {@code null}
 * 
 * @author dzh
 */
class PlaceHolderHandler<E> extends ModifierHandler<E> {
    @Override
    public E handleDeserialize0(String fieldName, Object entity, InputStream in) {
        throw new UnsupportedOperationException("this implementation should not be called");
    }
    @Override
    public E handleSerialize0(String fieldName, Object entity) {
        throw new UnsupportedOperationException("this implementation should not be called");
    }
    static class DefaultCharsetHandler extends PlaceHolderHandler<Charset>{}
    static class DefaultLengthHandler extends PlaceHolderHandler<Integer>{}
    static class DefaultConditionalHandler extends PlaceHolderHandler<Boolean>{}
    static class DefaultListTerminationHandler extends ListTerminationHandler{
        @Override
        public boolean handleDeserialize0(String fieldName, Object entity, InputStream in, List<Object> list)
                throws IOException {
            return false;
        }
    }
}
