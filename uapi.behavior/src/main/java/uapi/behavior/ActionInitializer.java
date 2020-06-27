/*
 * Copyright (c) 2020. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product.
 */

package uapi.behavior;

import uapi.common.ArgumentChecker;

import java.util.Map;

public class ActionInitializer {

    private final ActionIdentify _actionId;
    private String _label;
    private Map<Object, Object> _attributes;
    private Object[] _inputs;

    public static ActionInitializer instance(Class<?> actionType) {
        return instance(ActionIdentify.toActionId(actionType));
    }

    public static ActionInitializer instance(final ActionIdentify actionId) {
        return new ActionInitializer(actionId);
    }

    private ActionInitializer(
            final ActionIdentify actionId) {
        ArgumentChecker.required(actionId, "actionId");
        this._actionId = actionId;
    }

    public ActionIdentify actionIdentify() {
        return this._actionId;
    }

    public ActionInitializer label(final String label) {
        ArgumentChecker.required(label, "label");
        this._label = label;
        return this;
    }

    public String label() {
        return this._label;
    }

    /**
     * Set attribute for current action.
     *
     * @param   attributes
     *          The attribute for current action
     * @return  This behavior build instance
     * @throws  BehaviorException
     *          Current action does not support attribute
     */
    public ActionInitializer attributes(final Map<Object, Object> attributes) {
        ArgumentChecker.required(attributes, "attributes");
        this._attributes = attributes;
        return this;
    }

    public Map<Object, Object> attributes() {
        return this._attributes;
    }

    /**
     * Set input for current action
     *
     * @param   inputs
     *          The inputs for current action
     * @return  This behavior builder instance
     * @throws  BehaviorException
     *          Current action does not support inputs
     */
    public ActionInitializer inputs(final Object... inputs) {
        ArgumentChecker.required(inputs, "inputs");
        this._inputs = inputs;
        return this;
    }

    public Object[] inputs() {
        return this._inputs;
    }
}
