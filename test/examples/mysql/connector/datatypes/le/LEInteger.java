package examples.mysql.connector.datatypes.le;

import java.math.BigInteger;

import io.github.zhtmf.DataPacket;

/**
 * An integer that consumes 1, 3, 4, or 9 bytes, depending on its numeric value
 * @author dzh
 */
public abstract class LEInteger extends DataPacket{
    public abstract BigInteger getNumericValue();
}
