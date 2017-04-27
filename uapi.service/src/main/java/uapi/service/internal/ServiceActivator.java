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
import uapi.common.ArgumentChecker;
import uapi.common.Guarder;
import uapi.common.IntervalTime;
import uapi.common.Watcher;
import uapi.rx.Looper;
import uapi.service.ServiceErrors;
import uapi.service.ServiceException;

import java.util.LinkedList;
import java.util.List;
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

    public <T> T activeService(final ServiceHolder serviceHolder) {
        return activeService(serviceHolder, DEFAULT_TIME_OUT);
    }

    public <T> T activeService(final ServiceHolder serviceHolder, IntervalTime timeout) {
        ArgumentChecker.required(serviceHolder, "serviceHolder");
        if (timeout == null) {
            timeout = DEFAULT_TIME_OUT;
        }
        if (serviceHolder.isActivated()) {
            return (T) serviceHolder.getService();
        }

        // Make out unactivated dependency service tree, need check out cycle dependency case
        List<UnactivatedService> svcList = new LinkedList<>();
        constructServiceStack(new UnactivatedService(null, serviceHolder), svcList);

        ServiceActiveTask task = new ServiceActiveTask(svcList);
        Watcher.on(() -> {
            // Check unactivated service again when the watch is notified by other thread
            List<UnactivatedService> newSvcList =
                    Looper.on(svcList).filter(unactivatedSvc -> !unactivatedSvc.isActivated()).toList();

            return Guarder.by(this._lock).runForResult(() -> {
                // Check whether the service which in the tree is in existing service active task
                // if it is, then the service active should be wait until the existing active task finish
                UnactivatedService handlingSvc = Looper.on(this._tasks.iterator())
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
        CompletableFuture<T> future = CompletableFuture.supplyAsync(task);
        try {
            return future.get(timeout.milliseconds(), TimeUnit.MILLISECONDS);
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
                        .serviceId(serviceHolder.getQualifiedId()))
                    .build();
        }
    }

    private void constructServiceStack(final UnactivatedService service, final List<UnactivatedService> svcList) {
        svcList.add(0, service);
        if (service.isExternalService()) {
            // External service should not have dependencies
            return;
        }
        List<UnactivatedService> unactivatedSvcs = service.getUnactivatedDependencies();
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

    private final class ServiceActiveTask<T> implements Supplier<T> {

        private final List<UnactivatedService> _svcList;

        ServiceActiveTask(final List<UnactivatedService> serviceList) {
            this._svcList = serviceList;
        }

        @Override
        public T get() {
            int position = 0;
            UnactivatedService unactivatedSvc = null;
            while (position < this._svcList.size()) {
                unactivatedSvc = this._svcList.get(position);
                if (unactivatedSvc.isExternalService()) {
                    // load external service
                    ServiceHolder svcHolder =
                            ServiceActivator.this._extSvcLoader.loadService(unactivatedSvc.dependency());
//                    if (svcHolder == null) {
//                        throw ServiceException.builder()
//                                .errorCode(ServiceErrors.LOAD_EXTERNAL_SERVICE_FAILED)
//                                .variables(new ServiceErrors.LoadExternalServiceFailed()
//                                    .serviceId(unactivatedSvc.serviceId()).get())
//                                .build();
//                    }
                    if (svcHolder != null) {
                        unactivatedSvc.activate(svcHolder);
                    }
                } else {
                    unactivatedSvc.activate();
                }
                if (! unactivatedSvc.isActivated() && ! unactivatedSvc.dependency().isOptional()) {
                    throw ServiceException.builder()
                            .errorCode(ServiceErrors.SERVICE_ACTIVATION_FAILED)
                            .variables(new ServiceErrors.ServiceActivationFailed()
                                .serviceId(unactivatedSvc.serviceId()))
                            .build();
                }
                position++;
            }
            Guarder.by(ServiceActivator.this._lock).run(() -> ServiceActivator.this._tasks.remove(this));
            if (unactivatedSvc == null) {
                throw ServiceException.builder()
                        .errorCode(ServiceErrors.SERVICE_ACTIVATION_FAILED)
                        .variables(new ServiceErrors.ServiceActivationFailed()
                                .serviceId(unactivatedSvc.serviceId()))
                        .build();
            }
            return (T) unactivatedSvc.service();
        }

        private UnactivatedService isInHandling(List<UnactivatedService> unactivatedServices) {
            int foundIdx = Looper.on(unactivatedServices)
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
}
