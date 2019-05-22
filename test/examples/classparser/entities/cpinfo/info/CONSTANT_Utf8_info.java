package examples.classparser.entities.cpinfo.info;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.CHARSET;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.CHAR;
import io.github.zhtmf.converters.auxiliary.DataType;

@Unsigned
@CHARSET("UTF-8")
public class CONSTANT_Utf8_info extends DataPacket {
    @Order(0)
    @CHAR
    @Length(type=DataType.SHORT)
    public String bytes;
    @Override
    public String toString() {
        return "CONSTANT_Utf8_info [bytes=" + bytes + "]";
    }
}
