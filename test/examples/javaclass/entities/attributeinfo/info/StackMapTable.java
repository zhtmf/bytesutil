package examples.javaclass.entities.attributeinfo.info;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import examples.javaclass.entities.attributeinfo.info.stackmapframe.AppendFrame;
import examples.javaclass.entities.attributeinfo.info.stackmapframe.ChopFrame;
import examples.javaclass.entities.attributeinfo.info.stackmapframe.FullFrame;
import examples.javaclass.entities.attributeinfo.info.stackmapframe.SameFrame;
import examples.javaclass.entities.attributeinfo.info.stackmapframe.SameFrameExtended;
import examples.javaclass.entities.attributeinfo.info.stackmapframe.SameLocals1StackItemFrame;
import examples.javaclass.entities.attributeinfo.info.stackmapframe.SameLocals1StackItemFrameExtended;
import examples.javaclass.entities.attributeinfo.info.stackmapframe.StackMapFrame;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.modifiers.Variant;
import io.github.zhtmf.converters.auxiliary.DataType;
import io.github.zhtmf.converters.auxiliary.EntityHandler;
import io.github.zhtmf.converters.auxiliary.StreamUtils;

@Unsigned
public class StackMapTable extends DataPacket{
    @Order(0)
    @Length(type=DataType.SHORT)
    @Variant(StackMapFrameHandler.class)
    //number_of_entries implicit in this declaration
    public List<StackMapFrame> entries;
    
    public static class StackMapFrameHandler extends EntityHandler{

        @Override
        public DataPacket handle0(String fieldName, Object entity, InputStream is) throws IOException {
            //read frame_type ahead
            int frameType = StreamUtils.readByte(is, false);
            if(frameType>=0 && frameType<=63) {
                return new SameFrame();
            }else if(frameType >=64 && frameType<=127) {
                return new SameLocals1StackItemFrame();
            }else if(frameType == 247) {
                return new SameLocals1StackItemFrameExtended();
            }else if(frameType>=248 && frameType<=250) {
                return new ChopFrame();
            }else if(frameType == 251) {
                return new SameFrameExtended();
            }else if(frameType>=252 && frameType<=254) {
                return new AppendFrame();
            }else if(frameType==255) {
                return new FullFrame();
            }
            throw new Error("invalid frametype:"+frameType);
        }
    }
}
