package examples.mysql.connector.datatypes.le;

import java.math.BigInteger;

import io.github.zhtmf.annotations.modifiers.LittleEndian;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.BYTE;
import io.github.zhtmf.annotations.types.SHORT;

@LittleEndian
@Unsigned
public class LEInt2 extends LEInteger{
    
    /**
     * >=251 <=2^16 2-byte integer
     * 0xFC
     */
    @Order(0)
    @BYTE
    public int header = 0xFC;
    
    @Order(1)
    @SHORT
    public int value;

    @Override
    public BigInteger getNumericValue() {
        return new BigInteger(value+"");
    }

}
