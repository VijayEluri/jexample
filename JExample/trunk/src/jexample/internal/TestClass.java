package jexample.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import jexample.JExampleRunner;

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.notification.RunNotifier;


/**
 * Java class with test methods.
 * 
 * @author Lea Haensenberger
 * @author Adrian Kuhn
 * 
 */
public class TestClass {
    
    public final Class<?> javaClass;

    private final ExampleGraph graph;
	
	
    public TestClass(Class<?> fClass, ExampleGraph graph) {
		this.javaClass = fClass;
		this.graph = graph;
	}

	/**
	 * @return a {@link List} of all {@link Method}'s annotated with {@link Test}
	 */
	public List<Method> collectTestMethods() {
		List<Method> $ = new ArrayList<Method>();
        for (Method m : javaClass.getMethods()) {
            if (m.isAnnotationPresent(Test.class)) {
                $.add(m);
            }
        }
        return $;
	}
	
	/**
	 * @return the {@link Constructor} of <code>fClass</code>
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	public Constructor<?> getConstructor() throws SecurityException, NoSuchMethodException {
	    Constructor<?> $ = javaClass.getDeclaredConstructor();
	    $.setAccessible(true);
	    return $;
	}

    public Description getDescription() {
        Description $ = Description.createSuiteDescription(javaClass);
        for (Example tm : graph.getExamples())
            if (this.contains(tm))
                $.addChild(tm.description);
        return $;
    }    

    public void run(RunNotifier notifier) {
        graph.run(this, notifier);
    }

    public TestClass validate() {
        RunWith run = javaClass.getAnnotation(RunWith.class);
        if (run == null || run.value() != JExampleRunner.class ) {
            graph.throwNewError("Class %s is not a JExample test class, annotation @RunWith(JExampleRunner.class) missing.", this);
        }
        try {
            javaClass.getConstructor();
        }
        catch (NoSuchMethodException ex) {
            graph.addInitializationError(ex);
        }
        catch (SecurityException ex) {
            graph.addInitializationError(ex);
        }
        int len = collectTestMethods().size();
        if (len == 0) {
            graph.throwNewError("Class %s does not contain test methods.", this);
        }
        return this;
    }

    public void filter(final Filter filter) {
        graph.filter(filter);
    }
    
    public boolean contains(Example m) {
       return m.jmethod.getDeclaringClass().equals(javaClass);
    }

}
