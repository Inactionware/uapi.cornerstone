/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.service.internal

import spock.lang.Specification
import uapi.InvalidArgumentException
import uapi.service.Dependency
import uapi.service.IInjectable
import uapi.service.IInstance
import uapi.service.IPrototype
import uapi.service.IRegistry
import uapi.service.ISatisfyHook
import uapi.service.IService
import uapi.service.IServiceLifecycle
import uapi.service.IServiceLoader
import uapi.service.ITagged
import uapi.service.Injection
import uapi.service.QualifiedServiceId
import uapi.log.ILogger
import uapi.service.ServiceException

/**
 * Test case for Registry
 */
class RegistryTest extends Specification {

    Registry registry

    def setup() {
        registry = new Registry()
    }

    def "Test get id"() {
        expect:
        registry.getIds() == [IRegistry.canonicalName] as String[]
    }

    def 'Test auto active'() {
        expect:
        ! registry.autoActive()
    }

    def "Register a normal service with id"() {
        when:
        registry.register(service, serviceId)

        then:
        registry.findService(serviceId) == service

        where:
        serviceId | service
        "1"       | Mock(Object)
    }

    def "Register a IService instance with id"() {
        def svc = Mock(IService) {
            getIds() >> ["1", "2"]
        }

        when:
        registry.register(svc)

        then:
        registry.findService("1") == svc
        registry.findService("2") == svc
        registry.getCount() == 2
    }

    def "Register more IService instances"() {
        def svc1 = Mock(IService) {
            getIds() >> ["1", "2"]
        }
        def svc2 = Mock(IService) {
            getIds() >> ["3", "4"]
        }

        when:
        registry.register(svc1, svc2)

        then:
        registry.findService("1") == svc1
        registry.findService("2") == svc1
        registry.findService("3") == svc2
        registry.findService("4") == svc2
        registry.getCount() == 4
    }

    def "Test Optional"() {
        expect:
        registry.isOptional(svcId) == optional

        where:
        svcId                       | optional
        ISatisfyHook.canonicalName  | true
    }

    def "Test Satisfy Invocation"() {
        def svc1 = Mock(IService) {
            getIds() >> ["1", "2"]
        }
        def svc2 = Mock(IService) {
            getIds() >> ["3", "4"]
        }

        given:
        ISatisfyHook hook = Mock(ISatisfyHook) {
            isSatisfied(_) >> true
        }
        Injection injection = Mock(Injection) {
            getId() >> ISatisfyHook.canonicalName
            getObject() >> hook
        }
        registry.injectObject(injection)

        when:
        registry.register(svc1, svc2)

        then:
        registry.findService("1") == svc1
        registry.findService("2") == svc1
        registry.findService("3") == svc2
        registry.findService("4") == svc2
        registry.getCount() == 4
    }

    def 'Test find prototype instance service'() {
        given:
        def instance = Mock(IInstance) {
            getIds() >> ['2']
            prototypeId() >> '1'
        }
        def prototype = Mock(IPrototype) {
            getIds() >> ['1']
            1 * newInstance(_ as Map) >> instance
        }

        when:
        registry.register(prototype)

        then:
        registry.getCount() == 1
        registry.findService('1', Mock(Map)) == instance
        registry.getCount() == 2
        registry.findService('2') == instance
    }

    def 'Test a service depends on a prototype service'() {
        given:
        def instance = Mock(IInstance) {
            getIds() >> ['inst']
            prototypeId() >> 'proto'
        }
        def prototype = Mock(IPrototype) {
            getIds() >> ['proto']
            1 * newInstance(_ as Map) >> instance
        }
        def svc = Mock(IInjectableService) {
            getIds() >> ['svc']
            getDependencies() >> [Mock(Dependency) {
                getServiceId() >> Mock(QualifiedServiceId) {
                    getId() >> 'proto'
                    getFrom() >> 'Local'
                    isExternalService() >> false
                    isAssignTo(_ as QualifiedServiceId) >> true
                }
                getServiceType() >> IPrototype.class
                isSingle() >> true
                isOptional() >> false
            }]
            1 * injectObject(_)
        }

        when:
        registry.register(prototype, svc)
        def rtnSvc = registry.findService('svc')

        then:
        noExceptionThrown()
        registry.getCount() == 3
        rtnSvc == svc

    }

