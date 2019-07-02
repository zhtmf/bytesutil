package examples.mysql.connector.datatypes.le;

import java.math.BigInteger;

import io.github.zhtmf.annotations.modifiers.LittleEndian;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.BYTE;
import io.github.zhtmf.annotations.types.LONG;

@LittleEndian
@Unsigned
public class LEInt8 extends LEInteger{
    
    /**
     * >=2^24 <2^64 8-byte integer
     * 0xFC
     */
    @Order(0)
    @BYTE
    public int header = 0xFE;
    
    @Order(1)
    @LONG
    public BigInteger value;

    @Override
    public BigInteger getNumericValue() {
        return value;
    }
    @Override
    public String toString() {
        return super.toString();
    }
}
