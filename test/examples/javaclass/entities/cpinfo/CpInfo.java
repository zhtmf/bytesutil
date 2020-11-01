package examples.javaclass.entities.cpinfo;

import java.io.IOException;
import java.io.InputStream;

import examples.javaclass.entities.cpinfo.info.CONSTANT_Class_info;
import examples.javaclass.entities.cpinfo.info.CONSTANT_Double_info;
import examples.javaclass.entities.cpinfo.info.CONSTANT_Fieldref_info;
import examples.javaclass.entities.cpinfo.info.CONSTANT_Float_info;
import examples.javaclass.entities.cpinfo.info.CONSTANT_Integer_info;
import examples.javaclass.entities.cpinfo.info.CONSTANT_InterfaceMethodref_info;
import examples.javaclass.entities.cpinfo.info.CONSTANT_InvokeDynamic_info;
import examples.javaclass.entities.cpinfo.info.CONSTANT_Long_info;
import examples.javaclass.entities.cpinfo.info.CONSTANT_MethodHandle_info;
import examples.javaclass.entities.cpinfo.info.CONSTANT_MethodType_info;
import examples.javaclass.entities.cpinfo.info.CONSTANT_Methodref_info;
import examples.javaclass.entities.cpinfo.info.CONSTANT_NameAndType_info;
import examples.javaclass.entities.cpinfo.info.CONSTANT_String_info;
import examples.javaclass.entities.cpinfo.info.CONSTANT_Utf8_info;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.CHARSET;
import io.github.zhtmf.annotations.modifiers.Conditional;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Script;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.modifiers.Variant;
import io.github.zhtmf.annotations.types.BYTE;
import io.github.zhtmf.converters.auxiliary.EntityHandler;

@Unsigned
@CHARSET("UTF-8")
public class CpInfo extends DataPacket{
    @Order(0)
    @BYTE
    @Conditional(scripts = @Script(value= "entity.tag.value != examples.javaclass.entities.cpinfo.CPInfoTag.PLACEHOLDER.value", deserialize = "true"))
    public CPInfoTag tag;
    @Order(1)
    @Variant(CpInfoHandler.class)
    @Conditional(scripts = @Script(value= "entity.tag.value != examples.javaclass.entities.cpinfo.CPInfoTag.PLACEHOLDER.value", deserialize = "true"))
    public DataPacket info;
    
    public static final class CpInfoHandler extends EntityHandler{

        @Override
        public DataPacket handle0(String fieldName, Object entity, InputStream is) throws IOException {
            CpInfo cpInfo = (CpInfo)entity;
            switch(cpInfo.tag) {
            case CONSTANT_Class:
                return new CONSTANT_Class_info();
            case CONSTANT_Fieldref:
                return new CONSTANT_Fieldref_info();
            case CONSTANT_Methodref:
                return new CONSTANT_Methodref_info();
            case CONSTANT_InterfaceMethodref:
                return new CONSTANT_InterfaceMethodref_info();
            case CONSTANT_String:
                return new CONSTANT_String_info();
            case CONSTANT_Integer:
                return new CONSTANT_Integer_info();
            case CONSTANT_Float:
                return new CONSTANT_Float_info();
            case CONSTANT_Long:
                return new CONSTANT_Long_info();
            case CONSTANT_Double:
                return new CONSTANT_Double_info();
            case CONSTANT_NameAndType:
                return new CONSTANT_NameAndType_info();
            case CONSTANT_Utf8:
                return new CONSTANT_Utf8_info();
            case CONSTANT_MethodHandle:
                return new CONSTANT_MethodHandle_info();
            case CONSTANT_MethodType:
                return new CONSTANT_MethodType_info();
            case CONSTANT_InvokeDynamic:
                return new CONSTANT_InvokeDynamic_info();
            case PLACEHOLDER:
                return new DataPacket() {
                };
            }
            return null;
        }
    }

    @Override
    public String toString() {
        return "[tag=" + tag + ", info=" + info + "]";
    }
}
