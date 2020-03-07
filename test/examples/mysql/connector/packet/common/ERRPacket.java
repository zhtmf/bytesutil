package examples.mysql.connector.packet.common;

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
import io.github.zhtmf.annotations.types.BYTE;
import io.github.zhtmf.annotations.types.CHAR;
import io.github.zhtmf.annotations.types.SHORT;

@LittleEndian
@Unsigned
public class ERRPacket extends DataPacket implements ClientCapabilityAware, PayLoadLengthAware{
    
    @SuppressWarnings("unused")
    private int clientCapabilities;
    private int payLoadLength;

    /**
     * 0x00 or 0xFE the OK packet header
     */
    @Order(0)
    @BYTE
    public int header = 0xFF;
    
    @Order(1)
    @SHORT
    public int errorCode;
    
    @Order(2)
    @CHAR(1)
    @Conditional(scripts = @Script("(entity.clientCapabilities & "+ClientCapabilities.CLIENT_PROTOCOL_41+") != 0"))
    public char sqlStateMarker;
    
    @Order(3)
    @CHAR(5)
    @Conditional(scripts = @Script("(entity.clientCapabilities & "+ClientCapabilities.CLIENT_PROTOCOL_41+") != 0"))
    public String sqlSate;
    
    @Order(4)
    @CHAR
    @Length(scripts = @Script(value = "entity.errorMessage.length", deserialize = "entity.payLoadLength - handler.offset()"))
    public String errorMessage;
    
    @Override
    public String toString() {
        return "ERRPacket [header=" + header + ", errorCode=" + errorCode + ", sqlStateMarker=" + sqlStateMarker
                + ", sqlSate=" + sqlSate + ", errorMessage=" + errorMessage + "]";
    }

    @Override
    public void setPayLoadLength(int payLoadLength) {
        this.payLoadLength = payLoadLength;
    }

    @Override
    public void setClientCapability(int clientCapabilities) {
        this.clientCapabilities = clientCapabilities;
    }
    
    public int getPayLoadLength() {
        return this.payLoadLength;
    }
}
