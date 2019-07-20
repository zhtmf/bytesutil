package examples.mysql.connector.packet;

import java.io.IOException;
import java.io.InputStream;

import examples.mysql.connector.packet.common.PayLoadLengthAware;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.EndsWith;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.LittleEndian;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.BYTE;
import io.github.zhtmf.annotations.types.CHAR;
import io.github.zhtmf.converters.auxiliary.ModifierHandler;

@LittleEndian
@Unsigned
public class AuthSwitchRequest extends DataPacket implements PayLoadLengthAware{
    
    private int payLoadLength;

    @Order(0)
    @BYTE
    public int header = 0xFE;
    
    @Order(1)
    @CHAR
    @EndsWith({'\0'})
    public String pluginName;
    
    @Order(2)
    @CHAR
    @Length(handler=PluginProvidedData.class)
    public String pluginProvidedData;
    
    public static class PluginProvidedData extends ModifierHandler<Integer>{
        @Override
        public Integer handleDeserialize0(String fieldName, Object entity, InputStream is) throws IOException {
            AuthSwitchRequest ret = (AuthSwitchRequest)entity;
            return ret.payLoadLength - currentPosition();
        }

        @Override
        public Integer handleSerialize0(String fieldName, Object entity) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public String toString() {
        return "AuthSwitchRequest [header=" + header + ", pluginName=" + pluginName
                + ", pluginProvidedData=" + pluginProvidedData + "]";
    }

    @Override
    public void setPayLoadLength(int payLoadLength) {
       this.payLoadLength = payLoadLength;
    }

    @Override
    public int getPayLoadLength() {
        return this.payLoadLength;
    }
}
