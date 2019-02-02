/*
 * Copyright (c) 2019. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product.
 */

package uapi.behavior.internal;

import uapi.behavior.*;
import uapi.common.Functionals;
import uapi.common.Repository;
import uapi.rx.Looper;

public class InterceptedActionHolder extends ActionHolder {

    private final IInterceptor[] _interceptors;

    InterceptedActionHolder(
            final Repository<ActionIdentify, IAction> actionRepo,
            final IAction action,
            final String label,
            final ActionHolder previousAction,
            final Behavior behavior,
            final Functionals.Evaluator evaluator, Object... inputs) {
        super(action, label, previousAction, behavior, evaluator, inputs);

        if (action instanceof IIntercepted) {
            IIntercepted intercepted = (IIntercepted) action;
            ActionIdentify[] interceptorIds = intercepted.by();
            this._interceptors = Looper.on(interceptorIds).map(interceptorId -> {
                IAction interceptor;
                interceptor = actionRepo.get(interceptorId);
                if (interceptor == null) {
                    throw BehaviorException.builder()
                            .errorCode(BehaviorErrors.INTERCEPTOR_NOT_FOUND)
                            .variables(new BehaviorErrors.InterceptorNotFound()
                                    .actionId(action.getId())
                                    .interceptorId(interceptorId))
                            .build();
                }
                if (!(interceptor instanceof IInterceptor)) {
                    throw BehaviorException.builder()
                            .errorCode(BehaviorErrors.ACTION_IS_NOT_INTERCEPTOR)
                            .variables(new BehaviorErrors.ActionIsNotInterceptor()
                                    .actionId(interceptorId))
                            .build();
                }
                return (IInterceptor) interceptor;
            }).toArray();
        } else {
            this._interceptors = new IInterceptor[0];
        }
    }

    @Override
    void execute(
            final Object[] inputs,
            final ActionOutput[] outputs,
            final IExecutionContext context
    ) throws Exception {
        ActionOutput[] interceptorOuts = new ActionOutput[0];
        Looper.on(this._interceptors).foreach(interceptor -> {
            interceptor.process(inputs, interceptorOuts, context);
        });
        super.execute(inputs, outputs, context);
    }
}
