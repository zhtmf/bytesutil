package examples.javaclass.entities.bytecodes;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.BYTE;
import io.github.zhtmf.annotations.types.RAW;
import io.github.zhtmf.converters.auxiliary.ModifierHandler;

@Unsigned
public class Instruction extends DataPacket{
    @Order(0)
    @BYTE
    private OpCode opcode;
    @Order(1)
    @RAW
    @Length(handler=BytesLengthHandler.class)
    private byte[] otherBytes;
    
    //address offset in the bytes of the same method
    private int offset;
    public Instruction(int offset) {
        this.offset = offset;
    }
    
    public static final class BytesLengthHandler extends ModifierHandler<Integer>{

        @Override
        public Integer handleDeserialize0(String fieldName, Object entity, InputStream is) throws IOException {
            Instruction instruction = (Instruction)entity;
            switch(instruction.opcode) {
            //variable length instructions
            case tableswitch:{
                /*
                 * 0 to 3 bytes of padding zeros are inserted, so that the
                 * default_offset parameter starts at an offset in the bytecode which is a
                 * multiple of 4
                 * +1 for opcode itself
                 */
                int padding = 4-(instruction.offset+1)%4;
                padding = padding == 4 ? 0 : padding;
                for(int i=0;i<padding;++i) {
                    is.read();
                }
                byte[] integer = new byte[4];
                is.read(integer);//default_offset 
                is.read(integer);//low
                int low = getInt(integer);
                is.read(integer,0,4);
                int high = getInt(integer);
                return 12+padding + (high-low+1)*4;
            }
            case lookupswitch:{
                /*
                 * 0 to 3 bytes of padding zeros are inserted, so that the
                 * default_offset parameter starts at an offset in the bytecode which is a
                 * multiple of 4
                 * +1 for opcode itself
                 */
                int padding = 4-(instruction.offset+1)%4;
                padding = padding == 4 ? 0 : padding;
                for(int i=0;i<padding;++i) {
                    is.read();
                }
                byte[] integer = new byte[4];
                is.read(integer);//default_offset 
                is.read(integer);//n
                int n = getInt(integer);
                return 8+padding+n*8;
            }
            case wide:{
                int nextInstruction = is.read();
                if(nextInstruction == OpCode.iinc.ordinal()) {
                    return 4;
                }
                return 2;
            }
            default:
                return instruction.opcode.getOtherBytesLength();
            }
        }

        @Override
        public Integer handleSerialize0(String fieldName, Object entity) {
            Instruction instruction = (Instruction)entity;
            return instruction.otherBytes.length;
        }
        
        private int getInt(byte[] array) {
            return array[0]<<24 | array[1]<<16 | array[2]<<8 | array[3];
        }
        
    }
    
    @Override
    public String toString() {
        return opcode +": "+Arrays.toString(otherBytes);
    }
}
