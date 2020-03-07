package examples.mysql.connector.packet.connection;

import java.util.Arrays;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Conditional;
import io.github.zhtmf.annotations.modifiers.EndsWith;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.LittleEndian;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Script;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.BYTE;
import io.github.zhtmf.annotations.types.CHAR;
import io.github.zhtmf.annotations.types.INT;
import io.github.zhtmf.annotations.types.RAW;
import io.github.zhtmf.annotations.types.SHORT;

@Unsigned
@LittleEndian
public class HandshakeV10 extends DataPacket{

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
    //if clientCapabilities & CLIENT_PLUGIN_AUTH { 
    //length of the combined auth_plugin_data (scramble), if auth_plugin_data_len is > 0
    //or constant 0x00
    public int authPluginDataLen;

    @Order(10)
    @RAW(10) 
    public byte[] reserved;
    
    @Order(11)
    @RAW
    @Length(scripts = @Script("a=entity.authPluginDataLen-8;a<13 ? 13 : a;"))
    public byte[] restPluginProvidedData;
    
    @Order(12)
    @CHAR
    @EndsWith({'\0'})
    @Conditional(scripts = @Script("((entity.capFlags2 << 16 | entity.capFlags1) & "
            +ClientCapabilities.CLIENT_PLUGIN_AUTH+") != 0"))
    public String authPluginName;
    
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
