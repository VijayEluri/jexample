/**
 * 
 */
package extension.tests;


import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import extension.DependencyParser;
import extension.TestClass;
import extension.annotations.MyTest;

/**
 * @author Lea Haensenberger (lhaensenberger at students.unibe.ch)
 */
public class DependencyParserTest {

	private DependencyParser parser;
	private TestClass myClass;
	private Method annotatedMethod;
	private Method annotatedMethodString;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.myClass = new TestClass(this.getClass());
		parser = new DependencyParser(myClass);
		annotatedMethod = this.getClass().getMethod( "annotatedMethod");
	}
	
	@Test
	public void testGetDependencies() throws SecurityException, ClassNotFoundException, NoSuchMethodException{
		List<Method> methods = this.parser.getDependencies("annotatedMethod", annotatedMethod);
		assertEquals(1,methods.size());
		
		methods = this.parser.getDependencies("annotatedMethod(java.lang.String)", annotatedMethod);
		assertEquals(1,methods.size());
		
		methods = this.parser.getDependencies("annotatedMethod;annotatedMethod(java.lang.String)", annotatedMethod);
		assertEquals(2,methods.size());

		methods = this.parser.getDependencies("annotatedMethod(int)", annotatedMethod);
		assertEquals(1,methods.size());
	}
	
	@Test
	public void testExternalDeps() throws SecurityException, ClassNotFoundException, NoSuchMethodException{
        List<Method> methods = this.parser.getDependencies("B.otherTest", annotatedMethod);
        assertEquals(1,methods.size());
	}
	
	@Test(expected = ClassNotFoundException.class)
	public void testExtDepWithoutPackageNotFound() throws SecurityException, ClassNotFoundException, NoSuchMethodException{
        List<Method> methods = this.parser.getDependencies("TestClass.getJavaClass", annotatedMethod);
        assertEquals(1,methods.size());
	}
	
	@Test
	public void testExtDepWithPackageFound() throws SecurityException, ClassNotFoundException, NoSuchMethodException{
        List<Method> methods = this.parser.getDependencies("extension.TestClass.getJavaClass", annotatedMethod);
        assertEquals(1,methods.size());
	}
	
	@Test
	public void testGetDependenciesOnBefore() throws SecurityException, NoSuchMethodException {
		List<Method> methods = this.parser.getDependencies( this.getClass().getMethod( "annotatedMethod", String.class ) );
		assertEquals( 1, methods.size() );
		assertEquals( this.getClass().getMethod( "annotatedMethod" ), methods.get( 0 ) );
	}
	
	@MyTest
	public void annotatedMethod(){
		
	}
	
	@MyTest
	public void annotatedMethod(String string){
		
	}
	
	@MyTest
	public void annotatedMethod(int i){
		
	}

}
