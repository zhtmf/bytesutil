package examples.mysql.connector.packet.command;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Script;
import io.github.zhtmf.annotations.types.BYTE;
import io.github.zhtmf.annotations.types.CHAR;

public class COMInitDB extends DataPacket{
    @Order(0)
    @BYTE
    public int command = 0x02;
    //RestOfPacketStringHandler
    @Order(1)
    @CHAR
    @Length(scripts = @Script(
            value = "(entity[fieldName] + '').length"
    ,deserialize = "entity.payLoadLength - handler.offset"))
    public String schemaName;
}
