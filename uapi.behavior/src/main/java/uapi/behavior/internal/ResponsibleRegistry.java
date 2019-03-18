/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.behavior.internal;

import uapi.GeneralException;
import uapi.service.Tags;
import uapi.behavior.*;
import uapi.common.ArgumentChecker;
import uapi.common.Guarder;
import uapi.common.Repository;
import uapi.event.IEventBus;
import uapi.log.ILogger;
import uapi.service.IServiceLifecycle;
import uapi.service.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Registry for responsible
 */
@Service(IResponsibleRegistry.class)
@Tag(Tags.BEHAVIOR)
public class ResponsibleRegistry implements IResponsibleRegistry, IServiceLifecycle {

    @Inject
    protected ILogger _logger;

    @Inject
    protected IEventBus _eventBus;

    private final Repository<ActionIdentify, IAction> _actionRepo;

    private final Lock _lock;

    private final Map<String, IResponsible> _responsibles;

    public ResponsibleRegistry() {
        this._actionRepo = new Repository<>();
        this._lock = new ReentrantLock();
        this._responsibles = new HashMap<>();
    }

    @Inject
    @Optional
    public void addAction(IAction action) {
        ArgumentChecker.required(action, "action");
        if (action instanceof IInterceptor && action instanceof IIntercepted) {
            throw BehaviorException.builder()
                    .errorCode(BehaviorErrors.UNSUPPORTED_INTERCEPTIVE_INTERCEPTOR)
                    .variables(new BehaviorErrors.UnsupportedInterceptiveInterceptor()
                            .interceptorId(action.getId()))
                    .build();
        }
        // Check duplicated Action output
        ActionOutputMeta[] metas = action.outputMetas();
        for (int i = 0; i < metas.length; i++) {
            for (int j = i + 1; j < metas.length; j++) {
                if (metas[i].name().equals(metas[j].name())) {
                    throw BehaviorException.builder()
                            .errorCode(BehaviorErrors.DUPLICATED_ACTION_OUTPUT)
                            .variables(new BehaviorErrors.DuplicatedActionOutput()
                                    .outputName(metas[i].name())
                                    .actionId(action.getId()))
                            .build();
                }
            }
        }

        IAction existing = this._actionRepo.put(action);
        if (existing != null) {
            this._logger.warn("The existing action {} was overridden by new action {}", existing, action);
        }
    }

    @OnInject
    public void injectNewAction(IAction action) {
        addAction(action);
    }

    @Override
    public IResponsible register(String name) throws BehaviorException {
        ArgumentChecker.required(name, "name");
        Responsible responsible = new Responsible(name, this._eventBus, this._actionRepo);
        Guarder.by(this._lock).run(() -> {
            if (this._responsibles.containsKey(name)) {
                throw BehaviorException.builder()
                        .errorCode(BehaviorErrors.DUPLICATED_RESPONSIBLE_NAME)
                        .variables(new BehaviorErrors.DuplicatedResponsibleName()
                                .responsibleName(name))
                        .build();
            }
            this._responsibles.put(name, responsible);
        });
        return responsible;
    }

    @Override
    public void unregister(String name) {
        ArgumentChecker.required(name, "name");
        Guarder.by(this._lock).run(() -> this._responsibles.remove(name));
    }

    @Override
    public int responsibleCount() {
        return this._responsibles.size();
    }

    public int actionCount() {
        return this._actionRepo.count();
    }

    @Override
    public void onDependencyInject(String serviceId, Object service) {
        if (IAction.class.getCanonicalName().equals(serviceId) && service instanceof IAction) {
            this._actionRepo.put((IAction) service);
        } else {
            throw new GeneralException(
                    "Unsupported dependency injection - {}, {}", service, service.getClass().getCanonicalName());
        }
    }
}
