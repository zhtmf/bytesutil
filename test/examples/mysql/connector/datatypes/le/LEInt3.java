package examples.mysql.connector.datatypes.le;

import java.math.BigInteger;

import io.github.zhtmf.annotations.modifiers.LittleEndian;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.BYTE;
import io.github.zhtmf.annotations.types.INT3;

@LittleEndian
@Unsigned
public class LEInt3 extends LEInteger{
    
    /**
     * >=2^16 <2^24 3-byte integer
     * 0xFC
     */
    @Order(0)
    @BYTE
    public int header = 0xFD;
    
    @Order(1)
    @INT3
    public int value;

    @Override
    public BigInteger getNumericValue() {
        return new BigInteger(value+"");
    }
    @Override
    public String toString() {
        return super.toString();
    }
}
