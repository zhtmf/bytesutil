package examples.mysql.connector.packet;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.LittleEndian;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.BYTE;
import io.github.zhtmf.annotations.types.INT3;

@Unsigned
@LittleEndian
public abstract class BasePacket extends DataPacket{

    /**
     * Length of the payload. The number of bytes in the packet beyond the initial 4
     * bytes that make up the packet header.
     */
    @Order(0)
    @INT3
    public int payloadLength;
    
    /**
     * The sequence-id is incremented with each packet and may wrap around. It
     * starts at 0 and is reset to 0 when a new command begins in the Command Phase.
     */
    @Order(1)
    @BYTE
    public byte sequenceId;

    @Override
    public String toString() {
        return "BasePacket [payloadLength=" + payloadLength + ", sequenceId=" + sequenceId + "]";
    }
}
