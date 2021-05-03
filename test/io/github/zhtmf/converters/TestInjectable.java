package io.github.zhtmf.converters;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Injectable;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.BYTE;
import io.github.zhtmf.annotations.types.CHAR;

public class TestInjectable {
	
	public static class Child0 extends DataPacket{
		@Injectable
		public List<String> names;
		@Order(0)
		@BYTE
		public int childInteger;
		public Child0(int childInteger) {
			this.childInteger = childInteger;
		}
		public Child0() {
			
		}
	}
	
	public static class ChildA extends Child0{
		@Order(0)
		@BYTE
		public int childInteger1;
		@Injectable("names")
		public List<String> names1;
		public ChildA(int childInteger) {
			super(childInteger);
			this.childInteger1 = childInteger;
		}
		public ChildA() {
			
		}
	}
	
	@Unsigned
	public static class Parent0 extends DataPacket{
        @Order(1)
        @BYTE
        public int integer1;
        @Order(3)
        @CHAR(3)
        @Length(3)
        public List<String> names;
        @Order(4)
        @Length(3)
        public List<ChildA> children;
    }
    
    @Test
    public void test0() throws Exception {
    	
    	List<String> sample = Arrays.asList("abc","def","123");
    	Parent0 parent = new Parent0();
    	parent.names = sample;
    	parent.integer1 = 155;
    	parent.children = Arrays.asList(new ChildA(3), new ChildA(4), new ChildA(5));
    	TestUtils.serializeMultipleTimesAndRestore(parent, 5);
    	
    	byte[] raw = TestUtils.serializeAndGetBytes(parent);
    	Parent0 temp = new Parent0();
    	temp.deserialize(TestUtils.newInputStream(raw));
    	
    	temp.children.forEach(c->{
    		Assert.assertNotNull(c.names);
    		Assert.assertNotNull(c.names1);
    		Assert.assertEquals(c.names, sample);
    		Assert.assertEquals(c.names1, sample);
    	});
    }
    
    public static class A1 extends DataPacket{
    	@Order(1)
        @BYTE
        public int integer1 = 155;
    	@Order(2)
    	public B1 b1 = new B1();
    }
    
    public static class A2 extends A1{
    	@Order(1)
        @BYTE
        public int integer2;
    	@Order(2)
    	public B2 b2 = new B2();
    }
    
    public static class B0 extends DataPacket{
    	@Order(1)
        @CHAR(4)
        public String str = "0123";
    	@Injectable("integer1")
        public Integer int112345;
    }
    
    public static class B1 extends B0{
    	@Order(1)
        @CHAR(3)
        public String str = "123";
    	@Injectable("integer1")
        public int int1;
    }
    
    public static class B2 extends DataPacket{
    	@Order(1)
        @CHAR(3)
        public String str = "456";
    	@Injectable
        public int integer2;
    }
    
    @Test
    public void test1() throws Exception {
    	A2 a2 = new A2();
    	TestUtils.serializeMultipleTimesAndRestore(a2, 11);
    	byte[] res = TestUtils.serializeAndGetBytes(a2);
    	A2 restored = new A2();
    	restored.deserialize(TestUtils.newInputStream(res));
    	assertEquals(restored.b1.int1, restored.integer1);
    	assertEquals(((B0)restored.b1).int112345, (Integer)restored.integer1);
    	assertEquals(restored.b2.integer2, restored.integer2);
    	
    	B2 b2 = new B2();
    	TestUtils.serializeMultipleTimesAndRestore(b2, 5);
    }
    
