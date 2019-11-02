package examples.mysql.connector.packet.command;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.types.BYTE;

public class NULLValue extends DataPacket {
    @Order(0)
    @BYTE
    public int NULL = 0xFB;

    @Override
    public String toString() {
        return "NULLValue";
    }
}
