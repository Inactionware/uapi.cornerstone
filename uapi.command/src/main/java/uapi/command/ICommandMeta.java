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

import java.util.HashMap;
import java.util.Map;

/**
 * The implementation of this interface holds meta information of a command.
 */
public interface ICommandMeta {

    String PATH_SEPARATOR   = "/";

    /**
     * The the parent command path of this command.
     * If the parentPath is null means no parent command.
     *
     * @return  The parentPath command of this command
     */
    String parentPath();

    /**
     * The name of this command.
     * The name can't contains PATH_SEPARATOR
     *
     * @return  Name of this command
     */
    String name();

    /**
     * The namespace of this command.
     * The namespace can't contains PATH_SEPARATOR
     *
     * @return  Namespace of this command
     */
    String namespace();

    default String[] ancestors() {
        if (hasParent()) {
            return this.parentPath().split(PATH_SEPARATOR);
        }
        return null;
    }

    /**
     * The command identify which is consist by namespace and name in default.
     *
     * @return  The identify of the command
     */
//    default String commandId() {
//        Map<String, String> namedValues = new HashMap<>();
//        namedValues.put("namespace", namespace() != null ? namespace() : "");
//        namedValues.put("sep", PATH_SEPARATOR);
//        namedValues.put("parent", parentPath());
//        namedValues.put("sep", PATH_SEPARATOR);
//        namedValues.put("name", name());
//        return StringHelper.makeString("{namespace}{sep}{parent}{sep}{name}", namedValues);
//    }

    default boolean hasParent() {
        return parentPath() != null;
    }

    default int depth() {
        if (hasParent()) {
            return ancestors().length;
        }
        return 0;
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
