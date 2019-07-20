package examples.mysql.connector.datatypes.string;

import java.io.IOException;
import java.io.InputStream;

import examples.mysql.connector.packet.PayLoadLengthAware;
import io.github.zhtmf.converters.auxiliary.ModifierHandler;

public class RestOfPacketStringHandler extends ModifierHandler<Integer>{

    @Override
    public Integer handleDeserialize0(String fieldName, Object entity, InputStream is) throws IOException {
        /*
         * If a string is the last component of a packet, its length can be calculated
         * from the overall packet length minus the current position.
         */
        int length;
        if(entity instanceof PayLoadLengthAware) {
            length = ((PayLoadLengthAware)entity).getPayLoadLength();
        }else {
            throw new IllegalArgumentException(entity.getClass()+"");
        }
        return length - currentPosition();
    }

    @Override
    public Integer handleSerialize0(String fieldName, Object entity) {
        try {
            return entity.getClass().getField(fieldName).get(entity).toString().length();
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
