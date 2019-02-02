package test.exceptions;

public class TestCaseShortConverter {
//	public static class Entity0 extends DataPacket{
//		@Order(0)
//		@CHAR
//		@Length
//		public short b;
//	}
//	@Test
//	public void test0() throws ConversionException {
//		Entity0 entity = new Entity0();
//		try {
//			entity.b = -3;
//			entity.serialize(TestUtils.newByteArrayOutputStream());
//			Assert.fail();
//		} catch (Exception e) {
//			TestUtils.assertExactException(e, Utils.class, 0);
//			return;
//		}
//	}
//	public static class Entity1 extends DataPacket{
//		@Order(0)
//		@CHAR(2)
//		public short b;
//	}
//	@Test
//	public void test1() throws ConversionException {
//		Entity1 entity = new Entity1();
//		try {
//			entity.b = 120;
//			entity.serialize(TestUtils.newByteArrayOutputStream());
//			Assert.fail();
//		} catch (Exception e) {
//			TestUtils.assertExactException(e, Utils.class, 2);
//			return;
//		}
//	}
//	public static class Entity2 extends DataPacket{
//		@Order(0)
//		@CHAR(2)
//		public short b;
//	}
//	@Test
//	public void test2() throws ConversionException {
//		Entity2 entity = new Entity2();
//		try {
//			entity.deserialize(TestUtils.newInputStream(new byte[] {(byte)'0',(byte)'9'}));
//			Assert.fail();
//		} catch (Exception e) {
//			TestUtils.assertExactException(e, ShortConverter.class, 2);
//		}
//		try {
//			entity.deserialize(TestUtils.newInputStream(new byte[] {(byte)'1',(byte)'/'}));
//			Assert.fail();
//		} catch (Exception e) {
//			TestUtils.assertExactException(e, ShortConverter.class, 2);
//		}
//		try {
//			entity.deserialize(TestUtils.newInputStream(new byte[] {(byte)'1',(byte)';'}));
//			Assert.fail();
//		} catch (Exception e) {
//			TestUtils.assertExactException(e, ShortConverter.class, 2);
//		}
//	}
}