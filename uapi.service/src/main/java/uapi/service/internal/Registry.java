/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.service.internal;

import com.google.auto.service.AutoService;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import uapi.GeneralException;
import uapi.InvalidArgumentException;
import uapi.Tags;
import uapi.common.ArgumentChecker;
import uapi.common.CollectionHelper;
import uapi.common.Guarder;
import uapi.common.StringHelper;
import uapi.log.ConsoleLogger;
import uapi.rx.Looper;
import uapi.service.*;
import uapi.log.ILogger;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

/**
 * Implementation of IRegistry
 */
@AutoService(IService.class)
public class Registry implements IRegistry, IService, ITagged, IInjectable {

    private static final String[] tags = new String[] {Tags.REGISTRY };

    private final ReadWriteLock _svcRepoLock;
    private final SatisfyDecider _satisfyDecider;
    private final Multimap<String, ServiceHolder> _svcRepo;
    private final List<WeakReference<ISatisfyHook>> _satisfyHooks;
    private Map<String, IServiceLoader> _svcLoaders;
    private final SortedSet<IServiceLoader> _orderedSvcLoaders;
    private final ServiceActivator _svcActivator;

    private ILogger _logger;
    private final ILogger _defaultLogger;

    private final IServiceLoader.IServiceReadyListener _svcReadyListener;

    public Registry() {
        this._svcRepoLock = new ReentrantReadWriteLock();
        this._svcRepo = LinkedListMultimap.create();
        this._satisfyHooks = new CopyOnWriteArrayList<>();
        this._satisfyDecider = new SatisfyDecider();
        this._svcLoaders = new HashMap<>();
        this._orderedSvcLoaders = new TreeSet<>();
        this._defaultLogger = new ConsoleLogger();
        this._svcActivator = new ServiceActivator((dependency) -> {
            QualifiedServiceId qSvcId = dependency.getServiceId();
            String from = qSvcId.getFrom();
            String svcId = qSvcId.getId();
            // Check currently the service is already loaded or not
            ServiceHolder svcHolder = findServiceHolder(svcId, from);
            if (svcHolder != null) {
                return svcHolder;
            }

            Object svc;
            if (from.equals(QualifiedServiceId.FROM_ANY)) {
                // Search from any loader
                Iterator<IServiceLoader> svcLoadersIte = Registry.this._orderedSvcLoaders.iterator();
                boolean loaded = false;
                while (svcLoadersIte.hasNext()) {
                    IServiceLoader svcLoader = svcLoadersIte.next();
                    svc = svcLoader.load(qSvcId.getId(), dependency.getServiceType());
                    if (svc == null) {
                        continue;
                    }
                    loaded = true;
                    registerService(from, svc, new String[]{qSvcId.getId()}, new Dependency[0]);
                    if (dependency.isSingle()) {
                        break;
                    }
                }
                if (!loaded && !dependency.isOptional()) {
                    getLogger().error("No any service loader can load service {}", qSvcId);
                    return null;
                }
            } else {
                // Search specific service loader
                IServiceLoader svcLoader = this._svcLoaders.get(from);
                if (svcLoader == null) {
                    getLogger().error("Can't load service {} because no service loader for {}", qSvcId, from);
                    return null;
                }
                svc = svcLoader.load(qSvcId.getId(), dependency.getServiceType());
                if (svc == null && !dependency.isOptional()) {
                    getLogger().error("Load service {} from location {} failed", qSvcId, from);
                    return null;
                }
                registerService(from, svc, new String[]{qSvcId.getId()}, new Dependency[0]);
            }
            return findService(svcId, from);
        });

        this._svcReadyListener = (dependency, service) -> {
            // Register new service
            QualifiedServiceId qSvcId = dependency.getServiceId();
            registerService(qSvcId.getFrom(), service, new String[]{qSvcId.getId()}, new Dependency[0]);
        };
    }

    @Override
    public String[] getIds() {
        return new String[] { IRegistry.class.getCanonicalName() };
    }

    @Override
    public boolean autoActive() {
        return false;
    }

    @Override
    public void register(
            final IService service
    ) throws InvalidArgumentException {
        registerService(service);
    }

    @Override
    public void register(
            final IService... services
    ) throws InvalidArgumentException {
        Stream.of(services).forEach(this::register);
    }

