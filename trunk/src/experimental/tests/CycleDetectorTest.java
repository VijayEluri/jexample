/**
 * 
 */
package experimental.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.internal.runners.InitializationError;

import experimental.CycleDetector;
import experimental.TestClass;
import extension.annotations.Depends;
import extension.annotations.MyTest;

/**
 * @author Lea Haensenberger (lhaensenberger at students.unibe.ch)
 */
public class CycleDetectorTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {

	}


	/**
	 * Test method for {@link experimental.CycleDetector#testHasNoCycles()}.
	 * @throws NoSuchMethodException 
	 * @throws ClassNotFoundException 
	 * @throws InitializationError 
	 * @throws SecurityException 
	 */
	@Test
	public void testHasNoCycles() throws SecurityException, InitializationError, ClassNotFoundException, NoSuchMethodException {
		CycleDetector detector = new CycleDetector( new TestClass( WithoutCycles.class ) );
		assertEquals( 3, detector.checkCyclesAndGetMethods().size() );
	}

	/**
	 * Test method for {@link experimental.CycleDetector#testHasCycles()}.
	 * @throws NoSuchMethodException 
	 * @throws ClassNotFoundException 
	 * @throws InitializationError 
	 * @throws SecurityException 
	 */
	@Test(expected = InitializationError.class)
	public void testHasCycles() throws SecurityException, InitializationError, ClassNotFoundException, NoSuchMethodException {
		CycleDetector detector = new CycleDetector( new TestClass( WithCycles.class ) );
		detector.checkCyclesAndGetMethods();
	}

	/**
	 * Test method for {@link experimental.CycleDetector#testHasCycles()}.
	 * @throws NoSuchMethodException 
	 * @throws ClassNotFoundException 
	 * @throws InitializationError 
	 * @throws SecurityException 
	 */
	@Test(expected = InitializationError.class)
	public void testHasCyclesOverClasses() throws SecurityException, InitializationError, ClassNotFoundException, NoSuchMethodException {
		CycleDetector detector = new CycleDetector( new TestClass( WithCycleOverClasses.class ) );
		detector.checkCyclesAndGetMethods();
	}
	
	/**
	 * Test method for {@link experimental.CycleDetector#testHasNoCycles()}.
	 * @throws NoSuchMethodException 
	 * @throws ClassNotFoundException 
	 * @throws InitializationError 
	 * @throws SecurityException 
	 */
	@Test
	public void testHasNoCyclesOverClasses() throws SecurityException, InitializationError, ClassNotFoundException, NoSuchMethodException {
		CycleDetector detector = new CycleDetector( new TestClass( WithoutCycleOverClasses.class ) );
		assertEquals( 4, detector.checkCyclesAndGetMethods().size() );
	}

	private class WithoutCycles {
		@MyTest
		public void rootMethod() {

		}

		@MyTest
		@Depends( "rootMethod" )
		public void middleMethod() {

		}

		@MyTest
		@Depends( "middleMethod;rootMethod" )
		public void bottomMethod() {

		}
	}

	private class WithCycles {
		@MyTest
		public void rootMethod() {

		}

		@MyTest
		@Depends( "bottomCyclicMethod" )
		public void cyclicMethod() {

		}

		@MyTest
		@Depends( "rootMethod;cyclicMethod" )
		public void middleCyclicMethod() {

		}

		@MyTest
		@Depends( "middleCyclicMethod;rootMethod" )
		public void bottomCyclicMethod() {

		}

		@MyTest
		@Depends( "bottomCyclicMethod" )
		public void bottomBottomMethod() {

		}
	}

	private class WithCycleOverClasses {
		// with cycle over classes
		@MyTest
		public void rootMethod() {

		}

		@MyTest
		@Depends( "rootMethod;experimental.tests.B.cyclicMethod" )
		public void middleCyclicMethod() {

		}

		@MyTest
		@Depends( "middleCyclicMethod;rootMethod" )
		public void bottomCyclicMethod() {

		}

		@MyTest
		@Depends( "bottomCyclicMethod" )
		public void bottomBottomMethod() {

		}
	}
	
	private class WithoutCycleOverClasses {
		// with cycle over classes
		@MyTest
		public void rootMethod() {

		}

		@MyTest
		@Depends( "rootMethod" )
		public void middleMethod() {

		}

		@MyTest
		@Depends( "middleMethod;B.middleMethod" )
		public void bottomCyclicMethod() {

		}
	}
}