    @Test
    public void test2() throws Exception {
    	class Test2Class extends DataPacket{
    		@Order(0)
    		@Injectable
    		@BYTE
    		public int integer1 = 3;
    	}
    	
    	try {
            new Test2Class().deserialize(TestUtils.newInputStream(new byte[] {0x05}));
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, ClassInfo.class, 15);
        }
    }
    
    static class TestChild3 extends DataPacket{
		@Order(0)
		@BYTE
		public int subInteger1 = 4;
		@Injectable("not_found")
		public int integer1 = 345;
	}
	
    static class TestParent3 extends DataPacket{
		@Order(0)
		@BYTE
		public int integer1 = 3;
		@Order(1)
		public TestChild3 testChild = new TestChild3();
	}
    
    @Test
    public void test3() throws Exception {
    	try {
            TestUtils.serializeMultipleTimesAndRestore(new TestParent3());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, ClassInfo.class, 16);
        }
    }
    
    static class TestChild31 extends DataPacket{
		@Order(0)
		@BYTE
		public int subInteger1 = 4;
		public int integer1;
		@Injectable
		public void setinteger1(int i) {
			this.integer1 = i;
		}
		@Injectable
		public void se(int i) {
			this.integer1 = i;
		}
	}
    static class TestParent31 extends DataPacket{
		@Order(0)
		@BYTE
		public int integer1 = 3;
		@Order(1)
		public TestChild31 testChild12345 = new TestChild31();
	}
    
    @Test
    public void test31() throws Exception {
    	try {
    		byte[] raw = TestUtils.serializeAndGetBytes(new TestParent31());
    		new TestParent31().deserialize(TestUtils.newInputStream(raw));
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, ClassInfo.class, 16);
        }
    }
    
    static class TestChild4 extends DataPacket{
		@Order(0)
		@BYTE
		public int subInteger1 = 4;
		@Injectable
		public int integer1 = 345;
	}
	
    static class TestParent4 extends DataPacket{
		public static final int integer1 = 3;
		@Order(1)
		public TestChild4 testChild = new TestChild4();
	}
    
    @Test
    public void test4() throws Exception {
    	try {
            TestUtils.serializeMultipleTimesAndRestore(new TestParent4());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, ClassInfo.class, 16);
        }
    }
    
    static class TestChild5 extends DataPacket{
		@Order(0)
		@BYTE
		public int subInteger1 = 4;
		@Injectable
		public int integer1 = 345;
	}
	
    static class TestParent5 extends DataPacket{
    	@Order(0)
		@BYTE
		public long integer1 = 3;
		@Order(1)
		public TestChild5 testChild = new TestChild5();
	}
    
    @Test
    public void test5() throws Exception {
    	try {
            TestUtils.serializeMultipleTimesAndRestore(new TestParent5());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, ClassInfo.class, 17);
        }
    }
    
    static class TestChild6 extends DataPacket{
		@Order(0)
		@BYTE
		public int subInteger1 = 4;
		public int integer1;
		@Injectable("integer1")
		public void injectInteger1(Integer integer, int extra) {
			this.integer1 = integer;
		}
	}
	
    static class TestParent6 extends DataPacket{
    	@Order(0)
		@BYTE
		public long integer1 = 3;
		@Order(1)
		public TestChild6 testChild = new TestChild6();
	}
    
    @Test
    public void test6() throws Exception {
    	try {
            TestUtils.serializeMultipleTimesAndRestore(new TestParent6());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, ClassInfo.class, 18);
        }
    }
    
    static class TestChild7 extends DataPacket{
		@Order(0)
		@BYTE
		public int subInteger1 = 4;
		public int integer1;
		@Injectable("integer1")
		public void injectInteger1(Integer integer) {
			throw new RuntimeException("deliberated exception");
		}
	}
	
    static class TestParent7 extends DataPacket{
    	@Order(0)
		@BYTE
		public long integer1 = 3;
		@Order(1)
		public TestChild7 testChild = new TestChild7();
	}
    
    @Test
    public void test7() throws Exception {
    	try {
            TestUtils.serializeMultipleTimesAndRestore(new TestParent7());
            Assert.fail();
        } catch (Exception e) {
            TestUtils.assertExactException(e, ClassInfo.class, 19);
        }
    }
    
    public static class Child01 extends DataPacket{
    	private List<String> names;
		private List<String> namesAbcdef;
		@Injectable
		public void setNames(List<String> names) {
			this.names = Collections.unmodifiableList(names);
		}
		public List<String> getNames() {
			return names;
		}
		
		public List<String> getNamesAbcdef() {
			return namesAbcdef;
		}
		@Injectable("names")
		public void setNamesAbcdef(List<String> namesAbcdef) {
			this.namesAbcdef = namesAbcdef;
		}

		@Order(0)
		@BYTE
		public int childInteger;
		public Child01(int childInteger) {
			this.childInteger = childInteger;
		}
		public Child01() {
			
		}
	}
	
	public static class ChildA1 extends Child01{
		@Order(0)
		@BYTE
		public int childInteger1;
		
		private List<String> names1;
		
		private List<String> names2;
		
		public List<String> getNames1() {
			return names1;
		}
		
		public List<String> getNames2() {
			return names2;
		}

		@Injectable
		public void names(List<String> names) {
			this.names1 = names;
		}
		
		@Injectable("names")
		public void abcdef(List<String> names) {
			this.names2 = names;
		}
		
		public ChildA1(int childInteger) {
			super(childInteger);
			this.childInteger1 = childInteger;
		}
		public ChildA1() {
			
		}
	}
	
	@Unsigned
	public static class Parent01 extends DataPacket{
        @Order(1)
        @BYTE
        public int integer1;
        @Order(3)
        @CHAR(3)
        @Length(3)
        public List<String> names;
        @Order(4)
        @Length(3)
        public List<ChildA1> children;
    }
    
    @Test
    public void test01() throws Exception {
    	
    	List<String> sample = Arrays.asList("abc","def","123");
    	Parent01 parent = new Parent01();
    	parent.names = sample;
    	parent.integer1 = 155;
    	parent.children = Arrays.asList(new ChildA1(3), new ChildA1(4), new ChildA1(5));
    	TestUtils.serializeMultipleTimesAndRestore(parent, 5);
    	
    	byte[] raw = TestUtils.serializeAndGetBytes(parent);
    	Parent01 temp = new Parent01();
    	temp.deserialize(TestUtils.newInputStream(raw));
    	
    	temp.children.forEach(c->{
    		Assert.assertNotNull(c.getNames());
    		Assert.assertNotNull(c.getNames1());
    		Assert.assertNotNull(c.getNames2());
    		Assert.assertNotNull(c.getNamesAbcdef());
    		Assert.assertEquals(c.getNames(), sample);
    		Assert.assertEquals(c.getNames1(), sample);
    		Assert.assertEquals(c.getNames2(), sample);
    		Assert.assertEquals(c.getNamesAbcdef(), sample);
    	});
    }
    
    static class Test02Child extends DataPacket{
		@Order(0)
		@BYTE
		public int subInteger1 = 4;
		@Injectable
		public static Integer integer1;
		@Injectable("integer1")
		public static void injectInteger1(Integer integer) {
			Test02Child.integer1 = integer;
		}
	}
	
    static class Test02Parent extends DataPacket{
    	@Order(0)
    	@BYTE
		public int integer1 = 3;
		@Order(1)
		public Test02Child testChild = new Test02Child();
	}
    
    @Test
    public void test02() throws Exception {
    	Test02Parent parent = new Test02Parent();
    	byte[] raw = TestUtils.serializeAndGetBytes(parent);
    	Test02Parent tmp = new Test02Parent();
    	tmp.deserialize(TestUtils.newInputStream(raw));
    	assertEquals(Test02Child.integer1, null);
    }
}