    def 'Test find service by id and from'() {
        def svc1 = Mock(IService) {
            getIds() >> ["1", "2"]
        }
        def svc2 = Mock(IService) {
            getIds() >> ["3", "4"]
        }

        given:
        ISatisfyHook hook = Mock(ISatisfyHook) {
            isSatisfied(_) >> true
        }
        Injection injection = Mock(Injection) {
            getId() >> ISatisfyHook.canonicalName
            getObject() >> hook
        }
        registry.injectObject(injection)

        when:
        registry.register(svc1, svc2)

        then:
        registry.findService("1", QualifiedServiceId.FROM_LOCAL) == svc1
        registry.findService("2", QualifiedServiceId.FROM_LOCAL) == svc1
        registry.findService("3", QualifiedServiceId.FROM_LOCAL) == svc2
        registry.findService("4", QualifiedServiceId.FROM_LOCAL) == svc2
        registry.getCount() == 4
    }

    def 'Test find absent service'() {
        when:
        def svc = registry.findService(String.class)

        then:
        thrown(ServiceException)
        svc == null
    }

    def 'Test find single service but found multiple service'() {
        given:
        def svc1 = Mock(IService) {
            getIds() >> ['1']
        }
        def svc2 = Mock(IService) {
            getIds() >> ['1']
        }
        registry.register(svc1, svc2)

        when:
        registry.findService('1')

        then:
        thrown(ServiceException)
    }

    def 'Test find multiple service'() {
        given:
        def svc1 = Mock(IService) {
            getIds() >> [String.canonicalName]
        }
        def svc2 = Mock(IService) {
            getIds() >> [String.canonicalName]
        }
        registry.register(svc1, svc2)

        when:
        def svcs = registry.findServices(String.class)

        then:
        svcs.size() == 2
        svcs == [svc1, svc2]
    }

    def 'Test get tags'() {
        expect:
        registry.getTags() == ['Registry'] as String[]
    }

    def 'Test inject ISatisfyHook'() {
        given:
        def injection = Mock(Injection) {
            getId() >> ISatisfyHook.canonicalName
            getObject() >> Mock(ISatisfyHook)
        }

        when:
        registry.injectObject(injection)

        then:
        registry._satisfyHooks.size() > 0
    }

    def 'Test inject ISatisfyHook with incorrect type'() {
        given:
        def injection = Mock(Injection) {
            getId() >> ISatisfyHook.canonicalName
            getObject() >> Mock(IService)
        }

        when:
        registry.injectObject(injection)

        then:
        thrown(InvalidArgumentException)
    }

    def 'Test inject ILogger'() {
        given:
        def injection = Mock(Injection) {
            getId() >> ILogger.canonicalName
            getObject() >> Mock(ILogger)
        }

        when:
        registry.injectObject(injection)

        then:
        noExceptionThrown()
        registry._logger != null
    }

    def 'Test inject ILogger with incorrect type'() {
        given:
        def injection = Mock(Injection) {
            getId() >> ILogger.canonicalName
            getObject() >> Mock(IService)
        }

        when:
        registry.injectObject(injection)

        then:
        thrown(InvalidArgumentException)
    }

    def 'Test inject IServiceLoader'() {
        given:
        def injection = Mock(Injection) {
            getId() >> IServiceLoader.canonicalName
            getObject() >> Mock(IServiceLoader)
        }

        when:
        registry.injectObject(injection)

        then:
        noExceptionThrown()
        registry._svcLoaders.size() > 0
    }

    def 'Test inject IServiceLoader with incorrect type'() {
        given:
        def injection = Mock(Injection) {
            getId() >> IServiceLoader.canonicalName
            getObject() >> Mock(IService)
        }

        when:
        registry.injectObject(injection)

        then:
        thrown(InvalidArgumentException)
    }

    def 'Test inject unsupported object'() {
        given:
        def injection = Mock(Injection) {
            getId() >> IService.canonicalName
        }

        when:
        registry.injectObject(injection)

        then:
        thrown(InvalidArgumentException)
        0 * injection.getObject()
    }

    def 'Test get dependencies'() {
        when:
        def dependencies = registry.getDependencies()

        then:
        dependencies[idx].serviceId.toString() == svcId
        dependencies[idx].serviceType == svcType
        dependencies[idx].isSingle() == isSingle
        dependencies[idx].isOptional() == isOption

        where:
        idx | svcId                                 | svcType               | isSingle  | isOption
        0   | ISatisfyHook.class.name + '@Local'    | ISatisfyHook.class    | false     | true
        1   | ILogger.class.name + '@Local'         | ILogger.class         | true      | false
        2   | IServiceLoader.class.name + '@Local'  | IServiceLoader.class  | false     | true
    }

