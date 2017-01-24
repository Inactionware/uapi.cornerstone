/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.log;

/**
 * The logger used to output information to console, file...
 */
public interface ILogger {

    /**
     * Output information by trace level
     *
     * @param   message
     *          The message template
     * @param   parameters
     *          The parameters which used in message template
     */
    void trace(String message, Object... parameters);

    /**
     * Output information by debug level
     *
     * @param   message
     *          The message template
     * @param   parameters
     *          The parameters which used in message template
     */
    void debug(String message, Object... parameters);

    /**
     * Output information by info level
     *
     * @param   message
     *          The message template
     * @param   parameters
     *          The parameters which used in message template
     */
    void info(String message, Object... parameters);

    /**
     * Output information by warn level
     *
     * @param   message
     *          The message template
     * @param   parameters
     *          The parameters which used in message template
     */
    void warn(String message, Object... parameters);

    /**
     * Output information by warn level
     *
     * @param   t
     *          The throwable which will be outputted
     */
    void warn(Throwable t);

    /**
     * Output information by warn level
     *
     * @param   t
     *          The throwable which will be outputted
     * @param   message
     *          The message template
     * @param   parameters
     *          The parameters which used in message template
     */
    void warn(Throwable t, String message, Object... parameters);

    /**
     * Output information by error level
     *
     * @param   message
     *          The message template
     * @param   parameters
     *          The parameters which used in message template
     */
    void error(String message, Object... parameters);

    /**
     * Output information by error level
     *
     * @param   t
     *          The throwable which will be outputted
     */
    void error(Throwable t);

    /**
     * Output information by error level
     *
     * @param   t
     *          The throwable which will be outputted
     * @param   message
     *          The message template
     * @param   parameters
     *          The parameters which used in message template
     */
    void error(Throwable t, String message, Object... parameters);
}
