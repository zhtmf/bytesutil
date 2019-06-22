package examples.mysql.connector.datatypes.string;

import java.io.IOException;
import java.io.InputStream;

import examples.mysql.connector.packet.MySQLPacket;
import io.github.zhtmf.converters.auxiliary.ModifierHandler;

public class RestOfPacketStringHandler extends ModifierHandler<Integer>{

    @Override
    public Integer handleDeserialize0(String fieldName, Object entity, InputStream is) throws IOException {
        /*
         * If a string is the last component of a packet, its length can be calculated
         * from the overall packet length minus the current position.
         */
        int length = ((MySQLPacket)entity).payloadLength;
        return length - currentPosition() - 4;
    }

    @Override
    public Integer handleSerialize0(String fieldName, Object entity) {
        return ((String)entity).length();
    }
}
