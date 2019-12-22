package examples.ber;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.BigEndian;
import io.github.zhtmf.annotations.modifiers.Conditional;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Signed;
import io.github.zhtmf.annotations.types.BYTE;
import io.github.zhtmf.annotations.types.Bit;
import io.github.zhtmf.annotations.types.Varint;
import io.github.zhtmf.converters.auxiliary.ModifierHandler;

@BigEndian
public class BERData extends DataPacket {
    /**
     * bits 8 and 7 shall be encoded to represent the class of the tag as specified
     * in Table 1;<br/>
     * 
     * <pre>
     * Class          Bit 8 Bit 7 
     * Universal        0     0 
     * Application      0     1 
     * Context-specific 1     0 
     * Private          1     1
     * </pre>
     */
    @Order(0)
    @Bit(2)
    private TagClass tagClass;

    /**
     * Bit 6 shall be set to zero if the encoding is primitive, and shall be set to
     * one if the encoding is constructed.
     */
    @Order(1)
    @Bit
    private boolean primitive;

    /**
     * bits 5 to 1 shall encode the number of the tag as a binary integer with bit 5
     * as the most significant bit.
     */
    @Order(3)
    @Bit(5)
    private byte tagNumber;
    
    /**
     * For tags with a number greater than or equal to 31, the identifier shall
     * comprise a leading octet followed by one or more subsequent octets.
     */
    @Order(4)
    @Varint
    @Conditional(TagNumberCondition.class)
    private BigInteger tagNumber2;
    
    /*
     * 1. the definite form
     * 1.1 short form
     *  consist of a single octet in which bit 8 is zero and
     *  bits 7 to 1 encode the number of octets
     * 1.2 long form
     *  The initial octet shall be encoded as:
     *  A. bit 8 shall be one
     *  B. bits 7 to 1 shall encode the number of subsequent octets in the length octets, as an unsigned binary integer 
     *  with bit 7 as the most significant bit 
     *  C. the value 111111112 shall not be used. 
     *  D. Bits 8 to 1 of the following subsequent octets 
     *  shall be the encoding of an unsigned binary integer equal to the number of octets in the contents octets
     * 2. the indefinite form
     *  The single octet shall have bit 8 set to one, and bits 7 to 1 set to zero. 
     */
    
    @Order(5)
    @BYTE
    @Signed
    private byte length1;
    
    public static final class TagNumberCondition extends ModifierHandler<Boolean>{

        @Override
        public Boolean handleDeserialize0(String fieldName, Object entity, InputStream in) throws IOException {
            return ((BERData)entity).tagNumber == 0b11111;
        }

        @Override
        public Boolean handleSerialize0(String fieldName, Object entity) {
            return ((BERData)entity).tagNumber == 0b11111;
        }
        
    }
}
