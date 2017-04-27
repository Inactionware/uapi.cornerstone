package uapi.log;

import uapi.common.StringHelper;

/**
 * The logger which only output message to console
 */
public class ConsoleLogger implements ILogger {

    @Override
    public void trace(String message, Object... parameters) {
        System.out.println(StringHelper.makeString(message, parameters));
    }

    @Override
    public void debug(String message, Object... parameters) {
        System.out.println(StringHelper.makeString(message, parameters));
    }

    @Override
    public void info(String message, Object... parameters) {
        System.out.println(StringHelper.makeString(message, parameters));
    }

    @Override
    public void warn(String message, Object... parameters) {
        System.err.println(StringHelper.makeString(message, parameters));
    }

    @Override
    public void warn(Throwable t) {
        t.printStackTrace(System.err);
    }

    @Override
    public void warn(Throwable t, String message, Object... parameters) {
        System.err.println(StringHelper.makeString(message, parameters));
        t.printStackTrace(System.err);
    }

    @Override
    public void error(String message, Object... parameters) {
        System.err.println(StringHelper.makeString(message, parameters));
    }

    @Override
    public void error(Throwable t) {
        t.printStackTrace(System.err);
    }

    @Override
    public void error(Throwable t, String message, Object... parameters) {
        System.err.println(StringHelper.makeString(message, parameters));
        t.printStackTrace(System.err);
    }
}
