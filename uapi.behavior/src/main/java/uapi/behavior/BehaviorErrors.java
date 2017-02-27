/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.behavior;

import uapi.ExceptionErrors;

/**
 * Error codes for behavior framework.
 */
public class BehaviorErrors extends ExceptionErrors<BehaviorException> {

    public static final int CATEGORY   = 0x0100;

    public static final int UNMATCHED_ACTION        = 1;
    public static final int NOT_ONLY_POST_ACTION    = 2;
    public static final int BEHAVIOR_ID_IS_USED     = 3;
    public static final int NO_ACTION_WITH_LABEL    = 4;
    public static final int ACTION_LABEL_IS_BIND    = 5;

    static {
        mapCodeKey(UNMATCHED_ACTION, UnmatchedAction.KEY);
        mapCodeKey(NOT_ONLY_POST_ACTION, NotOnlyPostAction.KEY);
        mapCodeKey(BEHAVIOR_ID_IS_USED, BehaviorIdIsUsed.KEY);
        mapCodeKey(NO_ACTION_WITH_LABEL, NoActionWithLabel.KEY);
        mapCodeKey(ACTION_LABEL_IS_BIND, ActionLabelIsBind.KEY);
    }

    @Override
    protected String getPropertiesFile(BehaviorException exception) {
        if (exception.category() == CATEGORY) {
            return "/behaviorErrors.properties";
        }
        return null;
    }

    /**
     * Error string template:
     *      Unmatched output type {} of action {} to input type {} of action {}
     */
    public static final class UnmatchedAction extends IndexedVariables<UnmatchedAction> {

        private static final String KEY = "UnmatchedAction";

        private String _outputType;
        private String _inputType;
        private String _outputAction;
        private String _inputAction;

        public UnmatchedAction outputType(String outputType) {
            this._outputType = outputType;
            return this;
        }

        public UnmatchedAction inputType(String inputType) {
            this._inputType = inputType;
            return this;
        }

        public UnmatchedAction outputAction(String outputAction) {
            this._outputAction = outputAction;
            return this;
        }

        public UnmatchedAction inputAction(String inputAction) {
            this._inputAction = inputAction;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._outputType, this._inputType, this._outputAction , this._inputAction };
        }
    }

    /**
     * Error string template:
     *      Found zero or more post action when handler data without attributes - {}
     */
    public static final class NotOnlyPostAction extends IndexedVariables<NotOnlyPostAction> {

        private static final String KEY = "NotOnlyPostAction";

        private String _actionName;

        public NotOnlyPostAction actionName(String actionName) {
            this._actionName = actionName;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._actionName };
        }
    }

    /**
     * Error string template:
     *      The behavior is used - {}
     */
    public static final class BehaviorIdIsUsed extends IndexedVariables<BehaviorIdIsUsed> {

        private static final String KEY = "BehaviorIdIsUsed";

        private String _behaviorId;

        public BehaviorIdIsUsed behaviorId(String behaviorId) {
            this._behaviorId = behaviorId;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._behaviorId };
        }
    }

    /**
     * String template:
     *      No action is labeled - {}
     */
    public static final class NoActionWithLabel extends IndexedVariables<NoActionWithLabel> {

        private static final String KEY = "NoActonWithLabel";

        private String _label;

        public NoActionWithLabel label(String label) {
            this._label = label;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._label };
        }
    }

    /**
     * Class for action label is bind exception.
     * Error string template:
     *      The label [{}] has been bind to action [{}]
     */
    public static final class ActionLabelIsBind extends IndexedVariables<ActionLabelIsBind> {

        private static final String KEY = "ActionLabelIsBind";

        private String _label;
        private Object _actionId;

        public ActionLabelIsBind label(String label) {
            this._label = label;
            return this;
        }

        public ActionLabelIsBind actionId(Object actionId) {
            this._actionId = actionId;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._label, this._actionId };
        }
    }
}
