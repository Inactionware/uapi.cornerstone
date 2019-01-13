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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Error codes for behavior framework.
 */
public class BehaviorErrors extends FileBasedExceptionErrors<BehaviorException> {

    public static final int CATEGORY   = 0x0104;

    public static final int UNMATCHED_ACTION                        = 1;
    public static final int NOT_ONLY_NEXT_ACTION                    = 2;
    public static final int BEHAVIOR_ID_IS_USED                     = 3;
    public static final int NO_ACTION_WITH_LABEL                    = 4;
    public static final int ACTION_LABEL_IS_BIND                    = 5;
    public static final int EVALUATOR_IS_SET                        = 6;
    public static final int ACTION_NOT_FOUND                        = 7;
    public static final int EVALUATOR_NOT_USED                      = 8;
    public static final int NO_ACTION_IN_BEHAVIOR                   = 9;
    public static final int ACTION_IO_MISMATCH                      = 10;
    public static final int PUBLISH_UNREG_BEHAVIOR                  = 11;
    public static final int BEHAVIOR_IS_PUBLISHED                   = 12;
    public static final int INCONSISTENT_LEAF_ACTIONS               = 13;
    public static final int DUPLICATED_RESPONSIBLE_NAME             = 14;
    public static final int UNSUPPORTED_BEHAVIOR_EVENT_TYPE         = 15;
    public static final int UNSUPPORTED_INJECTED_SERVICE            = 16;
    public static final int FAILURE_ACTION_EXISTS                   = 17;
    public static final int SUCCESS_ACTION_EXISTS                   = 18;
    public static final int INTERCEPTOR_NOT_FOUND                   = 19;
    public static final int ACTION_IS_NOT_INTERCEPTOR               = 20;
    public static final int INTERCEPTOR_IO_NOT_MATCH_ACTION_INPUT   = 21;
    public static final int UNSUPPORTED_INTERCEPTIVE_INTERCEPTOR    = 22;
    public static final int RESERVED_RESULT_KEY                     = 23;
    public static final int ACTION_OUTPUT_TYPE_NOT_MATCHED          = 24;
    public static final int ACTION_OUTPUT_SET_TWICE                 = 25;
    public static final int INCOMPATIBLE_ACTION_TYPE                = 26;
    public static final int DUPLICATED_ACTION_LABEL                 = 27;
    public static final int GENERATE_ACTION_LABEL_OVER_MAX          = 28;
    public static final int INVALID_ACTION_INPUT_REF                = 29;
    public static final int REF_ACTION_NOT_EXIST_IN_BEHAVIOR        = 30;
    public static final int DUPLICATED_ACTION_OUTPUT                = 31;
    public static final int NO_OUTPUT_IN_ACTION                     = 32;
    public static final int INPUT_OUTPUT_COUNT_MISMATCH             = 33;
    public static final int INPUT_OUTPUT_TYPE_MISMATCH              = 34;
    public static final int INPUT_OBJECT_TYPE_MISMATCH              = 35;

    private static final Map<Integer, String> keyCodeMapping;

