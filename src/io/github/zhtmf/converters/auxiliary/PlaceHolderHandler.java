package io.github.zhtmf.converters.auxiliary;

import java.io.InputStream;
import java.nio.charset.Charset;

public class PlaceHolderHandler<E> extends ModifierHandler<E> {
    @Override
    public E handleDeserialize0(String fieldName, Object entity, InputStream is) {
        throw new UnsupportedOperationException("this implementation should not be called");
    }
    @Override
    public E handleSerialize0(String fieldName, Object entity) {
        throw new UnsupportedOperationException("this implementation should not be called");
    }
    
    public static class DefaultCharsetHandler extends PlaceHolderHandler<Charset>{}
    public static class DefaultLengthHandler extends PlaceHolderHandler<Integer>{}
}
