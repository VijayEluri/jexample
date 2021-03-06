/**
 * 
 */
package ch.unibe.jexample.internal;

import static ch.unibe.jexample.internal.ExampleState.GREEN;
import static ch.unibe.jexample.internal.ExampleState.RED;
import static ch.unibe.jexample.internal.ExampleState.WHITE;

import java.lang.reflect.InvocationTargetException;

import org.junit.Ignore;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

/**
 * Runs an example, reports to JUnit and returns test color.
 * 
 * @author Adrian Kuhn
 * 
 */
class ExampleRunner {

    private final Example eg;
    private final RunNotifier notifier;

    public ExampleRunner(Example example, RunNotifier notifier) {
        this.eg = example;
        this.notifier = notifier;
    }

    private ExampleState fail(Throwable e) {
        notifier.fireTestFailure(new Failure(eg.description, e));
        return RED;
    }

    private ExampleState failExpectedException() {
        return fail(new AssertionError("Expected exception: " + eg.expectedException.getName()));
    }

    private ExampleState failUnexpectedException(Throwable ex) {
        String message = "Unexpected exception, expected<" + eg.expectedException.getName() + "> but was<"
                + ex.getClass().getName() + ">";
        return fail(new Exception(message, ex));
    }

    private void finished() {
        notifier.fireTestFinished(eg.description);
    }

    private ExampleState ignore() {
        notifier.fireTestIgnored(eg.description);
        return WHITE;
    }

    private boolean isUnexpected(Throwable exception) {
        return !eg.expectedException.isAssignableFrom(exception.getClass());
    }

    /**
     * Runs the example and returns test color.
     * 
     * @return {@link GREEN} if the example was successful,<br> {@link RED} if the
     *         example is invalid or failed, and<br> {@link WHITE} if any of the
     *         dependencies failed.
     */
    public ExampleState run() {
        if (eg.errors.size() > 0) return abort(eg.errors);
        if (toBeIgnored()) return ignore();
        if (!runDependencies()) return ignore();
        started();
        try {
            return runExample();
        } finally {
            finished();
        }
    }

    private ExampleState abort(Throwable ex) {
        notifier.testAborted(eg.description, ex);
        return RED;
    }

    /**
     * Runs dependencies and returns success. Dependencies are runned in order
     * of declaration.
     * 
     * @return If all dependencies succeed return <code>true</code>.<br>
     *         If any fails, abort and return <code>false</code>.
     */
    private boolean runDependencies() {
        for (Example each: eg.providers) {
            each.run(notifier);
            if (!each.wasSuccessful()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Runs the bare example and handles exceptions.
     * 
     * @return {@link GREEN} if the example runs without exception (or throws an
     *         expected exception).<br> {@link RED} if the example fails (or if an
     *         exception was expected, does not throw the expected exception).
     */
    private ExampleState runExample() {
        try {
            eg.bareInvoke();
            if (eg.expectedException != null) return failExpectedException();
            return success();
        } catch (InvocationTargetException e) {
            Throwable actual = e.getTargetException();
            if (eg.expectedException == null) return fail(actual);
            if (isUnexpected(actual)) return failUnexpectedException(actual);
            return success();
        } catch (Throwable e) {
            return fail(e);
        }
    }

    private void started() {
        notifier.fireTestStarted(eg.description);
    }

    private ExampleState success() {
        return GREEN;
    }

    private boolean toBeIgnored() {
        return eg.method.getAnnotation(Ignore.class) != null;
    }

}