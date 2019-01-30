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

public class BehaviorFailureAction extends AnonymousAction<IBehaviorFailureCall> {

    public static final String OUT_BEHAVIOR_EVENT   = "BehaviorEvent";

    public BehaviorFailureAction(
            final IBehaviorFailureCall action,
            final ActionIdentify behaviorId
    ) {
        super(action, behaviorId);
    }

    @Override
    public ActionInputMeta[] inputMetas() {
        return new ActionInputMeta[] {
                new ActionInputMeta(BehaviorFailure.class)
        };
    }

    @Override
    public ActionOutputMeta[] outputMetas() {
        return new ActionOutputMeta[] {
                new ActionOutputMeta(BehaviorEvent.class, OUT_BEHAVIOR_EVENT)
        };
    }

    @Override
    public ActionResult process(
            final Object[] inputs,
            final ActionOutput[] outputs,
            final IExecutionContext context
    ) {
        BehaviorFailure behaviorFailure = (BehaviorFailure) inputs[0];
        BehaviorEvent event = action().accept(behaviorFailure, context);
        if (event != null) {
            outputs[0].set(event);
        }
        return new ActionResult(getId());
    }
}
