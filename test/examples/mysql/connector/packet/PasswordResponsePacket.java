package examples.mysql.connector.packet;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.types.RAW;

public class PasswordResponsePacket extends DataPacket{
    @Order(0)
    @RAW(20)
    public byte[] pwd;
}
