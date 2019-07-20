package examples.mysql.connector.packet;

import java.io.IOException;
import java.io.InputStream;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.types.BYTE;
import io.github.zhtmf.annotations.types.CHAR;
import io.github.zhtmf.converters.auxiliary.ModifierHandler;

public class COMQuery extends DataPacket{
    @Order(0)
    @BYTE
    public int command = 0x03;
    @Order(1)
    @CHAR
    @Length(handler=Query.class)
    public String query;
    
    public static class Query extends ModifierHandler<Integer>{
        @Override
        public Integer handleDeserialize0(String fieldName, Object entity, InputStream is) throws IOException {
            throw new UnsupportedOperationException();//TODO: more elegant way of doing this
        }

        @Override
        public Integer handleSerialize0(String fieldName, Object entity) {
            return ((COMQuery)entity).query.length();
        }
    }
}
