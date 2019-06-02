package examples.mysql.connector.datatypes.string;

import java.io.IOException;
import java.io.InputStream;

import examples.mysql.connector.packet.BasePacket;
import io.github.zhtmf.converters.auxiliary.ModifierHandler;

public abstract class RestOfPacketStringHandler extends ModifierHandler<Integer>{

    @Override
    public Integer handleDeserialize0(String fieldName, Object entity, InputStream is) throws IOException {
        /*
         * If a string is the last component of a packet, its length can be calculated
         * from the overall packet length minus the current position.
         */
        int length = ((BasePacket)entity).payloadLength;
        return length - currentPosition() - 4;
    }
}
