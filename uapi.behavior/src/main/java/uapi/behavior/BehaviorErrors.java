/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.behavior;

import uapi.exception.FileBasedExceptionErrors;
import uapi.exception.IndexedParameters;

/**
 * Error codes for behavior framework.
 */
public class BehaviorErrors extends FileBasedExceptionErrors<BehaviorException> {

    public static final int CATEGORY   = 0x0100;

    public static final int UNMATCHED_ACTION        = 1;
    public static final int NOT_ONLY_NEXT_ACTION    = 2;
    public static final int BEHAVIOR_ID_IS_USED     = 3;
    public static final int NO_ACTION_WITH_LABEL    = 4;
    public static final int ACTION_LABEL_IS_BIND    = 5;
    public static final int EVALUATOR_IS_SET        = 6;
    public static final int ACTION_NOT_FOUND        = 7;
    public static final int EVALUATOR_NOT_USED      = 8;
    public static final int NO_ACTION_IN_BEHAVIOR   = 9;
    public static final int ACTION_IO_MISMATCH      = 10;
    public static final int PUBLISH_UNREG_BEHAVIOR  = 11;
    public static final int BEHAVIOR_IS_PUBLISHED   = 12;

    static {
        mapCodeKey(UNMATCHED_ACTION, UnmatchedAction.KEY);
        mapCodeKey(NOT_ONLY_NEXT_ACTION, NotOnlyNextAction.KEY);
        mapCodeKey(BEHAVIOR_ID_IS_USED, BehaviorIdIsUsed.KEY);
        mapCodeKey(NO_ACTION_WITH_LABEL, NoActionWithLabel.KEY);
        mapCodeKey(ACTION_LABEL_IS_BIND, ActionLabelIsBind.KEY);
        mapCodeKey(EVALUATOR_IS_SET, EvaluatorIsSet.KEY);
        mapCodeKey(ACTION_NOT_FOUND, ActionNotFound.KEY);
        mapCodeKey(EVALUATOR_NOT_USED, EvaluatorNotUsed.KEY);
        mapCodeKey(NO_ACTION_IN_BEHAVIOR, NoActionInBehavior.KEY);
        mapCodeKey(ACTION_IO_MISMATCH, ActionIOMismatch.KEY);
        mapCodeKey(PUBLISH_UNREG_BEHAVIOR, PublishUnregBehavior.KEY);
        mapCodeKey(BEHAVIOR_IS_PUBLISHED, BehaviorIsPublished.KEY);
    }

    @Override
    protected String getFile(BehaviorException exception) {
        if (exception.category() == CATEGORY) {
            return "/behaviorErrors.properties";
        }
        return null;
    }

    /**
     * Error string template:
     *      Unmatched output type {} of action {} to input type {} of action {}
     */
    public static final class UnmatchedAction extends IndexedParameters<UnmatchedAction> {

        private static final String KEY = "UnmatchedAction";

        private String _outputType;
        private String _inputType;
        private ActionIdentify _outputAction;
        private ActionIdentify _inputAction;

        public UnmatchedAction outputType(String outputType) {
            this._outputType = outputType;
            return this;
        }

        public UnmatchedAction inputType(String inputType) {
            this._inputType = inputType;
            return this;
        }

        public UnmatchedAction outputAction(ActionIdentify actionId) {
            this._outputAction = actionId;
            return this;
        }

        public UnmatchedAction inputAction(ActionIdentify actionId) {
            this._inputAction = actionId;
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
    public static final class NotOnlyNextAction extends IndexedParameters<NotOnlyNextAction> {

        private static final String KEY = "NotOnlyNextAction";

        private ActionIdentify _actionId;

        public NotOnlyNextAction actionId(ActionIdentify actionId) {
            this._actionId = actionId;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._actionId};
        }
    }

    /**
     * Error string template:
     *      The behavior is used - {}
     */
    public static final class BehaviorIdIsUsed extends IndexedParameters<BehaviorIdIsUsed> {

        private static final String KEY = "BehaviorIdIsUsed";

        private ActionIdentify _behaviorId;

