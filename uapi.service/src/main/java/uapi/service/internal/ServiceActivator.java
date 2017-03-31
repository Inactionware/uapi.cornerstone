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
import uapi.rx.Looper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The service activator is used to activate service
 */
public class ServiceActivator {

    private static final IntervalTime DEFAULT_TIME_OUT  = IntervalTime.parse("5s");

    private static final int MAX_ACTIVE_THREAD_COUNT = Runtime.getRuntime().availableProcessors();

    private final Lock _lock;
    private final NotifiableList<ServiceActiveTask> _tasks;

    public ServiceActivator() {
        this._tasks = new NotifiableList<>(MAX_ACTIVE_THREAD_COUNT);
        this._lock = new ReentrantLock();
    }

    public <T> T activeService(final ServiceHolder serviceHolder) {
        ArgumentChecker.required(serviceHolder, "serviceHolder");
        if (serviceHolder.isActivated()) {
            return (T) serviceHolder.getService();
        }

        // Make out unactivated dependency service tree, need check out cycle dependency case
        List<UnactivatedService> svcList = new ArrayList<>();
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
        }).timeout(DEFAULT_TIME_OUT).start();

//        // Create new service active task thread to handle
//        CompletableFuture<T> future = CompletableFuture.supplyAsync(() -> {
//            int position = 0;
//            while (position < svcList.size()) {
//                    UnactivatedService unactivatedSvc = svcList.get(position);
//                    unactivatedSvc.serviceHolder().activate();
//                    position++;
//            }
//            ServiceActivator.this._handlingSvcs.remove(svcList);
//            return (T) svcList.get(0).serviceHolder().getService();
//        });
//        future.whenComplete((svc, t) -> true);
//        return (T) future.get();

        new Thread(task).start();
        boolean isTaskDone;
        try {
            isTaskDone = task._semaphore.tryAcquire(DEFAULT_TIME_OUT.milliseconds(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            throw new GeneralException(ex);
        }
        if (! isTaskDone) {
            throw new GeneralException("The task for activate service {} is timed out", serviceHolder.getQualifiedId());
        }
        this._tasks.remove(task);
        if (task._ex != null) {
            throw new GeneralException(task._ex);
        }
        return (T) serviceHolder.getService();
    }

    private void constructServiceStack(final UnactivatedService service, final List<UnactivatedService> svcList) {
        svcList.add(service);
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

    private final class ServiceActiveTask implements Runnable {

        private final List<UnactivatedService> _svcList;
        private final Semaphore _semaphore;
        private Exception _ex;

        ServiceActiveTask(final List<UnactivatedService> serviceList) {
            this._svcList = serviceList;
            this._semaphore = new Semaphore(0);
        }

        @Override
        public void run() {
            int position = 0;
            while (position < this._svcList.size()) {
                try {
                    UnactivatedService unactivatedSvc = this._svcList.get(position);
                    // TODO: need consider load external service
                    unactivatedSvc.activate();
                    position++;
                } catch (Exception ex) {
                    this._ex = ex;
                }
            }
            ServiceActivator.this._tasks.remove(this);
            this._semaphore.release();
        }

        private UnactivatedService isInHandling(List<UnactivatedService> unactivatedServices) {
            int foundIdx = Looper.on(unactivatedServices)
                    .map(this._svcList::indexOf)
                    .filter((idx) -> ! this._svcList.get(idx).isActivated())
                    .first(-1);
            if (foundIdx < 0) {
                return null;
            } else {
                return this._svcList.get(foundIdx);
            }
        }
    }
}
