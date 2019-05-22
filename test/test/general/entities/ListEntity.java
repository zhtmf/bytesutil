package test.general.entities;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Signed;
import io.github.zhtmf.annotations.types.BYTE;

public class ListEntity extends DataPacket{
    public static final int DEFAULT_TEMP = 3;
    public int temp = DEFAULT_TEMP;
    @BYTE
    @Signed
    @Order(0)
    public byte a;
}
