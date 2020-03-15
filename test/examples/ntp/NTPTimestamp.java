package examples.ntp;

import java.util.Date;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.INT;

public class NTPTimestamp extends DataPacket{
    
    public NTPTimestamp() {
        this(System.currentTimeMillis());
    }
    
    public NTPTimestamp(long timestamp) {
        this.seconds = timestamp/1000;
        this.fraction = timestamp%1000;
    }
    
    @Order(0)
    @INT
    @Unsigned
    public long seconds;
    
    @Order(1)
    @INT
    @Unsigned
    public long fraction;
    
    @Override
    public String toString() {
        return new Date(seconds * 1000 + fraction).toString();
    }
}
