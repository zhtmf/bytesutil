package examples.mysql.connector.packet.command;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import examples.mysql.connector.datatypes.string.LengthEncodedString;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Script;
import io.github.zhtmf.annotations.modifiers.Variant;
import io.github.zhtmf.converters.auxiliary.EntityHandler;

public class TextResultsetRow extends DataPacket {
    
    public int columnCount;
    
    /**
     * <p>
     * NULL is sent as 0xfb
     * <p>
     * everything else is converted into a string and is sent
     * as Protocol::LengthEncodedString.
     */
    @Order(0)
    @Variant(Value.class)
    @Length(scripts = @Script("entity.columnCount"))
    public List<DataPacket> value;
    
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