    static {
        keyCodeMapping = new ConcurrentHashMap<>();
        keyCodeMapping.put(UNMATCHED_ACTION, UnmatchedAction.KEY);
        keyCodeMapping.put(NOT_ONLY_NEXT_ACTION, NotOnlyNextAction.KEY);
        keyCodeMapping.put(BEHAVIOR_ID_IS_USED, BehaviorIdIsUsed.KEY);
        keyCodeMapping.put(NO_ACTION_WITH_LABEL, NoActionWithLabel.KEY);
        keyCodeMapping.put(ACTION_LABEL_IS_BIND, ActionLabelIsBind.KEY);
        keyCodeMapping.put(EVALUATOR_IS_SET, EvaluatorIsSet.KEY);
        keyCodeMapping.put(ACTION_NOT_FOUND, ActionNotFound.KEY);
        keyCodeMapping.put(EVALUATOR_NOT_USED, EvaluatorNotUsed.KEY);
        keyCodeMapping.put(NO_ACTION_IN_BEHAVIOR, NoActionInBehavior.KEY);
        keyCodeMapping.put(ACTION_IO_MISMATCH, ActionIOMismatch.KEY);
        keyCodeMapping.put(PUBLISH_UNREG_BEHAVIOR, PublishUnregBehavior.KEY);
        keyCodeMapping.put(BEHAVIOR_IS_PUBLISHED, BehaviorIsPublished.KEY);
        keyCodeMapping.put(INCONSISTENT_LEAF_ACTIONS, InconsistentLeafActions.KEY);
        keyCodeMapping.put(DUPLICATED_RESPONSIBLE_NAME, DuplicatedResponsibleName.KEY);
        keyCodeMapping.put(UNSUPPORTED_BEHAVIOR_EVENT_TYPE, UnsupportedBehaviorTraceEventType.KEY);
        keyCodeMapping.put(UNSUPPORTED_INJECTED_SERVICE, UnsupportedInjectedService.KEY);
        keyCodeMapping.put(FAILURE_ACTION_EXISTS, FailureActionExists.KEY);
        keyCodeMapping.put(SUCCESS_ACTION_EXISTS, SuccessActionExists.KEY);
        keyCodeMapping.put(INTERCEPTOR_NOT_FOUND, InterceptorNotFound.KEY);
        keyCodeMapping.put(ACTION_IS_NOT_INTERCEPTOR, ActionIsNotInterceptor.KEY);
        keyCodeMapping.put(INTERCEPTOR_IO_NOT_MATCH_ACTION_INPUT, InterceptorIONotMatchActionInput.KEY);
        keyCodeMapping.put(UNSUPPORTED_INTERCEPTIVE_INTERCEPTOR, UnsupportedInterceptiveInterceptor.KEY);
        keyCodeMapping.put(RESERVED_RESULT_KEY, ReservedResultKey.KEY);
        keyCodeMapping.put(ACTION_OUTPUT_TYPE_NOT_MATCHED, ActionOutputTypeNotMatched.KEY);
        keyCodeMapping.put(ACTION_OUTPUT_SET_TWICE, ActionOutputSetTwice.KEY);
        keyCodeMapping.put(INCOMPATIBLE_ACTION_TYPE, IncompatibleActionType.KEY);
        keyCodeMapping.put(DUPLICATED_ACTION_LABEL, DuplicatedActionLabel.KEY);
        keyCodeMapping.put(GENERATE_ACTION_LABEL_OVER_MAX, GenerateActionLabelOverMax.KEY);
        keyCodeMapping.put(INVALID_ACTION_INPUT_REF, InvalidActionInputRef.KEY);
        keyCodeMapping.put(REF_ACTION_NOT_EXIST_IN_BEHAVIOR, RefActionNotExistInBehavior.KEY);
        keyCodeMapping.put(DUPLICATED_ACTION_OUTPUT, DuplicatedActionOutput.KEY);
        keyCodeMapping.put(NO_OUTPUT_IN_ACTION, NoOutputInAction.KEY);
        keyCodeMapping.put(INPUT_OUTPUT_COUNT_MISMATCH, InputOutputCountMismatch.KEY);
        keyCodeMapping.put(INPUT_OUTPUT_TYPE_MISMATCH, InputOutputTypeMismatch.KEY);
        keyCodeMapping.put(INPUT_OBJECT_TYPE_MISMATCH, InputObjectTypeMismatch.KEY);
    }

    public BehaviorErrors() {
        super();
    }

    @Override
    protected String getFile(BehaviorException exception) {
        if (exception.category() == CATEGORY) {
            return "/behaviorErrors.properties";
        }
        return null;
    }

