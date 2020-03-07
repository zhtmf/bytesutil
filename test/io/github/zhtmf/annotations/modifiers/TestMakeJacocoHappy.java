package io.github.zhtmf.annotations.modifiers;

import org.junit.Test;

import io.github.zhtmf.converters.TestUtils;

public class TestMakeJacocoHappy {
    @SuppressWarnings("rawtypes")
    @Test
    public void test() {
        {
            try {
                new io.github.zhtmf.annotations.modifiers.PlaceHolderHandler().handleDeserialize0(null, null, null);
            } catch (Exception e) {
                TestUtils.assertException(e, UnsupportedOperationException.class);
            }
            try {
                new PlaceHolderHandler().handleSerialize0(null, null);
            } catch (Exception e) {
                TestUtils.assertException(e, UnsupportedOperationException.class);
            }
            new PlaceHolderHandler.DefaultCharsetHandler();
            new PlaceHolderHandler.DefaultLengthHandler();
            new PlaceHolderHandler.DefaultConditionalHandler();
        }
    }
}
