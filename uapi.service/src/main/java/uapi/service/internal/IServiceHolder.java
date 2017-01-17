/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.service.internal;

import uapi.service.CycleDependencyException;
import uapi.service.Dependency;
import uapi.service.IServiceReference;
import uapi.service.QualifiedServiceId;

import java.util.List;
import java.util.Stack;

/**
 * A service holder
 */
public interface IServiceHolder extends IServiceReference {

    /**
     * Try to activate the service
     *
     * @return  True means the service is activated otherwise it is failed
     */
    boolean tryActivate();

    /**
     * Try to activate the service
     *
     * @param   throwException
     *          Throw exception when the service can't be resolved, injected, satisfied...
     * @return  True means the service is activated otherwise it is failed
     */
    boolean tryActivate(boolean throwException);

    /**
     * Check the service is depends on specific service by service id
     *
     * @param   serviceId
     *          The service id which is the service that will to check this service is depends on it.
     * @return  True means this service is depends on the service, otherwise return false
     */
    boolean isDependsOn(final String serviceId);

    /**
     * Check the service is depends on specific service by qualified service id
     *
     * @param   qualifiedServiceId
     *          The qualified serviceid which is the service that will to check this service is depends on it.
     * @return  True means this service is depends on the service, otherwise return false
     */
    boolean isDependsOn(QualifiedServiceId qualifiedServiceId);

    /**
     * Set specific service to this service as its dependent service, if this service does
     * not depends on the service, and exception will be thrown
     *
     * @param   service
     *          The dependent service
     * @throws  CycleDependencyException
     *          When service has a cycle dependency
     */
    void setDependency(IServiceHolder service) throws CycleDependencyException;

    /**
     * Check whether this service dependency and dependent service has cycle dependency.
     * If it has an exception will be thrown
     *
     * @param   svcToCheck
     *          The service which will be checked
     * @param   dependencyStack
     *          The stack which used to store dependency path
     * @throws  CycleDependencyException
     *          When the service has a cycle dependency
     */
    void checkCycleDependency(
            final IServiceHolder svcToCheck,
            final Stack<IServiceHolder> dependencyStack
    ) throws CycleDependencyException;

    /**
     * Receive unresolved services from the repo
     *
     * @return  Unresolved service list
     */
    List<Dependency> getUnsetDependencies();

    /////////////////////////////////////////////
    // Methods for service life cycle exchange //
    /////////////////////////////////////////////

    /**
     * Resolve this service
     */
    void resolve();

    /**
     * Inject dependent services instance into this service
     */
    void inject();

    /**
     * Make this service satisfy
     */
    void satisfy();

    /**
     * Activate this service
     */
    void activate();
}
