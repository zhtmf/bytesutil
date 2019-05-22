package examples.classparser.entities.cpinfo.info;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.RAW;

@Unsigned
public class CONSTANT_Double_info extends DataPacket {
    @Order(0)
    @RAW(8)
    //combined high-bytes and low-bytes
    public byte[] bytes;
}
