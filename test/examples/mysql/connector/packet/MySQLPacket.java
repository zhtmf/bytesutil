package examples.mysql.connector.packet;

import java.io.IOException;
import java.io.InputStream;

import examples.mysql.connector.auxiliary.CapabilityFlags;
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
     * Used for sending response to server
     * @param payload
     * @param seq
     */
    public MySQLPacket(DataPacket payload,byte seq) {
        this.payload = payload;
        this.payloadLength = payload.length();
        this.sequenceId = seq;
    }
    /**
     * Used for receiving data from server
     * @param capabilitiesFlag
     */
    public MySQLPacket(int capabilitiesFlag) {
        this.capabilitiesFlag = capabilitiesFlag;
    }
    
    /**
     * mysql packets in initial handshake phase does not obey the "first byte
     * determines payload type" pattern, the packet send by the server is always a
     * HandshakeV10 or HandshakeV9
     * <p>
     * after that the first byte should be used to determine the actual payload type
     */
    public int phase = 1;
    public static final int INITIAL_HANDSHAKE = 0;
    
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
            if(pac.phase == INITIAL_HANDSHAKE) {
                return new HandshakeV10();
            }
            int b = is.read();
            if(b==0x00) {
                OKPacket ret = new OKPacket();
                ret.capabilities = pac.capabilitiesFlag;
                ret.payloadLength = pac.payloadLength;
                return ret;
            }else if(b==0xFF){
                ERRPacket ret = new ERRPacket();
                ret.selfLength = pac.payloadLength;
                return ret;
            }else if(b==0xFE) {
                AuthSwitchRequest ret = new AuthSwitchRequest();
                ret.selfLength = pac.payloadLength;
                return ret;
            }else {
                //may be a COM_QUERY response
                if((pac.capabilitiesFlag & CapabilityFlags.CLIENT_OPTIONAL_RESULTSET_METADATA)!=0) {
                    //b is metadata_follows
                    //read next byte
                    b = is.read();
                }
                //check if b is beginning of an int lenec
                //column count cannot be 0, so there is no ambiguity here
                if((b>0 && b<251) || (b == 0xFC || b == 0xFD || b == 0xFE)){
                    TextResultSet rs = new TextResultSet();
                    rs.clientCapabilities = pac.capabilitiesFlag;
                    rs.selfLength = pac.payloadLength;
                    return rs;
                }
                throw new IllegalArgumentException(b+"");
            }
        }
        
    }

    @Override
    public String toString() {
        return "MySQLPacket [payloadLength=" + payloadLength + ", sequenceId=" + sequenceId + ", payload=" + payload
                + "]";
    }
}