    @Override
    protected String getKey(BehaviorException e) {
        return keyCodeMapping.get(e.errorCode());
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
            return new Object[] { this._outputType, this._outputAction, this._inputType , this._inputAction };
        }
    }

    /**
     * Error string template:
     *      Found zero or more post action when handler data without attributes - {}
     */
    public static final class NotOnlyNextAction extends IndexedParameters<NotOnlyNextAction> {

        private static final String KEY = "NotOnlyNextAction";

        private ActionIdentify _actionId;
        private Object _data;

        public NotOnlyNextAction actionId(ActionIdentify actionId) {
            this._actionId = actionId;
            return this;
        }

        public NotOnlyNextAction data(Object data) {
            this._data = data;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._actionId, this._data};
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
     * Class for action label is bind cause.
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

    /**
     * Error string template:
     *      Inconsistent leaf action output type [{} vs. {}] on leaf actions [{} vs. {}]
     */
    public static final class InconsistentLeafActions extends IndexedParameters<InconsistentLeafActions> {

        private static final String KEY = "InconsistentLeafActions";

        private ActionIdentify _leafAction1;
        private ActionIdentify _leafAction2;
        private Class _leafAction1Output;
        private Class _leafAction2Output;

        public InconsistentLeafActions leafAction1(ActionIdentify actionId) {
            this._leafAction1 = actionId;
            return this;
        }

        public InconsistentLeafActions leafAction2(ActionIdentify actionId) {
            this._leafAction2 = actionId;
            return this;
        }

        public InconsistentLeafActions leafAction1Output(Class type) {
            this._leafAction1Output = type;
            return this;
        }

        public InconsistentLeafActions leafAction2Output(Class type) {
            this._leafAction2Output = type;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._leafAction1Output, this._leafAction2Output, this._leafAction1, this._leafAction2 };
        }
    }

    /**
     * Error string template:
     *      Register duplicated responsible name is denied - {}
     */
    public static final class DuplicatedResponsibleName extends IndexedParameters<DuplicatedResponsibleName> {

        private static final String KEY = "DuplicatedResponsibleName";

        private String _respName;

        public DuplicatedResponsibleName responsibleName(final String name) {
            this._respName = name;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._respName };
        }
    }

    /**
     * Error string template:
     *      Unsupported behavior trace event type - {}
     */
    public static final class UnsupportedBehaviorTraceEventType extends IndexedParameters<UnsupportedBehaviorTraceEventType> {

        private static final String KEY = "UnsupportedBehaviorTraceEventType";

        private Class _eventType;

        public UnsupportedBehaviorTraceEventType eventType(Class eventType) {
            this._eventType = eventType;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._eventType };
        }
    }

    /**
     * Error string template:
     *      The service was injected to service {} is not supported - {}
     */
    public static final class UnsupportedInjectedService extends IndexedParameters<UnsupportedInjectedService> {

        private static final String KEY = "UnsupportedInjectedService";

        private String _injectedSvc;
        private String _injectSvc;

        public UnsupportedInjectedService injectedService(String serviceId) {
            this._injectedSvc = serviceId;
            return this;
        }

        public UnsupportedInjectedService injectService(String serviceId) {
            this._injectSvc = serviceId;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._injectSvc, this._injectedSvc };
        }
    }

    /**
     * Error string template:
     *      The behavior's failure action is specified - {}
     */
    public static final class FailureActionExists extends IndexedParameters<FailureActionExists> {

        private static final String KEY = "FailureActionExists";

        private ActionIdentify _behaviorId;

        public FailureActionExists behaviorId(ActionIdentify behaviorId) {
            this._behaviorId = behaviorId;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._behaviorId };
        }
    }

    /**
     * Error string template
     *      The behavior's success action is specified - {}
     */
    public static final class SuccessActionExists extends IndexedParameters<SuccessActionExists> {

        public static final String KEY = "SuccessActionExists";

        private ActionIdentify _behaviorId;

        public SuccessActionExists behaviorId(ActionIdentify behaviorId) {
            this._behaviorId = behaviorId;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._behaviorId };
        }
    }

    /**
     * Error string template
     *      The action {} depends on interceptor {} which is not found in the repository
     */
    public static final class InterceptorNotFound extends IndexedParameters<InterceptorNotFound> {

        public static final String KEY = "InterceptorNotFound";

        private ActionIdentify _actionId;
        private ActionIdentify _interceptorId;

        public InterceptorNotFound actionId(ActionIdentify actionId) {
            this._actionId = actionId;
            return this;
        }

        public InterceptorNotFound interceptorId(ActionIdentify actionId) {
            this._interceptorId = actionId;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._actionId, this._interceptorId };
        }
    }

    /**
     * Error string template
     *      The action {} does not implement IInterceptor interface
     */
    public static final class ActionIsNotInterceptor extends IndexedParameters<ActionIsNotInterceptor> {

        public static final String KEY = "ActionIsNotInterceptor";

        private ActionIdentify _actionId;

        public ActionIsNotInterceptor actionId(ActionIdentify actionId) {
            this._actionId = actionId;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._actionId };
        }
    }

    /**
     * Error string template
     *      The IO type [{}] of interceptor {} does not match input type [{}] of action {
     */
    public static final class InterceptorIONotMatchActionInput extends IndexedParameters<InterceptorIONotMatchActionInput> {

        public static final String KEY = "InterceptorIONotMatchActionInput";

        private Class<?> _interceptorIOType;
        private Class<?> _actionInputType;
        private ActionIdentify _interceptorId;
        private ActionIdentify _actionId;

        public InterceptorIONotMatchActionInput interceptorIOType(Class<?> type) {
            this._interceptorIOType = type;
            return this;
        }

        public InterceptorIONotMatchActionInput actionInputType(Class<?> type) {
            this._actionInputType = type;
            return this;
        }

        public InterceptorIONotMatchActionInput interceptorId(ActionIdentify actionId) {
            this._interceptorId = actionId;
            return this;
        }

        public InterceptorIONotMatchActionInput actionId(ActionIdentify actionId) {
            this._actionId = actionId;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._interceptorIOType, this._interceptorId, this._actionInputType, this._actionId };
        }
    }

    /**
     * Error string template:
     *      An interceptor can not be intercepted by other interceptor - {}
     */
    public static final class UnsupportedInterceptiveInterceptor extends IndexedParameters<UnsupportedInterceptiveInterceptor> {

        public static final String KEY = "UnsupportedDependentDependency";

        private ActionIdentify _interceptorId;

        public UnsupportedInterceptiveInterceptor interceptorId(final ActionIdentify actionId) {
            this._interceptorId = actionId;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._interceptorId };
        }
    }

    /**
     * Error string template:
     *      The Action result key is reserved - {}
     */
    public static final class ReservedResultKey extends IndexedParameters<ReservedResultKey> {

        public static final String KEY = "ReservedResultKey";

        private String _key;

        public ReservedResultKey key(String key) {
            this._key = key;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._key };
        }
    }

    /**
     * Error string template:
     *      The Action output {} is not instance of type - {}
     */
    public static final class ActionOutputTypeNotMatched extends IndexedParameters<ActionOutputTypeNotMatched> {

        public static final String KEY = "ActionOutputTypeNotMatched";

        private Object _output;
        private Class<?> _type;

        public ActionOutputTypeNotMatched output(final Object output) {
            this._output = output;
            return this;
        }

        public ActionOutputTypeNotMatched type(final Class<?> type) {
            this._type = type;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._output, this._type };
        }
    }

    /**
     * Error string template:
     *      The Action output is set twice - {}
     */
    public static final class ActionOutputSetTwice extends IndexedParameters<ActionOutputSetTwice> {

        public static final String KEY = "ActionOutputSetTwice";

        private String _name;

        public ActionOutputSetTwice name(final String name) {
            this._name = name;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._name };
        }
    }

    /**
     * Error string template:
     *      The Action type is not Action or Behavior - {}
     */
    public static final class IncompatibleActionType extends IndexedParameters<IncompatibleActionType> {

        public static final String KEY = "IncompatibleActionType";

        private Class<?> _type;

        public IncompatibleActionType type(final Class<?> type) {
            this._type = type;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._type.getCanonicalName() };
        }
    }

    /**
     * Error string template:
     *      The Action label [{}] is duplicated in Behavior [{}]
     */
    public static class DuplicatedActionLabel extends IndexedParameters<DuplicatedActionLabel> {

        public  static final String KEY = "DuplicatedActionLabel";

        private String _label;
        private ActionIdentify _behaviorId;

        public DuplicatedActionLabel label(String label) {
            this._label = label;
            return this;
        }

        public DuplicatedActionLabel behaviorId(ActionIdentify id) {
            this._behaviorId = id;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._label, this._behaviorId.toString() };
        }
    }

    /**
     * Error string template:
     *      Try generate label for action [{}] count over max count [{}]
     */
    public static class GenerateActionLabelOverMax extends IndexedParameters<GenerateActionLabelOverMax> {

        public static final String KEY = "GenerateActionLabelOverMax";

        private ActionIdentify _actionId;
        private int _maxCount;

        public GenerateActionLabelOverMax actionId(final ActionIdentify actionId) {
            this._actionId = actionId;
            return this;
        }

        public GenerateActionLabelOverMax maxCount(final int maxCount) {
            this._maxCount = maxCount;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._actionId.toString(), this._maxCount };
        }
    }

    /**
     * Error string template:
     *      The action input reference is invalid - {}
     */
    public static final class InvalidActionInputRef extends IndexedParameters<InvalidActionInputRef> {

        public static final String KEY = "InvalidActionInputRef";

        private String _actionInputRef;

        public InvalidActionInputRef inputReference(String ref) {
            this._actionInputRef = ref;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._actionInputRef };
        }
    }

    /**
     * Error string template:
     *      The referenced action [{}] does not exist in behavior [{}]
     */
    public static final class RefActionNotExistInBehavior extends IndexedParameters<RefActionNotExistInBehavior> {

        public static final String KEY = "RefActionNotExistInBehavior";

        private String _actionLabel;
        private ActionIdentify _behaviorId;

        public RefActionNotExistInBehavior actionLabel(final String label) {
            this._actionLabel = label;
            return this;
        }

        public RefActionNotExistInBehavior behaviorId(final ActionIdentify id) {
            this._behaviorId = id;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._actionLabel, this._behaviorId.toString() };
        }
    }

    /**
     * Error string template:
     *      The output [{}] of action [{}] is duplicated
     */
    public static final class DuplicatedActionOutput extends IndexedParameters<DuplicatedActionOutput> {

        public static final String KEY = "DuplicatedActionOutput";

        private String _outputName;
        private ActionIdentify _actionId;

        public DuplicatedActionOutput outputName(final String name) {
            this._outputName = name;
            return this;
        }

        public DuplicatedActionOutput actionId(final ActionIdentify actionId) {
            this._actionId = actionId;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._outputName, this._actionId.toString() };
        }
    }

    /**
     * Error string template:
     *      No output named [{}] is defined in Action [{}]
     */
    public static class NoOutputInAction extends IndexedParameters<NoOutputInAction> {

        public static final String KEY = "NoOutputInAction";

        private String _outputName;
        private ActionIdentify _actionId;

        public NoOutputInAction outputName(final String name) {
            this._outputName = name;
            return this;
        }

        public NoOutputInAction actionId(final ActionIdentify actionId) {
            this._actionId = actionId;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._outputName, this._actionId.toString() };
        }
    }

    /**
     * Error string template:
     *      The output [{}:{}] does not match on input - type: {}, value: {}
     */
