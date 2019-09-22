package io.github.zhtmf.entities;

import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Signed;
import io.github.zhtmf.annotations.types.INT;
import io.github.zhtmf.annotations.types.SHORT;

public class Sub1 extends Base {
    @Order(0)
    @INT
    @Signed
    public int field1;
    @Order(1)
    @SHORT
    public int field2;
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + field1;
        result = prime * result + field2;
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        Sub1 other = (Sub1) obj;
        if (field1 != other.field1)
            return false;
        if (field2 != other.field2)
            return false;
        return true;
    }
}
