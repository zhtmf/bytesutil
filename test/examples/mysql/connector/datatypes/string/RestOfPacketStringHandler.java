package examples.mysql.connector.datatypes.string;

import java.io.IOException;
import java.io.InputStream;

import examples.mysql.connector.packet.OKPacket;
import io.github.zhtmf.converters.auxiliary.ModifierHandler;

public class RestOfPacketStringHandler extends ModifierHandler<Integer>{

    @Override
    public Integer handleDeserialize0(String fieldName, Object entity, InputStream is) throws IOException {
        /*
         * If a string is the last component of a packet, its length can be calculated
         * from the overall packet length minus the current position.
         */
        int length;
        if(entity instanceof OKPacket) {
            length = ((OKPacket)entity).payloadLength;
        }else {
            throw new IllegalArgumentException(entity.getClass()+"");
        }
        //4 for fields in outer MySQLPacket
        return length - currentPosition() + 4;
    }

    @Override
    public Integer handleSerialize0(String fieldName, Object entity) {
        return ((String)entity).length();
    }
}
