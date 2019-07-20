package examples.mysql.connector.packet.command;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import examples.mysql.connector.datatypes.string.LengthEncodedString;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Variant;
import io.github.zhtmf.converters.auxiliary.EntityHandler;
import io.github.zhtmf.converters.auxiliary.ModifierHandler;

public class TextResultsetRow extends DataPacket {
    
    public int columnCount;
    
    /**
     * NULL is sent as 0xfb everything else is converted into a string and is sent
     * as Protocol::LengthEncodedString.
     */
    @Order(0)
    @Variant(Value.class)
    @Length(handler=ColumnCount.class)
    public List<DataPacket> value;
    
    public static class ColumnCount extends ModifierHandler<Integer>{
        @Override
        public Integer handleDeserialize0(String fieldName, Object entity, InputStream is) throws IOException {
            TextResultsetRow ret = (TextResultsetRow)entity;
            return ret.columnCount;
        }

        @Override
        public Integer handleSerialize0(String fieldName, Object entity) {
            TextResultsetRow ret = (TextResultsetRow)entity;
            return ret.columnCount;
        }
    }
    
    public static final class Value extends EntityHandler{

        @Override
        public DataPacket handle0(String fieldName, Object entity, InputStream is) throws IOException {
            int b = is.read();
            if(b == 0xFB) {
                return new NULLValue();
            }else {
                return new LengthEncodedString();
            }
        }
    }

    @Override
    public String toString() {
        return "TextResultsetRow [value=" + value + "]";
    }
}
