/*
 * Copyright (c) 2019. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product.
 */

package uapi.behavior;

public interface Functionals {

    /**
     * It is invoked when Behavior execution is failed
     */
    @FunctionalInterface
    interface BehaviorFailureAction {
        BehaviorEvent accept(
                BehaviorFailure failure,
                IExecutionContext executionContext
        );
    }

    /**
     * It will be invoked when Behavior execution is successful
     */
    @FunctionalInterface
    interface BehaviorSuccessAction {
        BehaviorEvent accept(
                BehaviorSuccess success,
                IExecutionContext executionContext
        );
    }

    /**
     * The interface is used add anonymous action to Behavior
     */
    interface AnonymousCall {
        void accept(IExecutionContext executionContext) throws Exception;
    }
}
