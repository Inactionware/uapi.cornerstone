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
import uapi.common.CollectionHelper;
import uapi.rx.Looper;
import uapi.service.Dependency;
import uapi.service.ServiceErrors;
import uapi.service.ServiceException;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Semaphore;

/**
 * The service activator is used to activate service
 */
public class ServiceActivator {

    private static final int MAX_ACTIVE_THREAD_COUNT = Runtime.getRuntime().availableProcessors();

    private final List<ServiceActiveTask> _tasks;

    public ServiceActivator() {
        this._tasks = new LinkedList<>();
    }

    public <T> T activeService(final ServiceHolder serviceHolder) {
        ArgumentChecker.required(serviceHolder, "serviceHolder");
        if (serviceHolder.isActivated()) {
            return (T) serviceHolder.getService();
        }

        // Make out unactivated dependency service tree, need check out cycle dependency case
        Stack<UnactivatedService> svcStack = new Stack<>();
        constructServiceStack(new UnactivatedService(null, serviceHolder), svcStack);

        // Check whether the service which in the tree is in existing service active task
        // if it is, then the service active should be wait until the existing active task finish

        // Check service active task is full or not, if it is full then an exception should be thrown

        // Create new service active task thread to handle
        return null;
    }

    private void constructServiceStack(final UnactivatedService service, final List<UnactivatedService> svcList) {
        svcList.add(service);
        if (service.serviceHolder() == null) {
            return;
        }
        List<UnactivatedService> unactivatedSvcs = service.serviceHolder().getUnactivatedServices();
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

        private final List<IServiceHolder> _svcList;
        private final Semaphore _semaphore;
        private Exception _ex;

        ServiceActiveTask(final Stack<IServiceHolder> serviceList, final Semaphore semaphore) {
            this._svcList = serviceList;
            this._semaphore = semaphore;
        }

        @Override
        public void run() {
            int position = 0;
            while (position < this._svcList.size()) {
                try {
                    IServiceHolder svcHolder = this._svcList.get(position);
                    if (!svcHolder.tryActivate()) {
                        this._ex = new GeneralException("The service can't be activate");
                        break;
                    }
                    position++;
                } catch (Exception ex) {
                    this._ex = ex;
                }
            }
            ServiceActivator.this._tasks.remove(this);
            this._semaphore.release();
        }

        private boolean isInHandling(List<UnactivatedService> unactivatedServices) {
            return Looper.on(unactivatedServices)
                    .filter(this._svcList::contains)
                    .first(null) != null;
        }
    }
}
