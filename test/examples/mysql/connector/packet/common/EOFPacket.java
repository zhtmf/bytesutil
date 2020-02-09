package examples.mysql.connector.packet.common;

import java.io.IOException;
import java.io.InputStream;

import examples.mysql.connector.packet.ClientCapabilityAware;
import examples.mysql.connector.packet.connection.ClientCapabilities;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Conditional;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.types.BYTE;
import io.github.zhtmf.annotations.types.SHORT;
import io.github.zhtmf.converters.auxiliary.ModifierHandler;

public class EOFPacket extends DataPacket implements ClientCapabilityAware{
    private long clientCapabilities;
    @Order(0)
    @BYTE
    public int header = 0xFE;
    
    /**
     * number of warnings
     */
    @Order(1)
    @SHORT
    @Conditional(CapabilitiesCondition.class)
    public int warnings;
    @Order(2)
    @SHORT
    @Conditional(CapabilitiesCondition.class)
    public int statusFlags;

    @Override
    public void setClientCapability(int clientCapabilities) {
        this.clientCapabilities = clientCapabilities;
    }
    
    public static class CapabilitiesCondition extends ModifierHandler<Boolean>{
        @Override
        public Boolean handleDeserialize0(String fieldName, Object entity, InputStream is) throws IOException {
            return (((EOFPacket)entity).clientCapabilities & ClientCapabilities.CLIENT_PROTOCOL_41) !=0;
        }
        @Override
        public Boolean handleSerialize0(String fieldName, Object entity) {
            return (((EOFPacket)entity).clientCapabilities & ClientCapabilities.CLIENT_PROTOCOL_41) !=0;
        }
    }

    @Override
    public String toString() {
        return "EOFPacket [header=" + header + ", warnings=" + warnings + ", statusFlags=" + statusFlags + "]";
    }
}
