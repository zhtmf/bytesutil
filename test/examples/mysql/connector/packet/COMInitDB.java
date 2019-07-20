package examples.mysql.connector.packet;

import examples.mysql.connector.datatypes.string.RestOfPacketStringHandler;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.types.BYTE;
import io.github.zhtmf.annotations.types.CHAR;

public class COMInitDB extends DataPacket{
    @Order(0)
    @BYTE
    public int command = 0x02;
    @Order(1)
    @CHAR
    @Length(handler=RestOfPacketStringHandler.class)
    public String schemaName;
}
