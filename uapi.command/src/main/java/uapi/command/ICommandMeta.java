/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.command;

import uapi.GeneralException;
import uapi.common.ArgumentChecker;
import uapi.common.StringHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * The implementation of this interface holds meta information of a command.
 */
public interface ICommandMeta {

    String DEFAULT_NAMESPACE    = "";

    String ROOT_PATH            = "";

    String PATH_SEPARATOR       = "/";

    static String makeId(ICommandMeta commandMeta) {
        ArgumentChecker.required(commandMeta, "commandMeta");
        Map<String, String> namedValues = new HashMap<>();
        namedValues.put("namespace", commandMeta.namespace() != null ? commandMeta.namespace() : "");
        namedValues.put("sep", ICommandMeta.PATH_SEPARATOR);
        namedValues.put("parent", commandMeta.parentPath());
        namedValues.put("sep", ICommandMeta.PATH_SEPARATOR);
        namedValues.put("name", commandMeta.name());
        return StringHelper.makeString("{namespace}{sep}{parent}{sep}{name}", namedValues);
    }

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
    default String namespace() {
        return DEFAULT_NAMESPACE;
    }

    /**
     * The the parent command path of this command.
     * If the parentPath is null means no parent command.
     *
     * @return  The parentPath command of this command
     */
    default String parentPath() {
        return ROOT_PATH;
    }

    default String[] ancestors() {
        if (hasParent()) {
            return this.parentPath().split(PATH_SEPARATOR);
        }
        return new String[0];
    }

    default boolean hasParent() {
        return ! StringHelper.isNullOrEmpty(parentPath());
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
    default IParameterMeta[] parameterMetas() {
        return new IParameterMeta[0];
    }

    /**
     * List of option meta of this command.
     *
     * @return  Return option meta list
     */
    default IOptionMeta[] optionMetas() {
        return new IOptionMeta[0];
    }

    /**
     * Create new command executor.
     *
     * @return  The new command executor
     */
    default ICommandExecutor newExecutor() {
        throw new GeneralException("The command does not provide executor - {}", id());
    }

    default String id() {
//        Map<String, String> namedValues = new HashMap<>();
//        namedValues.put("namespace", namespace() != null ? namespace() : "");
//        namedValues.put("sep", ICommandMeta.PATH_SEPARATOR);
//        namedValues.put("parent", parentPath());
//        namedValues.put("sep", ICommandMeta.PATH_SEPARATOR);
//        namedValues.put("name", name());
//        return StringHelper.makeString("{namespace}{sep}{parent}{sep}{name}", namedValues);
        return makeId(this);
    }
}
