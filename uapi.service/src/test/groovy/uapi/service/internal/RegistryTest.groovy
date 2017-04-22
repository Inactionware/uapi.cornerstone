/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.service.internal

import spock.lang.Ignore
import spock.lang.Specification
import uapi.GeneralException
import uapi.InvalidArgumentException
import uapi.service.Dependency
import uapi.service.IInitial
import uapi.service.IInjectable
import uapi.service.IRegistry
import uapi.service.ISatisfyHook
import uapi.service.IService
import uapi.service.IServiceLoader
import uapi.service.Injection
import uapi.service.QualifiedServiceId
import uapi.log.ILogger

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
        thrown(GeneralException)
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

//    @Ignore
//    def 'Test start'() {
//        given:
//        def svc = Mock(IInitialService) {
//            getIds() >> ['1']
//        }
//        registry._logger = Mock(ILogger)
//        registry.register(svc)
//
//        when:
//        registry.activeAll()
//
//        then:
//        1 * svc.init()
//    }

//    @Ignore
//    def 'Test start on service has dependency'() {
//        given:
//        def svc = Mock(IInjectableService) {
//            getIds() >> ['1']
//            getDependencies() >> [Mock(Dependency) {
//                getServiceId() >> Mock(QualifiedServiceId) {
//                    getId() >> '2'
//                    getFrom() >> 'Local'
//                }
//                isSingle() >> true
//                isOptional() >> false
//            }]
//        }
//        def dependSvc = Mock(IService) {
//            getIds() >> ['2']
//        }
//        registry._logger = Mock(ILogger)
//        registry.register(svc)
//        registry.register(dependSvc)
//
//        when:
//        registry.activeAll()
//
//        then:
//        1 * svc.init()
//    }

//    def 'Test start on service has dependency which is optional'() {
//        given:
//        def svc = Mock(IInjectableService) {
//            getIds() >> ['1']
//            getDependencies() >> [Mock(Dependency) {
//                getServiceId() >> Mock(QualifiedServiceId) {
//                    getId() >> '2'
//                    getFrom() >> 'Local'
//                }
//                isSingle() >> true
//                isOptional() >> true
//            }]
//        }
//        registry._logger = Mock(ILogger)
//        registry.register(svc)
//
//        when:
//        registry.activeAll()
//
//        then:
//        noExceptionThrown()
//    }

//    @Ignore
//    def 'Test start on service has dependency which is required'() {
//        given:
//        def svc = Mock(IInjectableService) {
//            getIds() >> ['1']
//            getDependencies() >> [Mock(Dependency) {
//                getServiceId() >> Mock(QualifiedServiceId) {
//                    getId() >> '2'
//                    getFrom() >> 'Local'
//                }
//                isSingle() >> true
//                isOptional() >> false
//            }]
//        }
//        def logger = Mock(ILogger)
//        registry._logger = logger
//        registry.register(svc)
//
//        when:
//        registry.activeAll()
//
//        then:
//        noExceptionThrown()
//        1 * logger.error(_ as Exception)
//    }

    @Ignore
    def 'Test start on service has dependency which load from outside'() {
        given:
        def svc = Mock(IInjectableService) {
            getIds() >> ['1']
            getDependencies() >> [Mock(Dependency) {
                getServiceId() >> Mock(QualifiedServiceId) {
                    getId() >> '2'
                    getFrom() >> 'Remote'
                }
                isSingle() >> true
                isOptional() >> false
            }]
        }
        def logger = Mock(ILogger)
        registry._logger = logger
        registry.register(svc)
        def svcLoader = Mock(IServiceLoader) {
            getPriority() >> 1
            load('2', _) >> new Object()
        }
        registry._svcLoaders.put('Remote', svcLoader)

        when:
        registry.activeAll()

        then:
        noExceptionThrown()
        registry.findService('2') != null
    }

    @Ignore
    def 'Test start on service has dependency which load from outside but no external service loader'() {
        given:
        def svc = Mock(IInjectableService) {
            getIds() >> ['1']
            getDependencies() >> [Mock(Dependency) {
                getServiceId() >> Mock(QualifiedServiceId) {
                    getId() >> '2'
                    getFrom() >> 'Remote'
                }
                isSingle() >> true
                isOptional() >> false
            }]
        }
        def logger = Mock(ILogger)
        registry._logger = logger
        registry.register(svc)

        when:
        registry.activeAll()

        then:
        noExceptionThrown()
        1 * logger.error(_ as String, _ as QualifiedServiceId, _ as String)
    }

    @Ignore
    def 'Test start on service has dependency which load from outside but failed'() {
        given:
        def svc = Mock(IInjectableService) {
            getIds() >> ['1']
            getDependencies() >> [Mock(Dependency) {
                getServiceId() >> Mock(QualifiedServiceId) {
                    getId() >> '2'
                    getFrom() >> 'Remote'
                }
                isSingle() >> true
                isOptional() >> false
            }]
        }
        def logger = Mock(ILogger)
        registry._logger = logger
        registry.register(svc)
        def svcLoader = Mock(IServiceLoader) {
            getPriority() >> 1
            load('2', _) >> null
        }
        registry._svcLoaders.put('Remote', svcLoader)

        when:
        registry.activeAll()

        then:
        noExceptionThrown()
        1 * logger.error(_ as String, _ as QualifiedServiceId, _ as String)
    }

    @Ignore
    def 'Test start on service has dependency which load from any'() {
        given:
        def svc = Mock(IInjectableService) {
            getIds() >> ['1']
            getDependencies() >> [Mock(Dependency) {
                getServiceId() >> Mock(QualifiedServiceId) {
                    getId() >> '2'
                    getFrom() >> 'Any'
                }
                isSingle() >> true
                isOptional() >> false
            }]
        }
        def logger = Mock(ILogger)
        registry._logger = logger
        registry.register(svc)
        def svcLoader = Mock(IServiceLoader) {
            getPriority() >> 1
            load('2', _) >> new Object()
        }
        registry._svcLoaders.put('Remote', svcLoader)

        when:
        registry.activeAll()

        then:
        noExceptionThrown()
        registry.findService('2') != null
    }

    @Ignore
    def 'Test start on service has dependency which load from any but failed'() {
        given:
        def svc = Mock(IInjectableService) {
            getIds() >> ['1']
            getDependencies() >> [Mock(Dependency) {
                getServiceId() >> Mock(QualifiedServiceId) {
                    getId() >> '2'
                    getFrom() >> 'Any'
                }
                isSingle() >> true
                isOptional() >> false
            }]
        }
        def logger = Mock(ILogger)
        registry._logger = logger
        registry.register(svc)
        def svcLoader = Mock(IServiceLoader) {
            getPriority() >> 1
            load('2', _) >> null
        }
        registry._svcLoaders.put('Remote', svcLoader)

        when:
        registry.activeAll()

        then:
        noExceptionThrown()
        1 * logger.error(_ as String, _ as QualifiedServiceId)
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

    static interface IInitialService extends IService, IInitial {}

    static interface IInjectableService extends IService, IInjectable, IInitial {}
}
