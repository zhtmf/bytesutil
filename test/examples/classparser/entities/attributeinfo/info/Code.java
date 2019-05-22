package examples.classparser.entities.attributeinfo.info;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import examples.classparser.entities.attributeinfo.AttributeInfo;
import examples.classparser.entities.bytecodes.Instruction;
import examples.classparser.entities.cpinfo.CpInfo;
import io.github.zhtmf.ConversionException;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.modifiers.Variant;
import io.github.zhtmf.annotations.types.RAW;
import io.github.zhtmf.annotations.types.SHORT;
import io.github.zhtmf.converters.auxiliary.DataType;
import io.github.zhtmf.converters.auxiliary.EntityHandler;

@Unsigned
public class Code extends DataPacket{
    @Order(0)
    @SHORT
    public int maxStack;
    @Order(1)
    @SHORT
    public int maxLocals;
    @Order(2)
    @RAW
    @Length(type=DataType.INT)
    public byte[] code;
    private List<Instruction> instructions;
    @Order(3)
    @Length(type=DataType.SHORT)
    public List<ExceptionTable> exceptionTable;
    @Order(4)
    @Length(type=DataType.SHORT)
    @Variant(AttributeInfoHandler2.class)
    public List<AttributeInfo> attributes;
    
    private List<CpInfo> constantPool;
    public Code(List<CpInfo> constantPool) {
        this.constantPool = constantPool;
    }
    
    public static class AttributeInfoHandler2 extends EntityHandler{
        @Override
        public DataPacket handle0(String fieldName, Object entity, InputStream is) throws IOException {
            Code info = (Code)entity;
            return new AttributeInfo(Collections.unmodifiableList(info.constantPool));
        }
        
    }
    /*
     * deserialize list of jvm instructions manually,
     * as the length contained in class file is length of raw code bytes,
     *  not count of instructions.
     */
    public List<Instruction> getInstructions() {
        if(instructions==null) {
            int offset = 0;
            List<Instruction> ret = new ArrayList<>();
            ByteArrayInputStream bais = new ByteArrayInputStream(code);
            int pos = bais.available();
            while(pos>0) {
                Instruction instruction = new Instruction(offset);
                try {
                    instruction.deserialize(bais);
                } catch (IllegalArgumentException | ConversionException e) {
                    throw new Error(e);
                }
                offset += instruction.length();
                ret.add(instruction);
                pos = bais.available();
            }
            instructions = ret;
        }
        return instructions;
    }
    
    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        ret.append("Code: ");ret.append("\n");
        for(Instruction instruction:getInstructions()) {
            ret.append("    ").append(instruction);ret.append("\n");
        }
        return ret.toString();
    }
}
