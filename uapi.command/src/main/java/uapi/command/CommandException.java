/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.command;

import uapi.exception.ExceptionBuilder;
import uapi.exception.ParameterizedException;

/**
 * The root exception for command framework
 */
public class CommandException extends ParameterizedException {

    public static CommandExceptionBuilder builder() {
        return new CommandExceptionBuilder();
    }

    protected CommandException(ExceptionBuilder builder) {
        super(builder);
    }

    public static final class CommandExceptionBuilder
            extends ExceptionBuilder<CommandException, CommandExceptionBuilder> {

        private CommandExceptionBuilder() {
            super(CommandErrors.CATEGORY, new CommandErrors());
        }

        @Override
        protected CommandException createInstance() {
            return new CommandException(this);
        }
    }
}
