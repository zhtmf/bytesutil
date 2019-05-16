package examples.classparser.entities.attributeinfo.info.stackmapframe;

import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Unsigned;
import org.dzh.bytesutil.annotations.types.SHORT;

@Unsigned
public class ChopFrame extends StackMapFrame{
    @Order(0)
    @SHORT
    public int offsetDelta;
}
