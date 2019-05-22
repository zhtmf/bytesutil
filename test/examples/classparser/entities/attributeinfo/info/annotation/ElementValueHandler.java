package examples.classparser.entities.attributeinfo.info.annotation;

import java.io.IOException;
import java.io.InputStream;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.converters.auxiliary.EntityHandler;

public class ElementValueHandler extends EntityHandler{

    @Override
    public DataPacket handle0(String fieldName, Object entity, InputStream is) throws IOException {
        byte tag = (byte) is.read();
        switch(tag) {
        case 'B':
        case 'C':
        case 'D':
        case 'F':
        case 'I':
        case 'J':
        case 'S':
        case 'Z':
        case 's':
            return new ConstantElementValue();
        case 'c':
            return new ClassInfoElementValue();
        case 'e':
            return new EnumConstantElementValue();
        case '@':
            return new AnnotationElementValue();
        case '[':
            return new ArrayElementValue();
        }
        return null;
    }
    
}