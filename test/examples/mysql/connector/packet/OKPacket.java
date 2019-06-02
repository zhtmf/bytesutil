package examples.mysql.connector.packet;

import examples.mysql.connector.datatypes.le.LEIntHandler;
import examples.mysql.connector.datatypes.le.LEInteger;
import io.github.zhtmf.annotations.modifiers.LittleEndian;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.modifiers.Variant;
import io.github.zhtmf.annotations.types.BYTE;

/**
 * An OK packet is sent from the server to the client to signal successful
 * completion of a command.
 * 
 * As of MySQL 5.7.5, OK packes are also used to indicate EOF, and EOF packets
 * are deprecated.
 * 
 * @author dzh
 */
@LittleEndian
@Unsigned
public class OKPacket extends BasePacket{
    
    public long capabilities;

    /**
     * 0x00 or 0xFE the OK packet header
     */
    @Order(0)
    @BYTE
    public int header = 0x00;
    
    @Order(1)
    @Variant(LEIntHandler.class)
    public LEInteger affectedRows;
    
    @Order(2)
    @Variant(LEIntHandler.class)
    public LEInteger lastInsertId;
}
