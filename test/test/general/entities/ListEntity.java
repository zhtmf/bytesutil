package test.general.entities;

import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Signed;
import org.dzh.bytesutil.annotations.types.BYTE;

public class ListEntity extends DataPacket{
    public static final int DEFAULT_TEMP = 3;
    public int temp = DEFAULT_TEMP;
    @BYTE
    @Signed
    @Order(0)
    public byte a;
}
