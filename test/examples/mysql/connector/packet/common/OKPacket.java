package examples.mysql.connector.packet.common;

import java.io.IOException;
import java.io.InputStream;

import examples.mysql.connector.datatypes.le.LEIntHandler;
import examples.mysql.connector.datatypes.le.LEInteger;
import examples.mysql.connector.datatypes.string.LengthEncodedString;
import examples.mysql.connector.datatypes.string.RestOfPacketStringHandler;
import examples.mysql.connector.packet.ClientCapabilityAware;
import examples.mysql.connector.packet.PayLoadLengthAware;
import examples.mysql.connector.packet.connection.ClientCapabilities;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Conditional;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.LittleEndian;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.modifiers.Variant;
import io.github.zhtmf.annotations.types.BYTE;
import io.github.zhtmf.annotations.types.CHAR;
import io.github.zhtmf.annotations.types.SHORT;
import io.github.zhtmf.converters.auxiliary.ModifierHandler;

/**
 * An OK packet is sent from the server to the client to signal successful
 * completion of a command.
 * 
 * As of MySQL 5.7.5, OK packes are also used to indicate EOF, and EOF packets
 * are deprecated.
 * 
 * @author dzh
 */
@LittleEndian
@Unsigned
public class OKPacket extends DataPacket implements ClientCapabilityAware, PayLoadLengthAware{
    
    private long clientCapabilities;
    private int payLoadLength;
    /**
     * 0x00 or 0xFE the OK packet header
     */
    @Order(0)
    @BYTE
    public int header = 0xFE;
    
    @Order(1)
    @Variant(LEIntHandler.class)
    public LEInteger affectedRows;
    
    @Order(2)
    @Variant(LEIntHandler.class)
    public LEInteger lastInsertId;
    
    @Order(3)
    @SHORT
    //SERVER_STATUS_flags_enum
    @Conditional(CapabilitiesCondition.class)
    public int statusFlags;
    
    @Order(4)
    @SHORT
    @Conditional(CapabilitiesCondition.class)
    //number of warnings
    //if clientCapabilities & CLIENT_PROTOCOL_41 {
    public int warnings;
    
    @Order(5)
    @Conditional(CapabilitiesCondition.class)
    //if clientCapabilities & CLIENT_SESSION_TRACK
    public LengthEncodedString info;
    
    @Order(6)
    @Conditional(CapabilitiesCondition.class)
    //human readable status information
    //if clientCapabilities & CLIENT_SESSION_TRACK
    //if status_flags & SERVER_SESSION_STATE_CHANGED {
    public LengthEncodedString sessionStatusInfo;
    
    @Order(7)
    @CHAR
    @Conditional(CapabilitiesCondition.class)
    //if ! clientCapabilities & CLIENT_SESSION_TRACK
    @Length(handler=RestOfPacketStringHandler.class)
    public String info2;
    
    @Override
    public String toString() {
        return "OKPacket [header=" + header + ", affectedRows=" + affectedRows + ", lastInsertId=" + lastInsertId
                + ", statusFlags=" + statusFlags + ", warnings=" + warnings + ", info=" + info + ", sessionStatusInfo="
                + sessionStatusInfo + ", info2=" + info2 + "]";
    }

    public static class CapabilitiesCondition extends ModifierHandler<Boolean>{

        @Override
        public Boolean handleDeserialize0(String fieldName, Object entity, InputStream is) throws IOException {
            OKPacket packet = (OKPacket)entity;
            if("warnings".equals(fieldName)) {
                return (packet.clientCapabilities & ClientCapabilities.CLIENT_PROTOCOL_41)!=0
                    && (packet.clientCapabilities & ClientCapabilities.CLIENT_TRANSACTIONS)==0;
            }else if("info".equals(fieldName)) {
                return (packet.clientCapabilities & ClientCapabilities.CLIENT_SESSION_TRACK)!=0;
            }else if("sessionStatusInfo".equals(fieldName)) {
                return (packet.clientCapabilities & ClientCapabilities.CLIENT_SESSION_TRACK)!=0
                    && (packet.statusFlags & ClientCapabilities.SERVER_SESSION_STATE_CHANGED)!=0;
            }else if("info2".equals(fieldName)) {
                return (packet.clientCapabilities & ClientCapabilities.CLIENT_SESSION_TRACK)==0;
            }else if("statusFlags".equals(fieldName)) {
                return (packet.clientCapabilities & ClientCapabilities.CLIENT_PROTOCOL_41)!=0
                    || (packet.clientCapabilities & ClientCapabilities.CLIENT_TRANSACTIONS)!=0;
            }else {
                throw new IllegalStateException();
            }
        }

        @Override
        public Boolean handleSerialize0(String fieldName, Object entity) {
            OKPacket packet = (OKPacket)entity;
            if("warnings".equals(fieldName)) {
                return (packet.clientCapabilities & ClientCapabilities.CLIENT_PROTOCOL_41)!=0
                    && (packet.clientCapabilities & ClientCapabilities.CLIENT_TRANSACTIONS)==0;
            }else if("info".equals(fieldName)) {
                return (packet.clientCapabilities & ClientCapabilities.CLIENT_SESSION_TRACK)!=0;
            }else if("sessionStatusInfo".equals(fieldName)) {
                return (packet.clientCapabilities & ClientCapabilities.CLIENT_SESSION_TRACK)!=0
                    && (packet.statusFlags & ClientCapabilities.SERVER_SESSION_STATE_CHANGED)!=0;
            }else if("info2".equals(fieldName)) {
                return (packet.clientCapabilities & ClientCapabilities.CLIENT_SESSION_TRACK)==0;
            }else if("statusFlags".equals(fieldName)) {
                return (packet.clientCapabilities & ClientCapabilities.CLIENT_PROTOCOL_41)!=0
                    || (packet.clientCapabilities & ClientCapabilities.CLIENT_TRANSACTIONS)!=0;
            }else {
                throw new IllegalStateException();
            }
        }
        
    }

    @Override
    public void setClientCapability(int clientCapabilities) {
        this.clientCapabilities = clientCapabilities;
    }

    @Override
    public void setPayLoadLength(int payLoadLength) {
        this.payLoadLength = payLoadLength;
    }
    
    public int getPayLoadLength() {
        return this.payLoadLength;
    }
}