//    public static final class InputOutputNotMatch extends IndexedParameters<InputOutputNotMatch> {
//
//        public static final String KEY = "InputOutputNotMatch";
//
//        private ActionOutputMeta _outputMeta;
//        private Object _input;
//
//        public InputOutputNotMatch outputMeta(ActionOutputMeta outputMeta) {
//            this._outputMeta = outputMeta;
//            return this;
//        }
//
//        public InputOutputNotMatch input(final Object input) {
//            this._input = input;
//            return this;
//        }
//
//        @Override
//        public Object[] get() {
//            return new Object[] {
//                    this._outputMeta.name(),
//                    this._outputMeta.type(),
//                    this._input == null ? "null" : this._input.getClass().getCanonicalName(),
//                    this._input == null ? "null" : this._input
//            };
//        }
//    }

    /**
     * Error string template:
     *      The input count [{}] does not match that action [{}] input count [{}] on behavior [{}]
     */
    public static final class InputOutputCountMismatch extends IndexedParameters<InputOutputCountMismatch> {

        public static final String KEY = "InputOutputCountMismatch";

        private int _inputCount;
        private ActionIdentify _actionId;
        private int _actionInputCount;
        private ActionIdentify _behaviorId;

        public InputOutputCountMismatch inputCount(final int count) {
            this._inputCount = count;
            return this;
        }

        public InputOutputCountMismatch actionId(final ActionIdentify actionId) {
            this._actionId = actionId;
            return this;
        }

        public InputOutputCountMismatch actionInputCount(final int count) {
            this._actionInputCount = count;
            return this;
        }

        public InputOutputCountMismatch behaviorId(final ActionIdentify id) {
            this._behaviorId = id;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._inputCount, this._actionId, this._actionInputCount, this._behaviorId };
        }
    }

    /**
     * Error string template:
     *      The action [{}] output [type: {}, name:{}] can't assign to action [{}] to input [type: {}]
     */
    public static final class InputOutputTypeMismatch extends IndexedParameters<InputObjectTypeMismatch> {

        public static final String KEY = "InputOutputTypeMismatch";

        private ActionIdentify _outActionId;
        private Class<?> _outType;
        private String _outName;
        private ActionIdentify _inActionId;
        private Class<?> _inType;

        public InputOutputTypeMismatch outputActionId(final ActionIdentify id) {
            this._outActionId = id;
            return this;
        }

        public InputOutputTypeMismatch outputType(final Class<?> type) {
            this._outType = type;
            return this;
        }

        public InputOutputTypeMismatch outputName(final String name) {
            this._outName = name;
            return this;
        }

        public InputOutputTypeMismatch inputActionId(final ActionIdentify id) {
            this._inActionId = id;
            return this;
        }

        public InputOutputTypeMismatch inputType(final Class<?> type) {
            this._inType = type;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._outActionId, this._outType, this._outName, this._inActionId, this._inType };
        }
    }

    /**
     * Error string template:
     *      The input object [type: {}, value: {}] can't assign to action [{}] on input [type: {}]
     */
    public static final class InputObjectTypeMismatch extends IndexedParameters<InputObjectTypeMismatch> {

        public static final String KEY = "InputObjectTypeMismatch";

        private Object _inputObj;
        private Class<?> _inputObjType;
        private ActionIdentify _actionId;
        private Class<?> _actionInType;

        public InputObjectTypeMismatch inputObject(final Object obj) {
            this._inputObj = obj;
            return this;
        }

        public InputObjectTypeMismatch inputObjectType(final Class<?> objType) {
            this._inputObjType = objType;
            return this;
        }

        public InputObjectTypeMismatch actionId(final ActionIdentify actionId) {
            this._actionId = actionId;
            return this;
        }

        public InputObjectTypeMismatch actionInputType(final Class<?> type) {
            this._actionInType = type;
            return this;
        }

        @Override
        public Object[] get() {
            return new Object[] { this._inputObjType, this._inputObj, this._actionId, this._actionInType };
        }
    }
}
