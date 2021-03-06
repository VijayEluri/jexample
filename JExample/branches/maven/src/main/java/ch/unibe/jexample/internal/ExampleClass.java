package ch.unibe.jexample.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.notification.RunNotifier;

import ch.unibe.jexample.JExample;
import ch.unibe.jexample.internal.JExampleError.Kind;

/**
 * Java class with example methods.
 * 
 * @author Lea Haensenberger
 * @author Adrian Kuhn
 * 
 */
public class ExampleClass {

    public final Class<?> jclass;

    private final JExampleError errors;
    private final ExampleGraph graph;
    private boolean beforesRunned = false;

    public ExampleClass(Class<?> jclass, ExampleGraph graph) {
        this.errors = new JExampleError();
        this.graph = graph;
        this.jclass = jclass;
    }

    public Collection<MethodReference> collectTestMethods() {
        return MethodReference.all(jclass, Test.class);
    }

    public Description getDescription() {
        Description description = Description.createSuiteDescription(jclass);
        for (Example tm: graph.getExamples())
            if (this.contains(tm)) description.addChild(tm.description);
        return description;
    }

    public void run(RunNotifier notifier) {
        graph.run(this, notifier);
    }

    public void validate() throws JExampleError {
        RunWith run = (RunWith) jclass.getAnnotation(RunWith.class);
        if (run == null || run.value() != JExample.class) {
            errors.add(Kind.MISSING_RUNWITH_ANNOTATION,
                    "Class %s is not a JExample test class, annotation @RunWith(JExample.class) missing.", this);
        }
        try {
            Util.getConstructor(jclass);
        } catch (NoSuchMethodException ex) {
            errors.add(Kind.MISSING_CONSTRUCTOR, ex);
        } catch (SecurityException ex) {
            errors.add(Kind.MISSING_CONSTRUCTOR, ex);
        }
        if (collectTestMethods().isEmpty()) {
            errors.add(Kind.NO_EXAMPLES_FOUND, "Test class must contain test methods.");
        }
        if (!errors.isEmpty()) throw errors;
    }

    public void filter(final Filter filter) {
        graph.filter(filter);
    }

    public boolean contains(Example m) {
        return m.method.jclass.equals(jclass);
    }

    public void initializeExamples() {
        for (MethodReference m: collectTestMethods()) {
            graph.newExample(m);
        }
    }

    public void runBeforeClassBefores() {
        if (beforesRunned) return;
        for (Method m: jclass.getMethods()) {
            if (m.isAnnotationPresent(BeforeClass.class)) {
                try {
                    m.invoke(null);
                } catch (IllegalArgumentException ex) {
                    throw new RuntimeException(ex);
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                } catch (InvocationTargetException ex) {
                }
            }
        }
        beforesRunned = true;
    }

}
