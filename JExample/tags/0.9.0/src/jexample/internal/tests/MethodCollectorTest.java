/**
 * 
 */
package jexample.internal.tests;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.util.HashMap;

import jexample.Depends;
import jexample.internal.MethodCollector;
import jexample.internal.TestClass;
import jexample.internal.TestMethod;

import org.junit.Test;


/**
 * @author Lea Haensenberger (lhaensenberger at students.unibe.ch)
 */
public class MethodCollectorTest {

	public MethodCollectorTest() {
	}

	@Test
	public void testInternalDeps() throws SecurityException, NoSuchMethodException, ClassNotFoundException {
		MethodCollector coll = new MethodCollector( new TestClass( InternalDeps.class ),
				new HashMap<Method,TestMethod>() );
		assertEquals( 3, coll.collectTestMethods().size() );

		Method method1 = InternalDeps.class.getMethod( "test1" );
		Method method2 = InternalDeps.class.getMethod( "test2" );
		Method method3 = InternalDeps.class.getMethod( "test3" );
		assertEquals( 0, coll.collectTestMethods().get( method1 ).getDependencies().size() );
		assertEquals( 1, coll.collectTestMethods().get( method2 ).getDependencies().size() );
		assertEquals( 1, coll.collectTestMethods().get( method3 ).getDependencies().size() );
	}

	private class InternalDeps {

		public InternalDeps() {

		}

		@Test
		public void test1() {

		}

		@Test
		@Depends( "test1" )
		public void test2() {

		}

		@Test
		@Depends( "test1" )
		public void test3() {

		}
	}

	@Test
	public void testInternalDepsWithCycle() throws SecurityException, NoSuchMethodException, ClassNotFoundException {
		MethodCollector coll = new MethodCollector( new TestClass( InternalDepsWithCycle.class ),
				new HashMap<Method,TestMethod>() );
		assertEquals( 4, coll.collectTestMethods().size() );

		Method method0 = InternalDepsWithCycle.class.getMethod( "test0" );
		Method method1 = InternalDepsWithCycle.class.getMethod( "test1" );
		Method method2 = InternalDepsWithCycle.class.getMethod( "test2" );
		Method method3 = InternalDepsWithCycle.class.getMethod( "test3" );
		assertEquals( 0, coll.collectTestMethods().get( method0 ).getDependencies().size() );
		assertEquals( 2, coll.collectTestMethods().get( method1 ).getDependencies().size() );
		assertEquals( 1, coll.collectTestMethods().get( method2 ).getDependencies().size() );
		assertEquals( 1, coll.collectTestMethods().get( method3 ).getDependencies().size() );
	}

	private class InternalDepsWithCycle {

		public InternalDepsWithCycle() {

		}

		@Test
		public void test0() {

		}

		@Test
		@Depends( "test0;test3" )
		public void test1() {

		}

		@Test
		@Depends( "test1" )
		public void test2() {

		}

		@Test
		@Depends( "test2" )
		public void test3() {

		}
	}

	@Test
	public void testExternalDeps() throws SecurityException, NoSuchMethodException, ClassNotFoundException {
		MethodCollector coll = new MethodCollector( new TestClass( ExternalDeps.class ),
				new HashMap<Method,TestMethod>() );
		assertEquals( 5, coll.collectTestMethods().size() );

		Method method1 = ExternalDeps.class.getMethod( "test1" );
		Method method2 = ExternalDeps.class.getMethod( "test2" );
		Method method3 = ExternalDeps.class.getMethod( "test3" );
		assertEquals( 0, coll.collectTestMethods().get( method1 ).getDependencies().size() );
		assertEquals( 1, coll.collectTestMethods().get( method2 ).getDependencies().size() );
		assertEquals( 2, coll.collectTestMethods().get( method3 ).getDependencies().size() );
	}

	private class ExternalDeps {

		public ExternalDeps() {

		}

		@Test
		public void test1() {

		}

		@Test
		@Depends( "test1" )
		public void test2() {

		}

		@Test
		@Depends( "test1;B.middleMethod" )
		public void test3() {

		}
	}

	@Test
	public void testExternalDepsWithCycle() throws SecurityException, NoSuchMethodException, ClassNotFoundException {
		MethodCollector coll = new MethodCollector( new TestClass( ExternalDepsWithCycle.class ),
				new HashMap<Method,TestMethod>() );
		assertEquals( 4, coll.collectTestMethods().size() );

		Method method1 = ExternalDepsWithCycle.class.getMethod( "test1" );
		Method method2 = ExternalDepsWithCycle.class.getMethod( "test2" );
		Method method3 = ExternalDepsWithCycle.class.getMethod( "test3" );
		assertEquals( 1, coll.collectTestMethods().get( method1 ).getDependencies().size() );
		assertEquals( 1, coll.collectTestMethods().get( method2 ).getDependencies().size() );
		assertEquals( 2, coll.collectTestMethods().get( method3 ).getDependencies().size() );
	}

	private class ExternalDepsWithCycle {

		public ExternalDepsWithCycle() {

		}

		@Test
		@Depends( "test3" )
		public void test1() {

		}

		@Test
		@Depends( "test1" )
		public void test2() {

		}

		@Test
		@Depends( "test1;A.test4" )
		public void test3() {

		}
	}
}
