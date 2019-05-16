package test.exceptions;

import java.io.ByteArrayOutputStream;

import org.dzh.bytesutil.ConversionException;
import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.enums.NumericEnum;
import org.dzh.bytesutil.annotations.enums.StringEnum;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Unsigned;
import org.dzh.bytesutil.annotations.types.BYTE;
import org.dzh.bytesutil.annotations.types.CHAR;
import org.dzh.bytesutil.converters.auxiliary.EnumFieldInfo;
import org.junit.Assert;
import org.junit.Test;

import test.TestUtils;

public class TestEnumFieldInfo {
    private enum NEnum1 implements NumericEnum{
        FLAG1 {
            @Override
            public long getValue() {
                return 1;
            }
        },
        FLAG2 {
            @Override
            public long getValue() {
                return 1;
            }
        };
        @Override
        public abstract long getValue();
    }
    private enum SEnum1 implements StringEnum{
        FLAG1 {
            @Override
            public String getValue() {
                return "a";
            }
        },
        FLAG2 {
            @Override
            public String getValue() {
                return "a";
            }
        };
        @Override
        public abstract String getValue();
    }
    private enum SEnum2 implements StringEnum{
        FLAG1 {
            @Override
            public String getValue() {
                return null;
            }
        },
        FLAG2 {
            @Override
            public String getValue() {
                return "a";
            }
        };
        @Override
        public abstract String getValue();
    }
    private enum SEnum3 implements StringEnum{
        FLAG1 {
            @Override
            public String getValue() {
                return "c";
            }
        },
        FLAG2 {
            @Override
            public String getValue() {
                return "a";
            }
        };
        @Override
        public abstract String getValue();
    }
    private enum NEnum2 implements NumericEnum{
        FLAG1 {
            @Override
            public long getValue() {
                return 100000;
            }
        },
        FLAG2 {
            @Override
            public long getValue() {
                return 100000;
            }
        };
        @Override
        public abstract long getValue();
    }
    @Test
    public void test0() throws ConversionException {
        try {
            class Entity extends DataPacket{@Order(0)@CHAR public NEnum1 enm = NEnum1.FLAG1;}
            new Entity().serialize(new ByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactExceptionInHierarchy(e, EnumFieldInfo.class, 3);
        }
        try {
            class Entity extends DataPacket{@Order(0)@BYTE public NEnum1 enm = NEnum1.FLAG1;}
            new Entity().serialize(new ByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactExceptionInHierarchy(e, EnumFieldInfo.class, 2);
        }
        try {
            class Entity extends DataPacket{@Order(0)@BYTE public SEnum1 enm = SEnum1.FLAG1;}
            new Entity().serialize(new ByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactExceptionInHierarchy(e, EnumFieldInfo.class, 1);
        }
        try {
            class Entity extends DataPacket{@Order(0)@CHAR(1) public SEnum1 enm = SEnum1.FLAG1;}
            new Entity().serialize(new ByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactExceptionInHierarchy(e, EnumFieldInfo.class, 5);
        }
        try {
            class Entity extends DataPacket{@Order(0)@CHAR(1) public SEnum2 enm = SEnum2.FLAG1;}
            new Entity().serialize(new ByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactExceptionInHierarchy(e, EnumFieldInfo.class, 4);
        }
        try {
            class Entity extends DataPacket{@Order(0)@CHAR(1) public SEnum3 enm = SEnum3.FLAG1;}
            new Entity().deserialize(TestUtils.newInputStream(new byte[] {'x'}));
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactExceptionInHierarchy(e, EnumFieldInfo.class, 6);
        }
        try {
            class Entity extends DataPacket{@Order(0)@Unsigned@BYTE public NEnum2 enm = NEnum2.FLAG1;}
            new Entity().serialize(new ByteArrayOutputStream());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactExceptionInHierarchy(e, EnumFieldInfo.class, 7);
        }
    }
}
