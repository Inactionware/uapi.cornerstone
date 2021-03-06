/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.service.internal;

import uapi.GeneralException;
import uapi.UapiException;
import uapi.common.ArgumentChecker;
import uapi.common.Guarder;
import uapi.common.IntervalTime;
import uapi.common.Watcher;
import uapi.rx.Looper;
import uapi.service.ServiceErrors;
import uapi.service.ServiceException;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * The service activator is used to activate service
 */
public class ServiceActivator {

    private static final IntervalTime DEFAULT_TIME_OUT  = IntervalTime.parse("5s");

    private static final int MAX_ACTIVE_THREAD_COUNT = Runtime.getRuntime().availableProcessors();

    private final Lock _lock;
    private final AwaitingList<ServiceActiveTask> _tasks;

    private final IExternalServiceLoader _extSvcLoader;

    public ServiceActivator(final IExternalServiceLoader externalServiceLoader) {
        this._tasks = new AwaitingList<>(MAX_ACTIVE_THREAD_COUNT);
        this._lock = new ReentrantLock();
        this._extSvcLoader = externalServiceLoader;
    }

    public <T> Optional<T> tryActivateService(final ServiceHolder serviceHolder) {
        T result = null;
        try {
            result = activateService(serviceHolder);
        } catch (Exception ex) {
            // do nothing
        }
        return Optional.ofNullable(result);
    }

    public <T> T activateService(final ServiceHolder serviceHolder) {
        return activateService(serviceHolder, DEFAULT_TIME_OUT);
    }

