package io.github.zhtmf.annotations.modifiers;

import java.io.InputStream;
import java.nio.charset.Charset;

import io.github.zhtmf.converters.auxiliary.ModifierHandler;

class PlaceHolderHandler<E> extends ModifierHandler<E> {
    @Override
    public E handleDeserialize0(String fieldName, Object entity, InputStream is) {
        throw new UnsupportedOperationException("this implementation should not be called");
    }
    @Override
    public E handleSerialize0(String fieldName, Object entity) {
        throw new UnsupportedOperationException("this implementation should not be called");
    }
    static class DefaultCharsetHandler extends PlaceHolderHandler<Charset>{}
    static class DefaultLengthHandler extends PlaceHolderHandler<Integer>{}
}
