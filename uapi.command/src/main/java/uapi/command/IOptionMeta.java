/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.command;

/**
 * The meta information of command option
 */
public interface IOptionMeta {

    String SHORT_PREFIX = "-";
    String LONG_PREFIX  = "--";

    /**
     * The option name also known as long option name.
     *
     * @return  The option name
     */
    String name();

    /**
     * The short name of the option.
     *
     * @return  The short option name
     */
    char shortName();

    /**
     * The type of the option.
     *
     * @return  The option type
     */
    OptionType type();

    /**
     * The argument of the option if the option is string type.
     * It should be null if the option type is non-string type.
     *
     * @return  The option argument
     */
    String argument();

    /**
     * Description of the option.
     *
     * @return  Description of the option
     */
    String description();
}
