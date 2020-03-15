package examples.ntp;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.SHORT;

/**
 * A 32-bit signed fixed-point number in units of seconds, with the
 * fraction point between bits 15 and 16.
 * 
 * @author dzh
 */
public class NTPShort extends DataPacket{

    @Order(0)
    @SHORT
    @Unsigned
    public int seconds;
    
    @Order(1)
    @SHORT
    @Unsigned
    public int fraction;
}
