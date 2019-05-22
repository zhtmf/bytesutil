package examples.classparser.entities.attributeinfo.info;

import java.util.List;

import examples.classparser.entities.attributeinfo.info.annotation.Annotation;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.converters.auxiliary.DataType;

/**
 * used by 
 * Deprecated
 * RuntimeVisibleAnnotations
 * RuntimeInvisibleAnnotations
 * RuntimeVisibleParameterAnnotations
 * RuntimeInvisibleParameterAnnotations
 */
@Unsigned
public class AnnotationList extends DataPacket{
    @Order(0)
    @Length(type=DataType.SHORT)
    public List<Annotation> annotations;
}
