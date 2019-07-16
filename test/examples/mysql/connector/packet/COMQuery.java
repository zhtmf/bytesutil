package examples.mysql.connector.packet;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.EndsWith;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.types.BYTE;
import io.github.zhtmf.annotations.types.CHAR;

public class COMQuery extends DataPacket{
    @Order(0)
    @BYTE
    public int command = 0x03;
    @Order(1)
    @CHAR
    @EndsWith({'\0'})
    public String query;
}
