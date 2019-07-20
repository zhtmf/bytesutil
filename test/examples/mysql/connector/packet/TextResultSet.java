package examples.mysql.connector.packet;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import examples.mysql.connector.auxiliary.CapabilityFlags;
import examples.mysql.connector.datatypes.le.LEIntHandler;
import examples.mysql.connector.datatypes.le.LEInteger;
import examples.mysql.connector.datatypes.result.ColumnDefinition;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Conditional;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.LittleEndian;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.modifiers.Variant;
import io.github.zhtmf.annotations.types.BYTE;
import io.github.zhtmf.converters.auxiliary.ModifierHandler;

/**
 * A Text Resultset is a possible COM_QUERY Response. 
 * It is made up of 2 parts:
 * the column definitions (a.k.a. the metadata) 
 * the actual rows
 * @author dzh
 */
@LittleEndian
@Unsigned
public class TextResultSet extends DataPacket{
    public int clientCapabilities;
    //injected by outer MySQLPacket object
    public int selfLength;
    
    @Order(0)
    @BYTE
    @Conditional(Conditionals.class)
    public byte metadataFollows;
    
    @Order(1)
    @Variant(LEIntHandler.class)
    public LEInteger columnCount;
    
    @Order(2)
    @Length(handler=LengthHandler.class)
    public List<ColumnDefinition> fieldMetaData;
    
    public static class LengthHandler extends ModifierHandler<Integer>{

        @Override
        public Integer handleDeserialize0(String fieldName, Object entity, InputStream is) throws IOException {
            return ((TextResultSet)entity).columnCount.getNumericValue().intValue();
        }

        @Override
        public Integer handleSerialize0(String fieldName, Object entity) {
            return ((TextResultSet)entity).columnCount.getNumericValue().intValue();
        }
    }
    
    public static class Conditionals extends ModifierHandler<Boolean>{
        @Override
        public Boolean handleDeserialize0(String fieldName, Object entity, InputStream is) throws IOException {
            return handleSerialize0(fieldName, entity);
        }
        @Override
        public Boolean handleSerialize0(String fieldName, Object entity) {
            TextResultSet pac = (TextResultSet)entity;
            switch(fieldName) {
            case "metadataFollows":
                return (pac.clientCapabilities & CapabilityFlags.CLIENT_OPTIONAL_RESULTSET_METADATA) != 0;
            }
            throw new IllegalArgumentException(fieldName);
        }
    }
}
