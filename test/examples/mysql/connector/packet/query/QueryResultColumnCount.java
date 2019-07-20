package examples.mysql.connector.packet.query;

import java.io.IOException;
import java.io.InputStream;

import examples.mysql.connector.auxiliary.CapabilityFlags;
import examples.mysql.connector.datatypes.le.LEIntHandler;
import examples.mysql.connector.datatypes.le.LEInteger;
import examples.mysql.connector.packet.common.ClientCapabilityAware;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Conditional;
import io.github.zhtmf.annotations.modifiers.LittleEndian;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.modifiers.Variant;
import io.github.zhtmf.annotations.types.BYTE;
import io.github.zhtmf.converters.auxiliary.ModifierHandler;

@LittleEndian
@Unsigned
public class QueryResultColumnCount extends DataPacket implements ClientCapabilityAware{
    
    private int clientCapabilities;
    
    @Order(0)
    @BYTE
    @Conditional(Conditionals.class)
    public byte metadataFollows;
    
    @Order(1)
    @Variant(LEIntHandler.class)
    public LEInteger columnCount;
    
    public static class Conditionals extends ModifierHandler<Boolean>{
        @Override
        public Boolean handleDeserialize0(String fieldName, Object entity, InputStream is) throws IOException {
            return handleSerialize0(fieldName, entity);
        }
        @Override
        public Boolean handleSerialize0(String fieldName, Object entity) {
            QueryResultColumnCount pac = (QueryResultColumnCount)entity;
            switch(fieldName) {
            case "metadataFollows":
                return (pac.clientCapabilities & CapabilityFlags.CLIENT_OPTIONAL_RESULTSET_METADATA) != 0;
            }
            throw new IllegalArgumentException(fieldName);
        }
    }

    @Override
    public void setClientCapability(int clientCapabilities) {
        this.clientCapabilities = clientCapabilities;
    }

    @Override
    public String toString() {
        return "QueryResultColumnCount [metadataFollows=" + metadataFollows + ", columnCount=" + columnCount + "]";
    }
}