    @Override
    public void register(
            final Object service,
            final String... serviceIds
    ) throws InvalidArgumentException {
        register(QualifiedServiceId.FROM_LOCAL, service, serviceIds);
    }

    @Override
    public void register(
            final String serviceFrom,
            final Object service,
            final String... serviceIds
    ) throws InvalidArgumentException {
        ArgumentChecker.notEmpty(serviceFrom, "serviceFrom");
        ArgumentChecker.notNull(service, "service");
        registerService(serviceFrom, service, serviceIds, new Dependency[0]);
    }

    @Override
    public <T> T findService(
            final Class<T> serviceType
    ) throws ServiceException {
        ArgumentChecker.notNull(serviceType, "serviceType");
        return findService(serviceType.getName());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T findService(
            final String serviceId
    ) throws ServiceException {
        ArgumentChecker.notEmpty(serviceId, "serviceId");
        List<Object> svcs = findServices(serviceId);
        if (svcs.size() == 1) {
            return (T) svcs.get(0);
        }
        if (svcs.size() == 0) {
            throw ServiceException.builder()
                    .errorCode(ServiceErrors.NO_SERVICE_FOUND)
                    .variables(new ServiceErrors.NoServiceFound()
                            .serviceId(serviceId))
                    .build();
        }
        throw ServiceException.builder()
                .errorCode(ServiceErrors.MULTIPLE_SERVICE_FOUND)
                .variables(new ServiceErrors.MultipleServiceFound()
                    .serviceId(serviceId))
                .build();
    }

    @Override
    public <T> T findService(
            final Class serviceType,
            final Map<String, ?> attributes
    ) throws ServiceException {
        return findService(serviceType.getName(), attributes);
    }

    @Override
    public <T> T findService(
            final String serviceId,
            final Map<String, ?> attributes
    ) throws ServiceException {
        ArgumentChecker.required(serviceId, "serviceId");
        ArgumentChecker.required(attributes, "attributes");
        ServiceHolder svcHolder = findServiceHolder(serviceId, QualifiedServiceId.FROM_LOCAL);
        if (svcHolder == null) {
            throw ServiceException.builder()
                    .errorCode(ServiceErrors.NO_SERVICE_FOUND)
                    .variables(new ServiceErrors.NoServiceFound().serviceId(serviceId))
                    .build();
        }
        if (! (svcHolder instanceof PrototypeServiceHolder)) {
            throw ServiceException.builder()
                    .errorCode(ServiceErrors.NOT_A_PROTOTYPE_SERVICE)
                    .variables(new ServiceErrors.NotAPrototypeService().serviceId(serviceId))
                    .build();
        }
        IInstance instance = ((PrototypeServiceHolder) svcHolder).newInstance(attributes);
        register(instance);
        ServiceHolder instanceHolder = findServiceHolder(instance.getIds()[0]);
        if (instanceHolder == null) {
            throw ServiceException.builder()
                    .errorCode(ServiceErrors.INSTANCE_SERVICE_REGISTER_FAILED)
                    .variables(new ServiceErrors.InstanceServiceRegisterFailed()
                            .instanceServiceId(instance.getIds()[0])
                            .prototypeServiceId(svcHolder.getId()))
                    .build();
        }
        return this._svcActivator.activateService(instanceHolder);
    }

    @Override
    public <T> List<T> findServices(
            final Class<T> serviceType
    ) {
        ArgumentChecker.notNull(serviceType, "serviceType");
        return findServices(serviceType.getName());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> findServices(
            final String serviceId
    ) {
        ArgumentChecker.notEmpty(serviceId, "serviceId");
        List<ServiceHolder> svcHolders = findServiceHolders(serviceId);
        if (svcHolders == null || svcHolders.size() == 0) {
            return Collections.emptyList();
        }
        return (List<T>) Looper.on(svcHolders)
                .map(this._svcActivator::activateService)
                .filter(svc -> svc != null)
                .toList();
    }

    @Override
    public <T> T findService(
            final String serviceId,
            final String serviceFrom
    ) throws ServiceException {
        ArgumentChecker.required(serviceId, "serviceId");
        ArgumentChecker.required(serviceFrom, "serviceFrom");
        ServiceHolder svcHolder = findServiceHolder(serviceId, serviceFrom);
        if (svcHolder == null) {
            return null;
        }
        T svc = this._svcActivator.activateService(svcHolder);
        if (svc == null) {
            throw ServiceException.builder()
                    .errorCode(ServiceErrors.NO_SERVICE_FOUND)
                    .variables(new ServiceErrors.NoServiceFound()
                            .serviceId(serviceId))
                    .build();
        }
        return svc;
    }

    @Override
    public void activateTaggedService(
            final String tag
    ) {
        ArgumentChecker.notEmpty(tag, "tag");
        List<ServiceHolder> svcHolders = Guarder.by(this._svcRepoLock.readLock()).runForResult(() ->
            Looper.on(this._svcRepo.values())
                    .filter(svcHolder -> CollectionHelper.isContains(svcHolder.serviceTags(), tag))
                    .toList()
        );
        if (svcHolders.size() == 0) {
            return;
        }
        Looper.on(svcHolders).foreach(this._svcActivator::activateService);
    }

    @Override
    public void deactivateTaggedService(
            final String tag
    ) {
        ArgumentChecker.notEmpty(tag, "tag");
        List<ServiceHolder> svcHolders = Guarder.by(this._svcRepoLock.readLock()).runForResult(() ->
                Looper.on(this._svcRepo.values())
                        .filter(svcHolder -> CollectionHelper.isContains(svcHolder.serviceTags(), tag))
                        .toList()
        );
        if (svcHolders.size() == 0) {
            return;
        }
        Looper.on(svcHolders).foreach(this._svcActivator::deactivateService);
    }

    @Override
    public void deactivateServices(String[] serviceIds) {
        ArgumentChecker.required(serviceIds, "serviceIds");
        List<ServiceHolder> svcHolders = Guarder.by(this._svcRepoLock.readLock()).runForResult(() ->
            Looper.on(this._svcRepo.values())
                .filter(svcHolder -> CollectionHelper.isContains(serviceIds, svcHolder.getId()))
                .toList()
        );
        if (svcHolders.size() == 0) {
            return;
        }
        Looper.on(svcHolders).foreach(this._svcActivator::deactivateService);
    }

    private List<ServiceHolder> findServiceHolders(
            final String serviceId
    ) {
        List<ServiceHolder> svcHolders;
        try {
            svcHolders = Guarder.by(this._svcRepoLock.readLock()).runForResult(() ->
                Looper.on(this._svcRepo.values())
                    .filter(svcHolder -> svcHolder.getId().equals(serviceId))
                    .toList()
            );
        } catch (Exception ex) {
            getLogger().error(ex);
            svcHolders = Collections.emptyList();
        }
        return svcHolders;
    }

    private ServiceHolder findServiceHolder(final String serviceId) {
        return findServiceHolder(serviceId, QualifiedServiceId.FROM_LOCAL);
    }

    private ServiceHolder findServiceHolder(
            final String serviceId,
            final String serviceFrom
    ) {
        List<ServiceHolder> found = findServiceHolders(serviceId);
        if (found == null || found.size() == 0) {
            return null;
        }
        found = Looper.on(found)
                .filter(svcHolder -> svcHolder.getFrom().equals(serviceFrom))
                .toList();
        if (found == null || found.size() == 0) {
            return null;
        }
        if (found.size() == 1) {
            return found.get(0);
        }
        throw ServiceException.builder()
                .errorCode(ServiceErrors.MULTIPLE_SERVICE_FOUND)
                .variables(new ServiceErrors.MultipleServiceFound().serviceId(serviceId))
                .build();
    }

    @Override
    public String[] getTags() {
        return tags;
    }

    int getCount() {
        return Guarder.by(this._svcRepoLock.readLock()).runForResult(this._svcRepo::size);
    }

    private ILogger getLogger() {
        return this._logger == null ? this._defaultLogger : this._logger;
    }

    private void registerService(
            final IService svc) {
        final String[] svcIds = svc.getIds();
        final Dependency[] dependencies =
                svc instanceof IInjectable ? ((IInjectable) svc).getDependencies() : new Dependency[0];
        registerService(QualifiedServiceId.FROM_LOCAL, svc, svcIds, dependencies);
    }

    private void registerService(
            final String svcFrom,
            final Object svc,
            final String[] svcIds,
            final Dependency[] dependencies) {
        ArgumentChecker.notEmpty(svcFrom, "svcFrom");
        ArgumentChecker.notNull(svc, "svc");
        if (svcIds == null || svcIds.length == 0) {
            throw new InvalidArgumentException("The service id is required - {}", svc.getClass().getName());
        }

        Looper.on(svcIds)
                .map(svcId -> {
                    ServiceHolder svcHolder;
                    if (svc instanceof IPrototype) {
                        svcHolder = new PrototypeServiceHolder(svcFrom, (IPrototype) svc, svcId, dependencies, this._satisfyDecider);
                    } else if (svc instanceof IInstance) {
                        svcHolder = new InstanceServiceHolder(svcFrom, (IInstance) svc, svcId, dependencies, this._satisfyDecider);
                    } else {
                        svcHolder = new ServiceHolder(svcFrom, svc, svcId, dependencies, this._satisfyDecider);
                    }
                    return svcHolder;
                }).next(svcHolder -> {
                    if (svcHolder.getQualifiedId().isExternalService()) {
                        this._svcActivator.activateService(svcHolder);
                    }
                }).foreach(svcHolder -> {
                    List<ServiceHolder> hostSvcs = new ArrayList<>();
                    // Check whether the new register service depends on existing service
                    Guarder.by(this._svcRepoLock.readLock()).run(() -> {

                        Looper.on(this._svcRepo.values())
                                .filter(existingSvc -> svcHolder.isDependsOn(existingSvc.getQualifiedId()))
                                .foreach(hostSvcs::add);
//                                .foreach(existingSvc -> setDependency(svcHolder, existingSvc, initInstanceAttributes(svcHolder.getId())));

                    });
                    Looper.on(hostSvcs).foreach(existingSvc -> setDependency(svcHolder, existingSvc, initInstanceAttributes(svcHolder.getId())));
                    hostSvcs.clear();

                    // Check whether existing service depends on the new register service
                    Guarder.by(this._svcRepoLock.readLock()).run(() -> {
                        Looper.on(this._svcRepo.values())
                                .filter(existingSvc -> existingSvc.isDependsOn(svcHolder.getQualifiedId()))
                                .foreach(hostSvcs::add);
//                                .foreach(existingSvc -> setDependency(existingSvc, svcHolder, initInstanceAttributes(existingSvc.getId())));
                    });
                    Looper.on(hostSvcs).foreach(existingSvc -> setDependency(existingSvc, svcHolder, initInstanceAttributes(existingSvc.getId())));
                    hostSvcs.clear();

                    Guarder.by(this._svcRepoLock.writeLock()).run(() -> this._svcRepo.put(svcHolder.getId(), svcHolder));
                });

//            Looper.on(svcIds)
//                    .map(svcId -> new ServiceHolder(svcFrom, svc, svcId, dependencies, this._satisfyDecider))
//                    .next(svcHolder -> {
//                        if (svcHolder.getQualifiedId().isExternalService()) {
//                            this._svcActivator.activateService(svcHolder);
//                        }
//                    }).foreach(svcHolder -> {
//                        Guarder.by(this._svcRepoLock.readLock()).run(() -> {
//                            // Check whether the new register service depends on existing service
//                            Looper.on(this._svcRepo.values())
//                                    .filter(existingSvc -> svcHolder.isDependsOn(existingSvc.getQualifiedId()))
//                                    .foreach(existingSvc -> svcHolder.setDependency(existingSvc, this._svcActivator));
//                            // Check whether existing service depends on the new register service
//                            Looper.on(this._svcRepo.values())
//                                    .filter(existingSvc -> existingSvc.isDependsOn(svcHolder.getQualifiedId()))
//                                    .foreach(existingSvc -> existingSvc.setDependency(svcHolder, this._svcActivator));
//                        });
//                        Guarder.by(this._svcRepoLock.writeLock()).run(() -> this._svcRepo.put(svcHolder.getId(), svcHolder));
//                    });
    }

    private Map<String, ?> initInstanceAttributes(String refBy) {
        return initInstanceAttributes(new HashMap<>(), refBy);
    }

    private Map<String, ?> initInstanceAttributes(Map<String, Object> attributes, String refBy) {
        if (attributes == null) {
            attributes = new HashMap<>();
        }
        attributes.put("serveFor", refBy);
        return attributes;
    }

    private void setDependency(
            final ServiceHolder hostSvc,
            final ServiceHolder dependencySvc,
            final Map<String, ?> attributes
    ) {
        if (dependencySvc instanceof PrototypeServiceHolder) {
            // Get service instance and register it then set instance service holder
            IInstance instance = ((PrototypeServiceHolder) dependencySvc).newInstance(attributes);
            register(instance);
            ServiceHolder instanceHolder = findServiceHolder(instance.getIds()[0], QualifiedServiceId.FROM_LOCAL);
            if (instanceHolder == null) {
                throw new GeneralException("Register instance service is failed, prototype service is - {}", dependencySvc.getId());
            }
            if (! (instanceHolder instanceof InstanceServiceHolder)) {
                throw new GeneralException("Instance service should wrap by InstanceServiceHolder");
            }
            hostSvc.setInstanceDependency((InstanceServiceHolder) instanceHolder, this._svcActivator);
        } else {
            hostSvc.setDependency(dependencySvc, this._svcActivator);
        }
    }

    @Override
    public void injectObject(
            final Injection injection
    ) throws InvalidArgumentException, GeneralException {
        ArgumentChecker.notNull(injection, "injection");
        if (ISatisfyHook.class.getName().equals(injection.getId())) {
            Object injectedObj = injection.getObject();
            if (! (injectedObj instanceof ISatisfyHook)) {
                throw new InvalidArgumentException(
                        "The injected object {} can't be converted to {}",
                        injection.getObject(), ISatisfyHook.class.getName());
            }
            releaseHooks();
            ISatisfyHook hook = (ISatisfyHook) injectedObj;
            this._satisfyHooks.add(new WeakReference<>(hook));
            return;
        }
        if (ILogger.class.getName().equals(injection.getId())) {
            if (! (injection.getObject() instanceof ILogger)) {
                throw new InvalidArgumentException(
                        "The injected object {} can't be converted to {}",
                        injection.getObject(), ILogger.class.getName());
            }
            this._logger = (ILogger) injection.getObject();
            return;
        }
        if (IServiceLoader.class.getName().equals(injection.getId())) {
            if (! (injection.getObject() instanceof IServiceLoader)) {
                throw new InvalidArgumentException(
                        "The injected object {} can't be converted to {}",
                        injection.getObject(), IServiceLoader.class.getName());
            }
            IServiceLoader svcLoader = (IServiceLoader) injection.getObject();
            this._svcLoaders.put(svcLoader.getId(), svcLoader);
            this._orderedSvcLoaders.add(svcLoader);
            svcLoader.register(this._svcReadyListener);
            return;
        }
        throw new InvalidArgumentException("The Registry does not depends on service {}", injection);
    }

    @Override
    public Dependency[] getDependencies() {
        return new Dependency[] {
                new Dependency(
                        StringHelper.makeString("{}{}{}",
                                ISatisfyHook.class.getName(),
                                QualifiedServiceId.LOCATION,
                                QualifiedServiceId.FROM_LOCAL),
                        ISatisfyHook.class, false, true),
                new Dependency(
                        StringHelper.makeString("{}{}{}",
                                ILogger.class.getName(),
                                QualifiedServiceId.LOCATION,
                                QualifiedServiceId.FROM_LOCAL),
                        ILogger.class, true, false),
                new Dependency(
                        StringHelper.makeString("{}{}{}",
                                IServiceLoader.class.getName(),
                                QualifiedServiceId.LOCATION,
                                QualifiedServiceId.FROM_LOCAL),
                        IServiceLoader.class, false, true)
        };
    }

    @Override
    public boolean isOptional(
            String id
    ) throws InvalidArgumentException {
        if (ISatisfyHook.class.getName().equals(id)) {
            return true;
        }
        if (ILogger.class.getName().equals(id)) {
            return false;
        }
        if (IServiceLoader.class.getName().equals(id)) {
            return true;
        }
        throw new InvalidArgumentException("The Registry does not depends on service {}", id);
    }

    private void releaseHooks() {
        this._satisfyHooks.removeIf(it -> it.get() == null);
    }

    private final class SatisfyDecider implements ISatisfyHook {

        @Override
        public boolean isSatisfied(IServiceReference serviceRef) {
            boolean containsNull = false;
            boolean isSatisfied = true;
            for (WeakReference<ISatisfyHook> hookRef : Registry.this._satisfyHooks) {
                ISatisfyHook hook = hookRef.get();
                if (hook == null) {
                    containsNull = true;
                    continue;
                }
                isSatisfied = hook.isSatisfied(serviceRef);
                if (! isSatisfied) {
                    break;
                }
            }
            if (containsNull) {
                releaseHooks();
            }
            return isSatisfied;
        }
    }
}
