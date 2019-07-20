package examples.mysql.connector.packet.command;

import examples.mysql.connector.datatypes.le.LEIntHandler;
import examples.mysql.connector.datatypes.le.LEInteger;
import examples.mysql.connector.datatypes.string.LengthEncodedString;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.LittleEndian;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.modifiers.Variant;
import io.github.zhtmf.annotations.types.BYTE;
import io.github.zhtmf.annotations.types.INT;
import io.github.zhtmf.annotations.types.RAW;
import io.github.zhtmf.annotations.types.SHORT;

@LittleEndian
@Unsigned
public class ColumnDefinition extends DataPacket{
    @Order(0)
    //The catalog used. Currently always "def"
    public LengthEncodedString catalog;
    @Order(1)
    //schema name
    public LengthEncodedString schema;
    @Order(2)
    //virtual table name
    public LengthEncodedString table;
    @Order(3)
    //physical table name
    public LengthEncodedString orgTable;
    @Order(4)
    //virtual column name
    public LengthEncodedString name;
    @Order(5)
    //physical column name
    public LengthEncodedString orgName;
    @Order(6)
    @Variant(LEIntHandler.class)
    //length of fixed length fields 0x0c
    public LEInteger fixedLengthFieldsLength;
    @Order(7)
    @SHORT
    public int characterSet;
    @Order(8)
    @INT
    //maximum length of the field
    public long columnLength;
    @Order(9)
    @BYTE
    //type of the column as defined in enum_field_types
    public int type;
    @Order(10)
    @SHORT
    //Flags as defined in Column Definition Flags
    public int flags;
    @Order(11)
    @BYTE
    /*
     *  max shown decimal digits:
        0x00 for integers and static strings
        0x1f for dynamic strings, double, float
        0x00 to 0x51 for decimals
     */
    public int decimals;
    
    /**
     * not specified in 8.0 document
     */
    @Order(12)
    @RAW(2)
    public byte[] filler;
    
    @Override
    public String toString() {
        return "ColumnDefinition [catalog=" + catalog + ", schema=" + schema + ", table=" + table + ", orgTable="
                + orgTable + ", name=" + name + ", orgName=" + orgName + ", fixedLengthFieldsLength="
                + fixedLengthFieldsLength + ", characterSet=" + characterSet + ", columnLength=" + columnLength
                + ", type=" + type + ", flags=" + flags + ", decimals=" + decimals + "]";
    }
}
