package examples.classparser.entities.cpinfo;

import java.io.IOException;
import java.io.InputStream;

import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.CHARSET;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Unsigned;
import org.dzh.bytesutil.annotations.modifiers.Variant;
import org.dzh.bytesutil.annotations.types.BYTE;
import org.dzh.bytesutil.converters.auxiliary.EntityHandler;

import examples.classparser.entities.cpinfo.info.CONSTANT_Class_info;
import examples.classparser.entities.cpinfo.info.CONSTANT_Double_info;
import examples.classparser.entities.cpinfo.info.CONSTANT_Fieldref_info;
import examples.classparser.entities.cpinfo.info.CONSTANT_Float_info;
import examples.classparser.entities.cpinfo.info.CONSTANT_Integer_info;
import examples.classparser.entities.cpinfo.info.CONSTANT_InterfaceMethodref_info;
import examples.classparser.entities.cpinfo.info.CONSTANT_InvokeDynamic_info;
import examples.classparser.entities.cpinfo.info.CONSTANT_Long_info;
import examples.classparser.entities.cpinfo.info.CONSTANT_MethodHandle_info;
import examples.classparser.entities.cpinfo.info.CONSTANT_MethodType_info;
import examples.classparser.entities.cpinfo.info.CONSTANT_Methodref_info;
import examples.classparser.entities.cpinfo.info.CONSTANT_NameAndType_info;
import examples.classparser.entities.cpinfo.info.CONSTANT_String_info;
import examples.classparser.entities.cpinfo.info.CONSTANT_Utf8_info;

@Unsigned
@CHARSET("UTF-8")
public class CpInfo extends DataPacket{
    @Order(0)
    @BYTE
    public CPInfoTag tag;
    @Order(1)
    @Variant(CpInfoHandler.class)
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
            }
            return null;
        }
    }

    @Override
    public String toString() {
        return "[tag=" + tag + ", info=" + info + "]";
    }
}
