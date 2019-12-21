package io.github.zhtmf.annotations.types;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(FIELD)
public @interface Varint {

    SignedEncoding signedEncoding() default SignedEncoding.TWOS_COMPLEMENT;
    
    public static enum SignedEncoding{
        TWOS_COMPLEMENT,
        ZIGZAG
    }
}
