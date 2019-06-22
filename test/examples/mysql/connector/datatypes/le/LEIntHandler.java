package examples.mysql.connector.datatypes.le;

import java.io.IOException;
import java.io.InputStream;

import examples.mysql.connector.packet.MySQLPacket;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.converters.auxiliary.EntityHandler;

public class LEIntHandler extends EntityHandler{

    @Override
    public DataPacket handle0(String fieldName, Object entity, InputStream is) throws IOException {
        int first = is.read();
        if(first>=0 && first<251) {
            return new LEInt1();
        }
        switch(first) {
        case 0xFC:return new LEInt2();
        case 0xFD:return new LEInt3();
        case 0xFE:
            if (currentPosition() == 0) {
                /*
                 * If the first byte of a packet is a length-encoded integer and its byte value
                 * is 0xFE, you must check the length of the packet to verify that it has enough
                 * space for a 8-byte integer. If not, it may be an EOF_Packet instead.
                 */
                int len = ((MySQLPacket)entity).payloadLength;
                if(len<9) {
                    //TODO:
                    throw new UnsupportedOperationException();
                }else {
                    return new LEInt8();
                }
            }else {
                return new LEInt8();
            }
        default:
            throw new UnsupportedOperationException("unknown header:"+Integer.toHexString(first));
        }
    }

}
