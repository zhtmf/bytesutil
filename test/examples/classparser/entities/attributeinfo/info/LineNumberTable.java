package examples.classparser.entities.attributeinfo.info;

import java.util.List;

import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.Length;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Unsigned;
import org.dzh.bytesutil.converters.auxiliary.DataType;

@Unsigned
public class LineNumberTable extends DataPacket{
    @Order(0)
    @Length(type=DataType.SHORT)
    public List<LineNumber> lineNumberTable;
}
