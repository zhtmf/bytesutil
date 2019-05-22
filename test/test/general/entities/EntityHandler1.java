package test.general.entities;

import java.io.IOException;
import java.io.InputStream;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.converters.auxiliary.EntityHandler;
import io.github.zhtmf.converters.auxiliary.StreamUtils;

public class EntityHandler1 extends EntityHandler{

    @Override
    public DataPacket handle0(String fieldName, Object entity, InputStream is) throws IOException {
        //dataType
        int b = StreamUtils.readByte(is, true);
        if(b==1) {
            return new Sub1();
        }else if(b==2) {
            return new Sub2();
        }
        throw new Error("unknown b value:"+b);
    }
}
