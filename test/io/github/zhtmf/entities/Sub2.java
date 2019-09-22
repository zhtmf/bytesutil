package io.github.zhtmf.entities;

import io.github.zhtmf.annotations.modifiers.CHARSET;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.BYTE;
import io.github.zhtmf.annotations.types.CHAR;

public class Sub2 extends Base {
    @Order(0)
    @CHAR(6)
    public String str1;
    @Order(1)
    @CHAR(1)
    public String str2;
    @Order(2)
    @CHAR(2)
    public String str3;
    @Order(3)
    @BYTE
    @Unsigned
    public int type2;
    @Order(4)
    @CHAR(10)
    @CHARSET(handler=PropertyHandler1.class)
    public String str4;
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((str1 == null) ? 0 : str1.hashCode());
        result = prime * result + ((str2 == null) ? 0 : str2.hashCode());
        result = prime * result + ((str3 == null) ? 0 : str3.hashCode());
        result = prime * result + ((str4 == null) ? 0 : str4.hashCode());
        result = prime * result + type2;
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
        Sub2 other = (Sub2) obj;
        if (str1 == null) {
            if (other.str1 != null)
                return false;
        } else if (!str1.equals(other.str1))
            return false;
        if (str2 == null) {
            if (other.str2 != null)
                return false;
        } else if (!str2.equals(other.str2))
            return false;
        if (str3 == null) {
            if (other.str3 != null)
                return false;
        } else if (!str3.equals(other.str3))
            return false;
        if (str4 == null) {
            if (other.str4 != null)
                return false;
        } else if (!str4.equals(other.str4))
            return false;
        if (type2 != other.type2)
            return false;
        return true;
    }
    
    
    
}
