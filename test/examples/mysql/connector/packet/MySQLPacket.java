package examples.mysql.connector.packet;

import java.io.IOException;
import java.io.InputStream;

import examples.mysql.connector.packet.common.ClientCapabilityAware;
import examples.mysql.connector.packet.common.PayLoadLengthAware;
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
            int b = is.read();
            DataPacket ret;
            if(b==0x00) {
                ret = new OKPacket();
            }else if(b==0xFF){
                ret = new ERRPacket();
            }else if(b==0xFE) {
                //0xFE header can be both AuthSwitchRequest or EOFPacket,
                //they can only be distinguished by payload length
                if(pac.payloadLength==5) {
                    ret = new EOFPacket();
                }else {
                    ret = new AuthSwitchRequest();
                }
            }else {
                /*
                 * some mysql packets does not comply with the "first byte
                 * determines payload type" pattern, these packets are directly
                 * specified in other codes
                 */
                return pac.payload;
            }
            if(ret instanceof ClientCapabilityAware) {
                ((ClientCapabilityAware)ret).setClientCapability(pac.capabilitiesFlag);
            }
            if(ret instanceof PayLoadLengthAware) {
                ((PayLoadLengthAware)ret).setPayLoadLength(pac.payloadLength);
            }
            return ret;
        }
    }

    @Override
    public String toString() {
        return "MySQLPacket [payloadLength=" + payloadLength + ", sequenceId=" + sequenceId + ", payload=" + payload
                + "]";
    }
}
