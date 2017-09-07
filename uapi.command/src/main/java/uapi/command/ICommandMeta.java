/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.command;

import uapi.common.StringHelper;

/**
 * The implementation of this interface holds meta information of a command.
 */
public interface ICommandMeta {

    /**
     * The the parent command of this command.
     * If the parent is null means no parent.
     *
     * @return  The parent command of this command
     */
    String parent();

    /**
     * The name of this command.
     *
     * @return  Name of this command
     */
    String name();

    /**
     * The namespace of this command.
     *
     * @return  Namespace of this command
     */
    String namespace();

    /**
     * The command identify which is consist by namespace and name in default.
     *
     * @return  The identify of the command
     */
    default String commandId() {
        return StringHelper.makeString("{}.{}", namespace(), name());
    }

    default String parentId() {
        if (hasParent()) {
            return StringHelper.makeString("{}.{}", namespace(), parent());
        } else {
            return null;
        }
    }

    default boolean hasParent() {
        return parent() != null;
    }

    /**
     * Description of this command.
     *
     * @return  Description of this command
     */
    String description();

    /**
     * List of parameter meta of this command.
     *
     * @return  Return parameter meta list
     */
    IParameterMeta[] parameterMetas();

    /**
     * List of option meta of this command.
     *
     * @return  Return option meta list
     */
    IOptionMeta[] optionMetas();

    /**
     * Create new command executor.
     *
     * @return  The new command executor
     */
    ICommandExecutor newExecutor();
}
