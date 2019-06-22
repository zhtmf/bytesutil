package examples.mysql.connector.packet;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import examples.mysql.connector.auxiliary.CapabilityFlags;
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
import io.github.zhtmf.annotations.types.SHORT;
import io.github.zhtmf.converters.auxiliary.ModifierHandler;

@Unsigned
@LittleEndian
public class HandshakeV10 extends BasePacket{

    @Order(0)
    @BYTE
    //Always 10
    public int version;
    
    @Order(1)
    @CHAR
    @EndsWith({'\0'})
    //human readable status information
    public String serverVersion;
    
    @Order(2)
    @INT
    public long threadId;
    
    @Order(3)
    @RAW(8)
    //first 8 bytes of the plugin provided data (scramble)
    public byte[] pluginDataPart1;
    
    @Order(4)
    @BYTE
    //0x00 byte, terminating the first part of a scramble
    public byte filler = 0;
    
    @Order(5)
    @SHORT
    //The lower 2 bytes of the Capabilities Flags
    public int capFlags1;
    
    @Order(6)
    @BYTE
    //default server a_protocol_character_set, only the lower 8-bits
    public int charSet;
    
    @Order(7)
    @SHORT
    //SERVER_STATUS_flags_enum
    public int statusFlags;
    
    @Order(8)
    @SHORT
    //The upper 2 bytes of the Capabilities Flags
    public int capFlags2;
    
    @Order(9)
    @BYTE
    //if capabilities & CLIENT_PLUGIN_AUTH { 
    //length of the combined auth_plugin_data (scramble), if auth_plugin_data_len is > 0
    //or constant 0x00
    public int authPluginDataLen;

    @Order(10)
    @RAW(10)
    public byte[] reserved;
    
    @Order(11)
    @RAW
    @Length(handler=RestPluginLength.class)
    public byte[] restPluginProvidedData;
    
    @Order(12)
    @CHAR
    @EndsWith({'\0'})
    @Conditional(PluginName.class)
    public String authPluginName;
    
    public static class RestPluginLength extends ModifierHandler<Integer>{
        @Override
        public Integer handleDeserialize0(String fieldName, Object entity, InputStream is) throws IOException {
            HandshakeV10 v10 = (HandshakeV10)entity;
            return Math.max(13, v10.authPluginDataLen-8);
        }

        @Override
        public Integer handleSerialize0(String fieldName, Object entity) {
            HandshakeV10 v10 = (HandshakeV10)entity;
            return Math.max(13, v10.authPluginDataLen-8);
        }
    }
    
    public static class PluginName extends ModifierHandler<Boolean>{
        @Override
        public Boolean handleDeserialize0(String fieldName, Object entity, InputStream is) throws IOException {
            HandshakeV10 v10 = (HandshakeV10)entity;
            return ((v10.capFlags2 << 16 | v10.capFlags1) & (CapabilityFlags.CLIENT_PLUGIN_AUTH) )!=0;
        }

        @Override
        public Boolean handleSerialize0(String fieldName, Object entity) {
            HandshakeV10 v10 = (HandshakeV10)entity;
            return ((v10.capFlags2 << 16 | v10.capFlags1) & (CapabilityFlags.CLIENT_PLUGIN_AUTH) )!=0;
        }
    }

    @Override
    public String toString() {
        return super.toString()+"HandshakeV10 [version=" + version + ", serverVersion=" + serverVersion + ", threadId=" + threadId
                + ", pluginDataPart1=" + Arrays.toString(pluginDataPart1) + ", filler=" + filler + ", capFlags1="
                + capFlags1 + ", charSet=" + charSet + ", statusFlags=" + statusFlags + ", capFlags2=" + capFlags2
                + ", authPluginDataLen=" + authPluginDataLen + ", reserved=" + Arrays.toString(reserved)
                + ", restPluginProvidedData=" + Arrays.toString(restPluginProvidedData) + ", authPluginName="
                + authPluginName + "]";
    }
}
