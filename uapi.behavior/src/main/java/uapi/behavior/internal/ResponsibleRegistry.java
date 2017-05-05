/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.behavior.internal;

import uapi.behavior.*;
import uapi.common.ArgumentChecker;
import uapi.common.Guarder;
import uapi.common.Repository;
import uapi.event.IEventBus;
import uapi.log.ILogger;
import uapi.rx.Looper;
import uapi.service.IInitial;
import uapi.service.IServiceLifecycle;
import uapi.service.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Registry for responsible
 */
@Service
@Tag("Behavior")
public class ResponsibleRegistry implements IResponsibleRegistry {

    @Inject
    protected ILogger _logger;

    @Inject
    protected IEventBus _eventBus;

    private final Repository<ActionIdentify, IAction<?, ?>> _actionRepo;

    private final Lock _lock;

    private final Map<String, IResponsible> _responsibles;

    @Inject
    @Optional
    protected List<IResponsibleConstructor> _respConstructors;

    public ResponsibleRegistry() {
        this._actionRepo = new Repository<>();
        this._lock = new ReentrantLock();
        this._responsibles = new HashMap<>();
    }

    @Inject
    @Optional
    public void addAction(IAction<?, ?> action) {
        ArgumentChecker.required(action, "action");
        IAction<?, ?> existing = this._actionRepo.put(action);
        if (existing != null) {
            this._logger.warn("The existing action {} was overridden by new action {}", existing, action);
        }
    }

    @OnActivate
    public void constructResponsibles() {
        Looper.on(this._respConstructors).foreach(this::addConstructor);
    }

    @OnInject
    public void injectNewAction(IAction action) {
        addAction(action);
    }

    @OnInject
    public void injectNewConstructor(IResponsibleConstructor constructor) {
        addConstructor(constructor);
//        if (service instanceof IAction) {
//            addAction((IAction) service);
//        } else if (service instanceof IResponsibleConstructor) {
//            addConstructor((IResponsibleConstructor) service);
//        } else {
//            throw BehaviorException.builder()
//                    .errorCode(BehaviorErrors.UNSUPPORTED_INJECTED_SERVICE)
//                    .variables(new BehaviorErrors.UnsupportedInjectedService()
//                            .injectedService(serviceId)
//                            .injectService(ResponsibleRegistry.class.getName()))
//                    .build();
//        }
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

    private void addConstructor(IResponsibleConstructor constructor) {
        ArgumentChecker.required(constructor, "constructor");
        IResponsible responsible = register(constructor.name());
        constructor.construct(responsible);
    }
}
