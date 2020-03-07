package examples.mysql.connector.packet.connection;

import examples.mysql.connector.packet.PayLoadLengthAware;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.EndsWith;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.LittleEndian;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Script;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.BYTE;
import io.github.zhtmf.annotations.types.CHAR;

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
    @Length(scripts = @Script(value = "", deserialize = "entity.payLoadLength - handler.offset()"))
    public String pluginProvidedData;
    
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
