package examples.classparser.entities.attributeinfo.info;

import java.util.List;

import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.Length;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.converters.auxiliary.DataType;

public class RuntimeVisibleParameterAnnotations extends DataPacket{
    @Order(0)
    @Length(type=DataType.BYTE)
    public List<AnnotationList> parameters;
}
