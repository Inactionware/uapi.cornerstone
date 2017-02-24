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
import uapi.common.MapHelper;

import java.util.Map;

/**
 * Error codes for behavior framework.
 */
public class BehaviorErrors extends ExceptionErrors<BehaviorException> {

    public static final int CATEGORY   = 0x0100;

    public static final int BEHAVIOR_NOT_FOUND  = 1;
    public static final int UNMATCHED_ACTION    = 2;

    static {
        mapCodeKey(BEHAVIOR_NOT_FOUND, "BehaviorNotFound");
        mapCodeKey(UNMATCHED_ACTION, "UnmatchedAction");
    }

    @Override
    protected String getPropertiesFile(BehaviorException exception) {
        if (exception.category() == CATEGORY) {
            return "behaviorErrors.properties";
        }
        return null;
    }

    public static final class UnmatchedActionVariableBuilder extends NamedVariableBuilder {

        private static final String OUTPUT_TYPE     = "outputType";
        private static final String INPUT_TYPE      = "inputType";
        private static final String OUTPUT_ACTION   = "outputAction";
        private static final String INPUT_ACTION    = "inputAction";

        private String _outputType;
        private String _inputType;
        private String _outputAction;
        private String _inputAction;

        public UnmatchedActionVariableBuilder outputType(String outputType) {
            this._outputType = outputType;
            return this;
        }

        public UnmatchedActionVariableBuilder inputType(String inputType) {
            this._inputType = inputType;
            return this;
        }

        public UnmatchedActionVariableBuilder outputAction(String outputAction) {
            this._outputAction = outputAction;
            return this;
        }

        public UnmatchedActionVariableBuilder intputAction(String inputAction) {
            this._inputAction = inputAction;
            return this;
        }

        @Override
        public Map build() {
            return MapHelper.newMap()
                    .put(OUTPUT_TYPE, this._outputType)
                    .put(INPUT_TYPE, this._inputType)
                    .put(OUTPUT_ACTION, this._outputAction)
                    .put(INPUT_ACTION, this._inputAction)
                    .get();
        }
    }
}
