package examples.ber;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.BigEndian;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.types.Bit;

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
}
