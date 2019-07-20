package examples.mysql.connector.packet.common;

import java.io.IOException;
import java.io.InputStream;

import examples.mysql.connector.packet.CapabilityFlags;
import examples.mysql.connector.packet.ClientCapabilityAware;
import examples.mysql.connector.packet.PayLoadLengthAware;
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
public class ERRPacket extends DataPacket implements ClientCapabilityAware, PayLoadLengthAware{
    
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
            return ret.payLoadLength - currentPosition();
        }

        @Override
        public Integer handleSerialize0(String fieldName, Object entity) {
            return ((ERRPacket)entity).errorMessage.length();
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
