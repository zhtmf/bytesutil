package examples.classparser.entities.attributeinfo.info.stackmapframe;

import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.SHORT;

@Unsigned
public class ChopFrame extends StackMapFrame{
    @Order(0)
    @SHORT
    public int offsetDelta;
}
