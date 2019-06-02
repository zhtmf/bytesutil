package examples.mysql.connector.datatypes.le;

import java.math.BigInteger;

import io.github.zhtmf.annotations.modifiers.LittleEndian;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.BYTE;

@LittleEndian
@Unsigned
public class LEInt1 extends LEInteger{
    
    /**
     * >=0 <251 1-byte integer
     */
    @Order(0)
    @BYTE
    public int header;

    @Override
    public BigInteger getNumericValue() {
        return new BigInteger(header+"");
    }

}