    public <T> T activateService(final ServiceHolder serviceHolder, IntervalTime timeout) {
        ArgumentChecker.required(serviceHolder, "serviceHolder");
        if (timeout == null) {
            timeout = DEFAULT_TIME_OUT;
        }
        if (serviceHolder.isActivated()) {
            return (T) serviceHolder.getService();
        }

        // Make out unactivated dependency service tree, need check out cycle dependency case
        var svcList = new LinkedList<UnactivatedService>();
        constructServiceStack(new UnactivatedService(null, serviceHolder), svcList);

        var task = new ServiceActiveTask(svcList);
        Watcher.on(() -> {
            // Check unactivated service again when the watch is notified by other thread
            var newSvcList =
                    Looper.on(svcList).filter(unactivatedSvc -> !unactivatedSvc.isActivated()).toList();

            return Guarder.by(this._lock).runForResult(() -> {
                // Check whether the service which in the tree is in existing service active task
                // if it is, then the service active should be wait until the existing active task finish
                var handlingSvc = Looper.on(this._tasks.iterator())
                        .map(handlingTask -> handlingTask.isInHandling(newSvcList))
                        .first(null);
                if (handlingSvc != null) {
                    return new Watcher.ConditionResult(handlingSvc);
                }

                if (this._tasks.put(task)) {
                    return new Watcher.ConditionResult(false);
                } else {
                    return new Watcher.ConditionResult(this._tasks);
                }
            });
        }).timeout(timeout).start();

        // Assign service active task to handle
        CompletableFuture<ActivateServiceResult<T>> future = CompletableFuture.supplyAsync(task);
        ActivateServiceResult<T> result;
        try {
            result = future.get(timeout.milliseconds(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException ex) {
            if (ex.getCause() instanceof ServiceException) {
                throw (ServiceException) ex.getCause();
            } else {
                throw new GeneralException(ex);
            }
        } catch (TimeoutException ex) {
            throw ServiceException.builder()
                    .errorCode(ServiceErrors.SERVICE_ACTIVE_TASK_TIMED_OUT)
                    .variables(new ServiceErrors.ServiceActiveTaskTimedOut()
                        .serviceId(serviceHolder.getQualifiedId())
                        .serviceType(serviceHolder.getService()))
                    .build();
        }

        if (result.exception != null) {
            throw result.exception;
        }

        // Need try to activate services which monitored on new activated services
        try {
            Looper.on(svcList)
                    .filter(UnactivatedService::isActivated)
                    .map(UnactivatedService::holder)
                    .filter(ServiceHolder::hasMonitor)
                    .flatmap(svcHolder -> Looper.on(svcHolder.getMonitoredServices()))
                    .foreach(this::tryActivateService);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return result.service;
    }

    public void deactivateService(final ServiceHolder serviceHolder) {
        deactivateService(serviceHolder, DEFAULT_TIME_OUT);
    }

    public void deactivateService(final ServiceHolder serviceHolder, IntervalTime timeout) {
        ArgumentChecker.required(serviceHolder, "serviceHolder");
        if (timeout == null) {
            timeout = DEFAULT_TIME_OUT;
        }
        if (serviceHolder.isDeactivated()) {
            return;
        }

        var future = CompletableFuture.runAsync(new ServiceDeactivateTask(serviceHolder));
        try {
            future.get(timeout.milliseconds(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException ex) {
            throw new GeneralException(ex);
        } catch (TimeoutException ex) {
            throw ServiceException.builder()
                    .errorCode(ServiceErrors.SERVICE_DEACTIVATION_TASK_TIMED_OUT)
                    .variables(new ServiceErrors.ServiceDeactivationTaskTimedOut()
                        .serviceId(serviceHolder.getQualifiedId())
                        .serviceType(serviceHolder.getService()))
                    .build();
        }
    }

    private void constructServiceStack(final UnactivatedService service, final List<UnactivatedService> svcList) {
        svcList.add(0, service);
        if (service.isExternalService()) {
            // External service should not have dependencies
            return;
        }
        var unactivatedSvcs = service.getUnactivatedDependencies();
        if (unactivatedSvcs.size() == 0) {
            return;
        }
        Looper.on(unactivatedSvcs)
                .next(unactivatedSvc -> {
                    unactivatedSvc.referencedBy(service);
                    unactivatedSvc.checkCycleDependency();
                })
                .foreach(unactivatedService -> constructServiceStack(unactivatedService, svcList));
    }

    public final class A<T extends ActivateServiceResult<A>> implements Supplier<T> {

        @Override
        public T get() {
            return null;
        }
    }

    private final class ServiceActiveTask<T> implements Supplier<ActivateServiceResult<T>> {

        private final List<UnactivatedService> _svcList;

        ServiceActiveTask(final List<UnactivatedService> serviceList) {
            this._svcList = serviceList;
        }

        @Override
        public ActivateServiceResult<T> get() {
            int position = 0;
            UnactivatedService unactivatedSvc = null;
            Exception exception = null;
            try {
                while (position < this._svcList.size()) {
                    unactivatedSvc = this._svcList.get(position);
                    if (unactivatedSvc.isExternalService()) {
                        // load external service
                        var svcHolder =
                                ServiceActivator.this._extSvcLoader.loadService(unactivatedSvc.dependency());
                        if (svcHolder != null) {
                            unactivatedSvc.activate(svcHolder);
                        }
                    } else {
                        unactivatedSvc.activate();
                    }
                    if (!unactivatedSvc.isActivated() && !unactivatedSvc.dependency().isOptional()) {
                        throw ServiceException.builder()
                                .errorCode(ServiceErrors.SERVICE_ACTIVATION_FAILED)
                                .variables(new ServiceErrors.ServiceActivationFailed()
                                        .serviceId(unactivatedSvc.serviceId()))
                                .build();
                    }
                    position++;
                }
            } catch (Exception ex) {
                exception = ex;
            } finally {
                Guarder.by(ServiceActivator.this._lock).run(() -> ServiceActivator.this._tasks.remove(this));
            }

            if (exception != null) {
                if (exception instanceof ServiceException) {
                    return new ActivateServiceResult<>((ServiceException) exception);
                } else {
                    return new ActivateServiceResult<>(new UapiException(exception));
                }
            } else if (unactivatedSvc == null) {
                var ex = ServiceException.builder()
                        .errorCode(ServiceErrors.SERVICE_ACTIVATION_FAILED)
                        .variables(new ServiceErrors.ServiceActivationFailed()
                                .serviceId(unactivatedSvc.serviceId()))
                        .build();
                return new ActivateServiceResult<>(ex);
            } else {
                return new ActivateServiceResult(unactivatedSvc.service());
            }
        }

        private UnactivatedService isInHandling(List<UnactivatedService> unactivatedServices) {
            var foundIdx = Looper.on(unactivatedServices)
                    .map(this._svcList::indexOf)
                    .filter(idx -> idx >= 0)
                    .filter(idx -> ! this._svcList.get(idx).isActivated())
                    .first(-1);
            if (foundIdx < 0) {
                return null;
            } else {
                return this._svcList.get(foundIdx);
            }
        }
    }

    private final class ServiceDeactivateTask implements Runnable {

        private final ServiceHolder _svcHolder;

        ServiceDeactivateTask(final ServiceHolder serviceHolder) {
            this._svcHolder = serviceHolder;
        }

        @Override
        public void run() {
            this._svcHolder.deactivate();
        }
    }

    private static final class ActivateServiceResult<T> {

        private final T service;
        private final UapiException exception;

        ActivateServiceResult(final T service) {
            this(service, null);
        }

        private ActivateServiceResult(final UapiException exception) {
            this(null, exception);
        }

        private ActivateServiceResult(final T service, final UapiException exception) {
            this.service = service;
            this.exception = exception;
        }
    }
}
