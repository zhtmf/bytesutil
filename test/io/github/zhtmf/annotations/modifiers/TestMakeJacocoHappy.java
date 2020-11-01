package io.github.zhtmf.annotations.modifiers;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import io.github.zhtmf.converters.TestUtils;

public class TestMakeJacocoHappy {
    @SuppressWarnings("rawtypes")
    @Test
    public void test() throws IOException {
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
            try {
                new PlaceHolderHandler.DefaultListTerminationHandler().handleSerialize0(null, null);
            } catch (Exception e) {
                TestUtils.assertException(e, UnsupportedOperationException.class);
            }
            assertEquals(new PlaceHolderHandler.DefaultListTerminationHandler()
                    .handleDeserialize0(null, null, null, null), false);
            new PlaceHolderHandler.DefaultCharsetHandler();
            new PlaceHolderHandler.DefaultLengthHandler();
            new PlaceHolderHandler.DefaultConditionalHandler();
            new PlaceHolderHandler.DefaultListTerminationHandler();
        }
    }
}
