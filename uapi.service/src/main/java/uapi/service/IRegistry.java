/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.service;

import uapi.InvalidArgumentException;

import java.util.List;
import java.util.Map;

/**
 * A registry for storing service, it has following features:
 *  * Hold service in local map
 *  * Resolve dependency between services
 */
public interface IRegistry {

    /**
     * Register a local service
     *
     * @param   service
     *          The service which will be registered
     * @throws  InvalidArgumentException
     *          The exception will be thrown when the service is null
     */
    void register(
            final IService service
    ) throws InvalidArgumentException;

    /**
     * Register multiple local services
     *
     * @param   services
     *          The services which will be registered
     * @throws  InvalidArgumentException
     *          The exception will be thrown when the service is null
     */
    void register(
            final IService... services
    ) throws InvalidArgumentException;

    /**
     * Register a generic object as a local service
     *
     * @param   service
     *          The service object
     * @param   serviceIds
     *          The related identifies of the service object
     * @throws  InvalidArgumentException
     *          If the service is null or the related identifies is not specified
     */
    void register(
            final Object service,
            String... serviceIds
    ) throws InvalidArgumentException;

    /**
     * Register a outside service
     *
     * @param   serviceFrom
     *          Where is the service from
     * @param   service
     *          The service instance
     * @param   serviceIds
     *          The service ids
     * @throws  InvalidArgumentException
     *          If the serviceFrom, service is null
     */
    void register(
            final String serviceFrom,
            final Object service,
            final String... serviceIds
    ) throws InvalidArgumentException;

    /**
     * Find service by specified service id
     *
     * @param   serviceId
     *          The specified service id
     * @param   <T>
     *          The service type
     * @return  The service instance
     * @throws  ServiceException
     *          The service can't be found
     */
    <T> T findService(
            final String serviceId
    ) throws ServiceException;

    /**
     * Find service by specific service type
     *
     * @param   serviceType
     *          The specified service type
     * @param   <T>
     *          The service type
     * @return  The service instance
     * @throws  ServiceException
     *          The service can't be found
     */
    <T> T findService(
            final Class<T> serviceType
    ) throws ServiceException;

    /**
     * Find service from specified location
     * @param   serviceId
     *          The service id which used for service finding
     * @param   serviceFrom
     *          Where is the service from
     * @param   <T>
     *          The service type
     * @return  The service instance
     * @throws  ServiceException
     *          The service can't be found
     */
    <T> T findService(
            final String serviceId,
            final String serviceFrom
    ) throws ServiceException;

    /**
     * Find instance service by service id and specific attributes
     *
     * @param   serviceId
     *          The prototype service id
     * @param   attributes
     *          The attributes which used for creating instance service
     * @param   <T>
     *          The service type
     * @return  The instance service
     * @throws  ServiceException
     *          The prototype service can't be found or creating instance service failed
     */
    <T> T findService(
            final String serviceId,
            final Map<String, ?> attributes
    ) throws ServiceException;

    /**
     * Find instance service by service type and specific attributes
     *
     * @param   serviceType
     *          The prototype service type
     * @param   attributes
     *          The attributes which used for creating instance service
     * @param   <T>
     *          The service type
     * @return  The instance service
     * @throws  ServiceException
     *          The prototype service can't be found or creating instance service failed
     */
    <T> T findService(
            final Class serviceType,
            final Map<String, ?> attributes
    ) throws ServiceException;

    /**
     * Find multiple service by specific service id
     *
     * @param   serviceId
     *          The service id which used for service finding
     * @param   <T>
     *          The service type
     * @return  The service list
     */
    <T> List<T> findServices(final String serviceId);

    /**
     * Find multiple services by specific service type
     *
     * @param   serviceType
     *          The service type which used for service finding
     * @param   <T>
     *          The service type
     * @return  The service list
     */
    <T> List<T> findServices(final Class<T> serviceType);

    /**
     * Activate service(s) by specific tag
     *
     * @param   tag
     *          The tag
     */
    void activateTaggedService(final String tag);

    /**
     * Deactivate service(s) by specific tag
     *
     * @param   tag
     *          The tags
     */
    void deactivateTaggedService(final String tag);

    /**
     * Deactivate service(s) by specific id
     *
     * @param   serviceIds
     *          Service id array
     */
    void deactivateServices(String[] serviceIds);
}
