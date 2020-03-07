package examples.mysql.connector.packet.common;

import examples.mysql.connector.datatypes.le.LEIntHandler;
import examples.mysql.connector.datatypes.le.LEInteger;
import examples.mysql.connector.datatypes.string.LengthEncodedString;
import examples.mysql.connector.packet.ClientCapabilityAware;
import examples.mysql.connector.packet.PayLoadLengthAware;
import examples.mysql.connector.packet.connection.ClientCapabilities;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Conditional;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.LittleEndian;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Script;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.modifiers.Variant;
import io.github.zhtmf.annotations.types.BYTE;
import io.github.zhtmf.annotations.types.CHAR;
import io.github.zhtmf.annotations.types.SHORT;

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
    
    @SuppressWarnings("unused")
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
    @Conditional(scripts = @Script(
            "c = entity.clientCapabilities;"
            + "(c & "+ClientCapabilities.CLIENT_PROTOCOL_41+") != 0 && (c &"+ClientCapabilities.CLIENT_TRANSACTIONS+") != 0")
    )
    public int statusFlags;
    
    @Order(4)
    @SHORT
    @Conditional(scripts = @Script(
            "c = entity.clientCapabilities;"
            + "(c & "+ClientCapabilities.CLIENT_PROTOCOL_41+") != 0 && (c &"+ClientCapabilities.CLIENT_TRANSACTIONS+") == 0")
    )
    //number of warnings
    //if clientCapabilities & CLIENT_PROTOCOL_41 {
    public int warnings;
    
    @Order(5)
    @Conditional(scripts = @Script(
            "c = entity.clientCapabilities;"
            + "(c & "+ClientCapabilities.CLIENT_SESSION_TRACK+") != 0")
    )
    //if clientCapabilities & CLIENT_SESSION_TRACK
    public LengthEncodedString info;
    
    @Order(6)
    @Conditional(scripts = @Script(
            "c = entity.clientCapabilities;"
            + "(c & "+ClientCapabilities.CLIENT_SESSION_TRACK+") != 0 && (c &"+ClientCapabilities.SERVER_SESSION_STATE_CHANGED+") != 0")
    )
    //human readable status information
    //if clientCapabilities & CLIENT_SESSION_TRACK
    //if status_flags & SERVER_SESSION_STATE_CHANGED {
    public LengthEncodedString sessionStatusInfo;
    
    @Order(7)
    @CHAR
    @Conditional(scripts = @Script(
            "c = entity.clientCapabilities;"
            + "(c & "+ClientCapabilities.CLIENT_SESSION_TRACK+") == 0")
    )
    //if ! clientCapabilities & CLIENT_SESSION_TRACK
    //RestOfPacketStringHandler
    @Length(scripts = @Script(
            value = "(entity[fieldName] + '').length"
    ,deserialize = "entity.payLoadLength - handler.offset()"))
    public String info2;
    
    @Override
    public String toString() {
        return "OKPacket [header=" + header + ", affectedRows=" + affectedRows + ", lastInsertId=" + lastInsertId
                + ", statusFlags=" + statusFlags + ", warnings=" + warnings + ", info=" + info + ", sessionStatusInfo="
                + sessionStatusInfo + ", info2=" + info2 + "]";
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