        public BehaviorIdIsUsed behaviorId(ActionIdentify behaviorId) {
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
    public static final class NoActionWithLabel extends IndexedParameters<NoActionWithLabel> {

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
    public static final class ActionLabelIsBind extends IndexedParameters<ActionLabelIsBind> {

        private static final String KEY = "ActionLabelIsBind";

        private String _label;
        private ActionIdentify _actionId;

        public ActionLabelIsBind label(String label) {
            this._label = label;
            return this;
        }

        public ActionLabelIsBind actionId(ActionIdentify actionId) {
            this._actionId = actionId;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._label, this._actionId };
        }
    }

    /**
     * Error string template:
     *      The evaluator is set for action - {}
     */
    public static final class EvaluatorIsSet extends IndexedParameters<EvaluatorIsSet> {

        private static final String KEY = "EvaluatorIsSet";

        private ActionIdentify _actionId;

        public EvaluatorIsSet actionId(ActionIdentify actionId) {
            this._actionId = actionId;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._actionId };
        }
    }

    /**
     * Error string template:
     *      There is no action named - {}
     */
    public static final class ActionNotFound extends IndexedParameters<ActionNotFound> {

        private static final String KEY = "ActionNotFound";

        private ActionIdentify _actionId;

        public ActionNotFound actionId(ActionIdentify actionId) {
            this._actionId = actionId;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._actionId};
        }
    }

    /**
     * Error string template:
     *      The evaluator is set but it does not used
     */
    public static final class EvaluatorNotUsed extends IndexedParameters<EvaluatorNotUsed> {

        private static final String KEY = "EvaluatorNotUsed";
    }

    /**
     * Error string template:
     *      The behavior builder has no action defined - {}
     */
    public static final class NoActionInBehavior extends IndexedParameters<NoActionInBehavior> {

        private static final String KEY = "NoActionInBehavior";

        private ActionIdentify _behaviorId;

        public NoActionInBehavior behaviorId(ActionIdentify behaviorId) {
            this._behaviorId = behaviorId;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._behaviorId };
        }
    }

    /**
     * Error string template:
     *      Incorrect output/input type [{} vs. {}] between actions - {} and {}
     */
    public static final class ActionIOMismatch extends IndexedParameters<ActionIOMismatch> {

        private static final String KEY = "ActionIOMismatch";

        private Class _outputType;
        private Class _inputType;
        private ActionIdentify _outputAction;
        private ActionIdentify _inputAction;

        public ActionIOMismatch outputType(final Class outputType) {
            this._outputType = outputType;
            return this;
        }

        public ActionIOMismatch inputType(final Class inputType) {
            this._inputType = inputType;
            return this;
        }

        public ActionIOMismatch outputAction(final ActionIdentify outputAction) {
            this._outputAction = outputAction;
            return this;
        }

        public ActionIOMismatch inputAction(final ActionIdentify inputAction) {
            this._inputAction = inputAction;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._outputType, this._inputType, this._outputAction, this._inputAction };
        }
    }

    /**
     * Error string template
     *      Can't publish behavior - {} in - {}, the behavior is unregistered
     */
    public static final class PublishUnregBehavior extends IndexedParameters<PublishUnregBehavior> {

        private static final String KEY = "PublishUnregBehavior";

        private ActionIdentify _behaviorId;
        private String _responsibleName;

        public PublishUnregBehavior behaviorId(ActionIdentify behaviorId) {
            this._behaviorId = behaviorId;
            return this;
        }

        public PublishUnregBehavior responsibleName(String responsibleName) {
            this._responsibleName = responsibleName;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._behaviorId, this._responsibleName };
        }
    }

    /**
     * Error string template:
     *      The behavior - {} in {} is published
     */
    public static final class BehaviorIsPublished extends IndexedParameters<BehaviorIsPublished> {

        private static final String KEY = "BehaviorIsPublished";

        private ActionIdentify _behaviorId;
        private String _responsibleName;

        public BehaviorIsPublished behaviorId(ActionIdentify behaviorId) {
            this._behaviorId = behaviorId;
            return this;
        }

        public BehaviorIsPublished responsibleName(String responsibleName) {
            this._responsibleName = responsibleName;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._behaviorId, this._responsibleName };
        }
    }
}
