package examples.mysql.connector.packet.common;

import examples.mysql.connector.packet.ClientCapabilityAware;
import examples.mysql.connector.packet.connection.ClientCapabilities;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Conditional;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Script;
import io.github.zhtmf.annotations.types.BYTE;
import io.github.zhtmf.annotations.types.SHORT;

public class EOFPacket extends DataPacket implements ClientCapabilityAware{
    @SuppressWarnings("unused")
    private long clientCapabilities;
    @Order(0)
    @BYTE
    public int header = 0xFE;
    
    /**
     * number of warnings
     */
    @Order(1)
    @SHORT
    @Conditional(scripts = @Script("(entity.clientCapabilities & "+ClientCapabilities.CLIENT_PROTOCOL_41+") != 0"))
    public int warnings;
    @Order(2)
    @SHORT
    @Conditional(scripts = @Script("(entity.clientCapabilities & "+ClientCapabilities.CLIENT_PROTOCOL_41+") != 0"))
    public int statusFlags;

    @Override
    public void setClientCapability(int clientCapabilities) {
        this.clientCapabilities = clientCapabilities;
    }
    
    @Override
    public String toString() {
        return "EOFPacket [header=" + header + ", warnings=" + warnings + ", statusFlags=" + statusFlags + "]";
    }
}
