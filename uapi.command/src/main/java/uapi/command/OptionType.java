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
 * The command option type.
 */
public enum OptionType {

    /**
     * Boolean option means the option is a switch or on-off
     */
    Boolean,

    /**
     * String option means the option accept a string parameter
     */
    String
}
