package examples.mysql.connector.datatypes.string;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

import examples.mysql.connector.datatypes.le.LEInt8;
import examples.mysql.connector.datatypes.le.LEIntHandler;
import examples.mysql.connector.datatypes.le.LEInteger;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.modifiers.Variant;
import io.github.zhtmf.annotations.types.CHAR;
import io.github.zhtmf.converters.auxiliary.ModifierHandler;

//A length encoded string is a string that is prefixed with length encoded integer describing the length of the string.
@Unsigned
public class LengthEncodedString extends DataPacket{

    @Order(0)
    @Variant(LEIntHandler.class)
    public LEInteger length;
    
    @Order(1)
    @CHAR
    @Length(handler=LengthHandler.class)
    public String actualString;
    
    public LengthEncodedString() {
    }
    
    public LengthEncodedString(String str) {
        this.actualString = str;
        LEInt8 i8 = new LEInt8();
        i8.value = BigInteger.valueOf(str.length());
        this.length = i8;
    }
    
    public static class LengthHandler extends ModifierHandler<Integer>{

        @Override
        public Integer handleDeserialize0(String fieldName, Object entity, InputStream is) throws IOException {
            return ((LengthEncodedString)entity).length.getNumericValue().intValue();
        }

        @Override
        public Integer handleSerialize0(String fieldName, Object entity) {
            return ((LengthEncodedString)entity).length.getNumericValue().intValue();
        }
    }

    @Override
    public String toString() {
        return "LengthEncodedString [length=" + length + ", actualString=" + actualString + "]";
    }
}