    def 'Test is optional'() {
        expect:
        registry.isOptional(ISatisfyHook.class.name)
        ! registry.isOptional(ILogger.class.name)
        registry.isOptional(IServiceLoader.class.name)
    }

    def 'Test is optional with incorrect type'() {
        when:
        registry.isOptional(id)

        then:
        thrown(InvalidArgumentException)

        where:
        id                      | placeholder
        '1'                     | null
        IService.class.name     | null
    }

    def 'Test find service by specific external service loader'() {
        given:
        def svc = Mock(IInjectableService) {
            getIds() >> ['1']
            getDependencies() >> [Mock(Dependency) {
                getServiceId() >> Mock(QualifiedServiceId) {
                    getId() >> '2'
                    getFrom() >> 'Remote'
                    isExternalService() >> true
                }
                getServiceType() >> String.class
                isSingle() >> true
                isOptional() >> false
            }]
        }
        def logger = Mock(ILogger)
        registry._logger = logger
        registry.register(svc)
        def svcLoader = Mock(IServiceLoader) {
            getPriority() >> 1
            load('2', _ as Class) >> Mock(ServiceHolder) {
                isActivated() >> true
            }
        }
        registry._svcLoaders.put('Remote', svcLoader)

        when:
        registry.findService('1')

        then:
        noExceptionThrown()
        registry.findService('2') != null
    }

    def 'Test find service by specific external service loader but no such service loader'() {
        given:
        def svc = Mock(IInjectableService) {
            getIds() >> ['1']
            getDependencies() >> [Mock(Dependency) {
                getServiceId() >> Mock(QualifiedServiceId) {
                    getId() >> '2'
                    getFrom() >> 'Remote'
                    isExternalService() >> true
                }
                getServiceType() >> String.class
                isSingle() >> true
                isOptional() >> false
            }]
        }
        def logger = Mock(ILogger)
        registry._logger = logger
        registry.register(svc)

        when:
        def found = registry.findService('1')
        def found2 = registry.findService('2')

        then:
        thrown(ServiceException)
        found == null
        found2 == null
    }

    def 'Test find service by specific external service loader but service loader load failed'() {
        given:
        def svc = Mock(IInjectableService) {
            getIds() >> ['1']
            getDependencies() >> [Mock(Dependency) {
                getServiceId() >> Mock(QualifiedServiceId) {
                    getId() >> '2'
                    getFrom() >> 'Remote'
                    isExternalService() >> true
                }
                getServiceType() >> String.class
                isSingle() >> true
                isOptional() >> false
            }]
        }
        def logger = Mock(ILogger)
        registry._logger = logger
        registry.register(svc)
        def svcLoader = Mock(IServiceLoader) {
            getPriority() >> 1
        }
        registry._svcLoaders.put('Remote', svcLoader)

        when:
        def found = registry.findService('1')
        def found2 = registry.findService('2')

        then:
        thrown(ServiceException)
        found == null
        found2 == null
    }

    def 'Test find service by any external service loader'() {
        given:
        def svc = Mock(IInjectableService) {
            getIds() >> ['1']
            getDependencies() >> [Mock(Dependency) {
                getServiceId() >> Mock(QualifiedServiceId) {
                    getId() >> '2'
                    getFrom() >> 'Any'
                    isExternalService() >> true
                }
                getServiceType() >> String.class
                isSingle() >> true
                isOptional() >> false
            }]
        }
        def logger = Mock(ILogger)
        registry._logger = logger
        registry.register(svc)
        def svcLoader = Mock(IServiceLoader) {
            getPriority() >> 1
            load('2', _ as Class) >> Mock(ServiceHolder) {
                isActivated() >> true
            }
        }
        registry._orderedSvcLoaders.add(svcLoader)

        when:
        registry.findService('1')

        then:
        noExceptionThrown()
        registry.findService('2') != null
    }

