package examples.mysql.connector.packet;

import java.io.IOException;
import java.io.InputStream;

import examples.mysql.connector.auxiliary.CapabilityFlags;
import examples.mysql.connector.datatypes.string.LengthEncodedString;
import io.github.zhtmf.annotations.modifiers.Conditional;
import io.github.zhtmf.annotations.modifiers.EndsWith;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.LittleEndian;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.BYTE;
import io.github.zhtmf.annotations.types.CHAR;
import io.github.zhtmf.annotations.types.INT;
import io.github.zhtmf.annotations.types.RAW;
import io.github.zhtmf.converters.auxiliary.DataType;
import io.github.zhtmf.converters.auxiliary.ModifierHandler;

@Unsigned
@LittleEndian
public class HandshakeResponse41 extends BasePacket{

    @Order(0)
    @INT
    //Capabilities Flags, CLIENT_PROTOCOL_41 always set.
    public long clientFlag;
    
    @Order(1)
    @INT
    public long maxPacketSize;
    
    @Order(2)
    @BYTE
    //  client charset a_protocol_character_set, only the lower 8-bits
    public int charSet;
    
    @Order(3)
    @RAW(23)
    //filler to the size of the handhshake response packet. All 0s.
    public byte[] filler = new byte[23];
    
    @Order(4)
    @CHAR
    @EndsWith({'\0'})
    public String username;
    
    @Order(5)
    @Conditional(AuthResponse.class)
    public LengthEncodedString authResponse;
    
    @Order(6)
    @CHAR
    @Length(type=DataType.BYTE)
    public String authResponseWithLength;
    
    public static class AuthResponse extends ModifierHandler<Boolean>{
        @Override
        public Boolean handleDeserialize0(String fieldName, Object entity, InputStream is) throws IOException {
            HandshakeResponse41 resp = (HandshakeResponse41)entity;
            return (resp.clientFlag & CapabilityFlags.CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA) != 0;
        }
        @Override
        public Boolean handleSerialize0(String fieldName, Object entity) {
            HandshakeResponse41 resp = (HandshakeResponse41)entity;
            return (resp.clientFlag & CapabilityFlags.CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA) != 0;
        }
    }
}
