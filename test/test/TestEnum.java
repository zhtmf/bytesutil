package test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.dzh.bytesutil.ConversionException;
import org.dzh.bytesutil.DataPacket;
import org.dzh.bytesutil.annotations.enums.NumericEnum;
import org.dzh.bytesutil.annotations.enums.StringEnum;
import org.dzh.bytesutil.annotations.modifiers.CHARSET;
import org.dzh.bytesutil.annotations.modifiers.Length;
import org.dzh.bytesutil.annotations.modifiers.Order;
import org.dzh.bytesutil.annotations.modifiers.Unsigned;
import org.dzh.bytesutil.annotations.types.BYTE;
import org.dzh.bytesutil.annotations.types.CHAR;
import org.dzh.bytesutil.annotations.types.INT;
import org.junit.Assert;
import org.junit.Test;

/*
 * 支持整数类型和字符串类型和枚举类型之间的转换
 * 枚举类型需要实现自定义接口I1或I2的getValue方法，返回各枚举成员对应的值（数字或者字符串）。
 * 如果不实现接口，需要用toString()方法返回包含对应值的字符串（如果制定映射到数字会尝试对做转换，失败时报错）
 * 枚举类上不需要用额外的注解来指明，使用和其他属性一致的BYTE INT，CHAR等注解来定义行为
 * 
 * I1 = NumericEnum
 * 定义方法 long getValue()
 * 返回值要和同一属性上的数字类型注解(BYTE INT SHORT)做比对，如果越界要报错。
 * 要检查多个不同的枚举属性返回值是否相同，如有相同要报错
 * 
 * I2 = StringEnum
 * 定义方法 String getValue()
 * 返回值不可为NULL
 * 要检查多个不同的枚举属性返回值是否相同，如有相同要报错
 * 
 * 将这个属性的FieldInfo的fieldClass设置为long/java.lang.String。
 * 在fieldInfo.get里，返回long值或者返回string给调用方法
 * 在fieldInfo.set里，要判断如果是枚举类，要转换为对应的枚举成员值并设置类字段的值
 */
public class TestEnum {
	
	private enum NEnum1 implements NumericEnum{
		FLAG1 {
			@Override
			public long getValue() {
				return 60000;
			}
		},
		FLAG2 {
			@Override
			public long getValue() {
				return 300;
			}
		};
		@Override
		public abstract long getValue();
	}
	
	private enum NEnum2{
		FLAG1 {
			@Override
			public String toString() {
				return "127";
			}
		},
		FLAG2 {
			@Override
			public String toString() {
				return "255";
			}
		};
	}
	
	private enum SEnum1 implements StringEnum{
		FLAG1 {
			@Override
			public String getValue() {
				return "SUCCESS";
			}
		},
		FLAG2 {
			@Override
			public String getValue() {
				return "FAILURE";
			}
		};
	}
	
	private enum SEnum2{
		FLAG1 {
			@Override
			public String toString() {
				return "成功";
			}
		},
		FLAG2 {
			@Override
			public String toString() {
				return "失败";
			}
		};
	}

	@CHARSET("UTF-8")
	public static class Test1 extends DataPacket{
		@INT
		@Order(0)
		public NEnum1 nenum1;
		@BYTE
		@Unsigned
		@Order(1)
		public NEnum2 nenum2;
		@CHAR(7)
		@Order(2)
		public SEnum1 senum1;
		@CHAR
		@Length(6)
		@Order(3)
		public SEnum2 senum2;
	}
	
	@Test
	public void test() throws ConversionException {
		Test1 src = new Test1();
		Test1 dest = new Test1();
		{
			src.nenum1 = NEnum1.FLAG1;
			src.nenum2 = NEnum2.FLAG2;
			src.senum1 = SEnum1.FLAG1;
			src.senum2 = SEnum2.FLAG2;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			src.serialize(baos);
			dest.deserialize(new ByteArrayInputStream(baos.toByteArray()));
			Assert.assertEquals(src.nenum1, dest.nenum1);
			Assert.assertEquals(src.nenum2, dest.nenum2);
			Assert.assertEquals(src.senum1, dest.senum1);
			Assert.assertEquals(src.senum2, dest.senum2);
		}
		{
			src.nenum1 = NEnum1.FLAG2;
			src.nenum2 = NEnum2.FLAG1;
			src.senum1 = SEnum1.FLAG2;
			src.senum2 = SEnum2.FLAG1;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			src.serialize(baos);
			dest.deserialize(new ByteArrayInputStream(baos.toByteArray()));
			Assert.assertEquals(src.nenum1, dest.nenum1);
			Assert.assertEquals(src.nenum2, dest.nenum2);
			Assert.assertEquals(src.senum1, dest.senum1);
			Assert.assertEquals(src.senum2, dest.senum2);
		}
	}
	
	public static void main(String[] args) throws IllegalArgumentException, ConversionException {
		new TestEnum().test();
	}
}
