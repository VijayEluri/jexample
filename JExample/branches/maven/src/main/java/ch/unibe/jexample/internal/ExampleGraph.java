package ch.unibe.jexample.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.internal.runners.CompositeRunner;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.notification.RunNotifier;

import ch.unibe.jexample.JExample;

/**
 * Validates, describes and runs all JExample tests. Implemented as a singleton
 * in order to persist results between runs of many classes. JUnit's eclipse
 * plug-in runs all classes of a package in one bunch, but restarts JUnit for
 * each new launch. Thus, this singleton should work for Eclipse.
 * 
 * @author Lea Haensenberger
 * @author Adrian Kuhn
 * 
 */
public class ExampleGraph {

    private static ExampleGraph GRAPH;
    private Map<MethodReference,Example> examples;
    private Map<Class<?>,ExampleClass> classes;
    private boolean anyHasBeenRun = false;

    public ExampleGraph() {
        this.examples = new HashMap<MethodReference,Example>();
        this.classes = new HashMap<Class<?>,ExampleClass>();
    }

    public static ExampleGraph instance() {
        return GRAPH == null ? GRAPH = new ExampleGraph() : GRAPH;
    }

    protected ExampleClass newExampleClass(Class<?> jclass) {
        ExampleClass egClass = classes.get(jclass);
        if (egClass == null) {
            egClass = new ExampleClass(jclass, this);
            classes.put(jclass, egClass);
        }
        return egClass;
    }

    protected Example newExample(MethodReference method) {
        Example e = examples.get(method);
        if (e != null) return e;
        e = new Example(method, newExampleClass(method.jclass));
        examples.put(method, e);
        for (MethodReference m: e.collectDependencies()) {
            Example d = newExample(m);
            e.providers.add(d);
            e.validateCycle();
        }
        e.validate();
        return e;
    }

    public ExampleClass add(Class<?> jclass) throws JExampleError {
        if (anyHasBeenRun) throw new RuntimeException("Cannot add test to running system.");
        ExampleClass egClass = newExampleClass(jclass);
        egClass.validate();
        egClass.initializeExamples();
        return egClass;
    }

    public void run(ExampleClass testClass, RunNotifier notifier) {
        anyHasBeenRun = true;
        for (Example e: this.getExamples()) {
            if (testClass.contains(e)) {
                e.run(notifier);
            }
        }
    }

    public Collection<MethodReference> getMethods() {
        return this.examples.keySet();
    }

    public Collection<Example> getExamples() {
        return this.examples.values();
    }

    public Result runJExample() {
        CompositeRunner runner = new CompositeRunner("All");
        for (ExampleClass eg: classes.values()) {
            runner.add(new JExample(eg));
        }
        return new JUnitCore().run(runner);
    }

    public Result runJExample(Class<?>... tests) throws JExampleError {
        for (Class<?> each: tests)
            add(each);
        return runJExample();
    }

    public Example findExample(Class<?> c, String name) {
        Example found = null;
        for (Example e: getExamples()) {
            if (e.method.equals(c, name)) {
                if (found != null) throw new RuntimeException();
                found = e;
            }
        }
        return found;
    }

    public void filter(Filter filter) {
        Iterator<Example> it = examples.values().iterator();
        while (it.hasNext()) {
            if (!filter.shouldRun(it.next().description)) {
                it.remove();
            }
        }
        // copy list of values to avoid concurrent modification
        Collection<Example> copy = new ArrayList<Example>(examples.values());
        for (Example e: copy) {
            for (Example dependency: e.providers.transitiveClosure()) {
                examples.put(dependency.method, dependency);
            }
        }
    }

    public Example findExample(MethodReference ref) {
        for (Example eg: getExamples()) {
            if (ref.equals(eg.method)) return eg;
        }
        return null;
    }

}
