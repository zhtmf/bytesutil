package examples.mysql.connector.packet;

import java.io.IOException;
import java.io.InputStream;

import examples.mysql.connector.datatypes.le.LEIntHandler;
import examples.mysql.connector.datatypes.le.LEInteger;
import examples.mysql.connector.datatypes.string.LengthEncodedString;
import examples.mysql.connector.datatypes.string.RestOfPacketStringHandler;
import io.github.zhtmf.annotations.modifiers.Conditional;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.LittleEndian;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.modifiers.Variant;
import io.github.zhtmf.annotations.types.BYTE;
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
public class OKPacket extends BasePacket{
    
    public long capabilities;

    /**
     * 0x00 or 0xFE the OK packet header
     */
    @Order(0)
    @BYTE
    public int header = 0x00;
    
    @Order(1)
    @Variant(LEIntHandler.class)
    public LEInteger affectedRows;
    
    @Order(2)
    @Variant(LEIntHandler.class)
    public LEInteger lastInsertId;
    
    @Order(3)
    @SHORT
    //SERVER_STATUS_flags_enum
    public int statusFlags;
    
    @Order(4)
    @SHORT
    @Conditional(CapabilitiesCondition.class)
    //number of warnings
    //if capabilities & CLIENT_PROTOCOL_41 {
    public int warnings;
    
    @Order(5)
    @Conditional(CapabilitiesCondition2.class)
    //if capabilities & CLIENT_SESSION_TRACK
    public LengthEncodedString info;
    
    @Order(6)
    @Conditional(CapabilitiesCondition3.class)
    //human readable status information
    //if capabilities & CLIENT_SESSION_TRACK
    //if status_flags & SERVER_SESSION_STATE_CHANGED {
    public LengthEncodedString sessionStatusInfo;
    
    @Order(7)
    @Conditional(CapabilitiesCondition4.class)
    //if ! capabilities & CLIENT_SESSION_TRACK
    @Length(handler=RestOfPacketStringHandler.class)
    public String info2;
    
    public static class CapabilitiesCondition extends ModifierHandler<Boolean>{

        @Override
        public Boolean handleDeserialize0(String fieldName, Object entity, InputStream is) throws IOException {
            OKPacket packet = (OKPacket)entity;
            return (packet.capabilities & 512)!=0;
        }

        @Override
        public Boolean handleSerialize0(String fieldName, Object entity) {
            OKPacket packet = (OKPacket)entity;
            return (packet.capabilities & 512)!=0;
        }
        
    }
    
    public static class CapabilitiesCondition2 extends ModifierHandler<Boolean>{

        @Override
        public Boolean handleDeserialize0(String fieldName, Object entity, InputStream is) throws IOException {
            OKPacket packet = (OKPacket)entity;
            return (packet.capabilities & 1L<<23)!=0;
        }

        @Override
        public Boolean handleSerialize0(String fieldName, Object entity) {
            OKPacket packet = (OKPacket)entity;
            return (packet.capabilities & 1L<<23)!=0;
        }
        
    }
    
    public static class CapabilitiesCondition3 extends ModifierHandler<Boolean>{

        @Override
        public Boolean handleDeserialize0(String fieldName, Object entity, InputStream is) throws IOException {
            OKPacket packet = (OKPacket)entity;
            return (packet.capabilities & 1L<<23)!=0 && (packet.statusFlags & 1<<14)!=0;
        }

        @Override
        public Boolean handleSerialize0(String fieldName, Object entity) {
            OKPacket packet = (OKPacket)entity;
            return (packet.capabilities & 1L<<23)!=0 && (packet.statusFlags & 1<<14)!=0;
        }
        
    }
    
    public static class CapabilitiesCondition4 extends ModifierHandler<Boolean>{

        @Override
        public Boolean handleDeserialize0(String fieldName, Object entity, InputStream is) throws IOException {
            OKPacket packet = (OKPacket)entity;
            return (packet.capabilities & 1L<<23)==0;
        }

        @Override
        public Boolean handleSerialize0(String fieldName, Object entity) {
            OKPacket packet = (OKPacket)entity;
            return (packet.capabilities & 1L<<23)==0;
        }
    }
}