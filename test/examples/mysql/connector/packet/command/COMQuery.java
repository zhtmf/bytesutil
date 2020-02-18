package examples.mysql.connector.packet.command;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Script;
import io.github.zhtmf.annotations.types.BYTE;
import io.github.zhtmf.annotations.types.CHAR;

public class COMQuery extends DataPacket{
    @Order(0)
    @BYTE
    public int command = 0x03;
    @Order(1)
    @CHAR
    @Length(scripts = @Script("entity.query.length"))
    //this should be a string<EOF>
    public String query;
}
