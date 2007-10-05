/**
 * 
 */
package experimental.demo;

import static org.junit.Assert.assertTrue;
import org.junit.runner.RunWith;

import experimental.ComposedTestRunner;
import extension.annotations.Depends;
import extension.annotations.MyTest;


/**
 * @author Lea Haensenberger (lhaensenberger at students.unibe.ch)
 */
@RunWith(ComposedTestRunner.class)
public class SomeDependencies {

	public SomeDependencies(){}
	
	@MyTest
	@Depends("SomeMoreDependencies.middleMethodMoreDeps")
	public void middleMethodDeps(){
		System.out.println("I'm run in the middle, I'm in SomeDependencies.");
	}
}
