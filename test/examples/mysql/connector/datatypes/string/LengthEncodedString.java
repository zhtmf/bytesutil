package examples.mysql.connector.datatypes.string;

import java.math.BigInteger;

import examples.mysql.connector.datatypes.le.LEInt8;
import examples.mysql.connector.datatypes.le.LEIntHandler;
import examples.mysql.connector.datatypes.le.LEInteger;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Script;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.modifiers.Variant;
import io.github.zhtmf.annotations.types.CHAR;

//A length encoded string is a string that is prefixed with length encoded integer describing the length of the string.
@Unsigned
public class LengthEncodedString extends DataPacket{

    @Order(0)
    @Variant(LEIntHandler.class)
    public LEInteger integerLength;
    
    @Order(1)
    @CHAR
    @Length(scripts = @Script("entity.integerLength.numericValue"))
    public String actualString;
    
    public LengthEncodedString() {
    }
    
    public LengthEncodedString(String str) {
        this.actualString = str;
        LEInt8 i8 = new LEInt8();
        i8.value = BigInteger.valueOf(str.length());
        this.integerLength = i8;
    }

    @Override
    public String toString() {
        return "LengthEncodedString [length=" + integerLength + ", actualString=" + actualString + "]";
    }
}