    def 'Test find service form more external service loader'() {
        given:
        def svc = Mock(IInjectableService) {
            getIds() >> ['1']
            getDependencies() >> [Mock(Dependency) {
                getServiceId() >> Mock(QualifiedServiceId) {
                    getId() >> '2'
                    getFrom() >> 'Any'
                    isExternalService() >> true
                }
                getServiceType() >> String.class
                isSingle() >> true
                isOptional() >> false
            }]
        }
        def logger = Mock(ILogger)
        registry._logger = logger
        registry.register(svc)
        def svcLoader1 = new IServiceLoader() {
            public String getId() { return 'Test' }
            public int getPriority() { return 1 }
            public <Object> Object load(String serviceId, Class<?> serviceType) { return null }
            public void register(IServiceLoader.IServiceReadyListener listener) {}
        }
        def svcLoaderInjection1 = Mock(Injection) {
            getId() >> IServiceLoader.name
            getObject() >> svcLoader1
        }
        def mocksvcHolder = Mock(ServiceHolder) {
            isActivated() >> true
        }
        def svcLoader2 = new IServiceLoader() {
            public String getId() { return 'Remote' }
            public int getPriority() { return 2 }
            public <ServiceHolder> ServiceHolder load(String serviceId, Class<?> serviceType) {
                return mocksvcHolder
            }
            public void register(IServiceLoader.IServiceReadyListener listener) {}
        }
        def svcLoaderInjection2 = Mock(Injection) {
            getId() >> IServiceLoader.name
            getObject() >> svcLoader2
        }

        registry.injectObject(svcLoaderInjection1)
        registry.injectObject(svcLoaderInjection2)

        when:
        registry.findService('1')

        then:
        noExceptionThrown()
        registry.findService('2') != null
    }

    def 'Test find service but no any external service loader can load it'() {
        given:
        def svc = Mock(IInjectableService) {
            getIds() >> ['1']
            getDependencies() >> [Mock(Dependency) {
                getServiceId() >> Mock(QualifiedServiceId) {
                    getId() >> '2'
                    getFrom() >> 'Any'
                    isExternalService() >> true
                }
                getServiceType() >> String.class
                isSingle() >> true
                isOptional() >> false
            }]
        }
        def logger = Mock(ILogger)
        registry._logger = logger
        registry.register(svc)
        def svcLoader1 = new IServiceLoader() {
            public String getId() { return 'Test' }
            public int getPriority() { return 1 }
            public <Object> Object load(String serviceId, Class<?> serviceType) { return null }
            public void register(IServiceLoader.IServiceReadyListener listener) {}
        }
        def svcLoaderInjection1 = Mock(Injection) {
            getId() >> IServiceLoader.name
            getObject() >> svcLoader1
        }
        def svcLoader2 = new IServiceLoader() {
            public String getId() { return 'Remote' }
            public int getPriority() { return 2 }
            public <Object> Object load(String serviceId, Class<?> serviceType) { return null }
            public void register(IServiceLoader.IServiceReadyListener listener) {}
        }
        def svcLoaderInjection2 = Mock(Injection) {
            getId() >> IServiceLoader.name
            getObject() >> svcLoader2
        }

        registry.injectObject(svcLoaderInjection1)
        registry.injectObject(svcLoaderInjection2)

        when:
        def found = registry.findService('1')
        def found2 = registry.findService('2')

        then:
        thrown(ServiceException)
        found == null
        found2 == null
    }

    def 'Test activate tagged service'() {
        given:
        def svc = Mock(TaggedService) {
            getIds() >> ['1']
            getTags() >> ['tag']
            1 * onActivate()
            0 * onDeactivate()
            0 * onDependencyInject(_, _)
        }
        def logger = Mock(ILogger)
        registry._logger = logger
        registry.register(svc)

        when:
        registry.activateTaggedService('tag')

        then:
        noExceptionThrown()
    }

    def 'Test deactivate tagged service'() {
        given:
        def svc = Mock(TaggedService) {
            getIds() >> ['1']
            getTags() >> ['tag']
            1 * onActivate()
            1 * onDeactivate()
            0 * onDependencyInject(_, _)
        }
        def logger = Mock(ILogger)
        registry._logger = logger
        registry.register(svc)

        when:
        registry.activateTaggedService('tag')
        registry.deactivateTaggedService('tag')

        then:
        noExceptionThrown()
    }

    def 'Test deactivate service by id'() {
        given:
        def svc = Mock(TaggedService) {
            getIds() >> ['1']
            getTags() >> []
            1 * onActivate()
            1 * onDeactivate()
            0 * onDependencyInject(_, _)
        }
        def logger = Mock(ILogger)
        registry._logger = logger
        registry.register(svc)
        registry.findService('1')

        when:
        registry.deactivateServices(['1'] as String[])

        then:
        noExceptionThrown()
    }

    static interface IInitialService extends IService {}

    static interface IInjectableService extends IService, IInjectable {}

    static interface TaggedService extends IService, ITagged, IServiceLifecycle {}
}
