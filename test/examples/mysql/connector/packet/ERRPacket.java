package examples.mysql.connector.packet;

import java.io.IOException;
import java.io.InputStream;

import examples.mysql.connector.auxiliary.CapabilityFlags;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Conditional;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.LittleEndian;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.BYTE;
import io.github.zhtmf.annotations.types.CHAR;
import io.github.zhtmf.annotations.types.SHORT;
import io.github.zhtmf.converters.auxiliary.ModifierHandler;

@LittleEndian
@Unsigned
public class ERRPacket extends DataPacket{
    
    public int clientCapabilities;
    
    //injected by outer MySQLPacket object
    public int selfLength;

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
    @Conditional(SqlState.class)
    public char sqlStateMarker;
    
    @Order(3)
    @CHAR(5)
    @Conditional(SqlState.class)
    public String sqlSate;
    
    @Order(4)
    @CHAR
    @Length(handler=ERMSGLength.class)
    public String errorMessage;
    
    public static class ERMSGLength extends ModifierHandler<Integer>{
        @Override
        public Integer handleDeserialize0(String fieldName, Object entity, InputStream is) throws IOException {
            ERRPacket ret = (ERRPacket)entity;
            return ret.selfLength - currentPosition();
        }

        @Override
        public Integer handleSerialize0(String fieldName, Object entity) {
            return ((String)entity).length();
        }
    }
    
    public static class SqlState extends ModifierHandler<Boolean>{
        @Override
        public Boolean handleDeserialize0(String fieldName, Object entity, InputStream is) throws IOException {
            ERRPacket pac = (ERRPacket)entity;
            return (pac.clientCapabilities & (CapabilityFlags.CLIENT_PROTOCOL_41) )!=0;
        }

        @Override
        public Boolean handleSerialize0(String fieldName, Object entity) {
            ERRPacket pac = (ERRPacket)entity;
            return (pac.clientCapabilities & (CapabilityFlags.CLIENT_PROTOCOL_41) )!=0;
        }
    }

    @Override
    public String toString() {
        return "ERRPacket [clientCapabilities=" + clientCapabilities + ", selfLength=" + selfLength + ", header="
                + header + ", errorCode=" + errorCode + ", sqlStateMarker=" + sqlStateMarker + ", sqlSate=" + sqlSate
                + ", errorMessage=" + errorMessage + "]";
    }
}
