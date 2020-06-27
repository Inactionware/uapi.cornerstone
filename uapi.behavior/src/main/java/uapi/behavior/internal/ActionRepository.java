/*
 * Copyright (c) 2020. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product.
 */

package uapi.behavior.internal;

import uapi.behavior.ActionIdentify;
import uapi.behavior.IAction;
import uapi.behavior.IActionMeta;
import uapi.common.ArgumentChecker;
import uapi.common.MapHelper;
import uapi.common.Repository;
import uapi.log.ILogger;
import uapi.service.IRegistry;
import uapi.service.QualifiedServiceId;
import uapi.service.Tags;
import uapi.service.annotation.*;
import uapi.service.ServiceType;

import java.util.HashMap;
import java.util.Map;

@Service
@Tag(Tags.BEHAVIOR)
public class ActionRepository extends Repository<ActionIdentify, IAction> {

    @Inject
    protected ILogger _logger;

    @Inject
    protected IRegistry _svcReg;

    private Map<ActionIdentify, IActionMeta> _actionMetas = new HashMap<>();

    @Inject
    @Optional
    public void addAction(IActionMeta actionMeta) {
        var existing = this._actionMetas.put(actionMeta.actionId(), actionMeta);
        if (existing != null) {
            this._logger.warn("The existing action {} was overridden by new action {}",
                    existing.actionId(), actionMeta.actionId());
        }
    }

    @OnInject
    public void injectAction(IActionMeta actionMeta) {
        addAction(actionMeta);
    }

    @Override
    public IAction get(
            final ActionIdentify id
    ) {
        return get(id, null);
    }

    public IAction get(
            final ActionIdentify id,
            final Map<Object, Object> attributes
    ) {
        ArgumentChecker.required(id, "id");
        IActionMeta actionMeta = this._actionMetas.get(id);
        if (actionMeta == null) {
            return null;
        }
        if (actionMeta.serviceType() == ServiceType.Prototype) {
            Map<Object, Object> attrs;
            if (attributes == null) {
                attrs = MapHelper.EMPTY;
            } else {
                attrs = attributes;
            }
            return this._svcReg.findService(actionMeta.actionId().getId(), attrs);
        } else {
            IAction action = super.get(id);
            if (action != null) {
                return action;
            }
            return this._svcReg.findService(actionMeta.actionId().getName(), QualifiedServiceId.FROM_LOCAL);
        }
    }
}
