package examples.mysql.connector.packet;

import java.io.IOException;
import java.io.InputStream;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.LittleEndian;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.modifiers.Variant;
import io.github.zhtmf.annotations.types.BYTE;
import io.github.zhtmf.annotations.types.INT3;
import io.github.zhtmf.converters.auxiliary.EntityHandler;

@Unsigned
@LittleEndian
public class MySQLPacket extends DataPacket{
    
    /**
     * mysql packets in initial handshake phase does not obey 
     * the "first byte determines payload type" pattern, the packet send by the server 
     * is always a HandshakeV10 or HandshakeV9
     * after that the first byte should be read to determine the actual payload type
     */
    public int phase;
    
    /**
     * client flag, passed down to OKPacket or other packets 
     * for conditional branches
     */
    public int capabilitiesFlag;

    /**
     * Length of the payload. The number of bytes in the packet beyond the initial 4
     * bytes that make up the packet header.
     */
    @Order(0)
    @INT3
    public int payloadLength;
    
    /**
     * The sequence-id is incremented with each packet and may wrap around. It
     * starts at 0 and is reset to 0 when a new command begins in the Command Phase.
     */
    @Order(1)
    @BYTE
    public byte sequenceId;
    
    @Order(2)
    @Variant(PayLoad.class)
    public DataPacket payload;
    
    public static class PayLoad extends EntityHandler{

        @Override
        public DataPacket handle0(String fieldName, Object entity, InputStream is) throws IOException {
            MySQLPacket pac = (MySQLPacket)entity;
            if(pac.phase!=0) {
                int b = is.read();
                if(b==0x00 || b==0xFE) {
                    OKPacket ret = new OKPacket();
                    ret.capabilities = pac.capabilitiesFlag;
                    ret.payloadLength = pac.payloadLength;
                    return ret;
                }else if(b==0xFF){
                    ERRPacket ret = new ERRPacket();
                    ret.selfLength = pac.payloadLength;
                    return ret;
                }else {
                    throw new IllegalArgumentException(b+"");
                }
            }else {
                return new HandshakeV10();
            }
        }
        
    }

    @Override
    public String toString() {
        return "MySQLPacket [payloadLength=" + payloadLength + ", sequenceId=" + sequenceId + ", payload=" + payload
                + "]";
    }
}
