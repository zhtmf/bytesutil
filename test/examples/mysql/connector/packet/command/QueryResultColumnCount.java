package examples.mysql.connector.packet.command;

import examples.mysql.connector.datatypes.le.LEIntHandler;
import examples.mysql.connector.datatypes.le.LEInteger;
import examples.mysql.connector.packet.ClientCapabilityAware;
import examples.mysql.connector.packet.connection.ClientCapabilities;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Conditional;
import io.github.zhtmf.annotations.modifiers.LittleEndian;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Script;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.modifiers.Variant;
import io.github.zhtmf.annotations.types.BYTE;

@LittleEndian
@Unsigned
public class QueryResultColumnCount extends DataPacket implements ClientCapabilityAware{
    
    @SuppressWarnings("unused")
    private int clientCapabilities;
    
    @Order(0)
    @BYTE
    @Conditional(scripts = @Script("(entity.clientCapabilities & "+ClientCapabilities.CLIENT_OPTIONAL_RESULTSET_METADATA+")!=0"))
    public byte metadataFollows;
    
    @Order(1)
    @Variant(LEIntHandler.class)
    public LEInteger columnCount;

    @Override
    public void setClientCapability(int clientCapabilities) {
        this.clientCapabilities = clientCapabilities;
    }

    @Override
    public String toString() {
        return "QueryResultColumnCount [metadataFollows=" + metadataFollows + ", columnCount=" + columnCount + "]";
    }
}
