package examples.mysql.connector.datatypes.le;

import java.io.IOException;
import java.io.InputStream;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.converters.auxiliary.EntityHandler;

public class LEIntHandler extends EntityHandler{

    @Override
    public DataPacket handle0(String fieldName, Object entity, InputStream is) throws IOException {
        int first = is.read();
        if(first>=0 && first<251) {
            return new LEInt1();
        }
        switch(first) {
        case 0xFC:return new LEInt2();
        case 0xFD:return new LEInt3();
        case 0xFE:return new LEInt8();
        default:
            throw new UnsupportedOperationException("unknown header:"+Integer.toHexString(first));
        }
    }

}
