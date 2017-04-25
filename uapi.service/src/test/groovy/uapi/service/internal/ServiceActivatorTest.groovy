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
import uapi.common.IntervalTime
import uapi.service.Dependency
import uapi.service.QualifiedServiceId
import uapi.service.ServiceException

/**
 * Unit tests for ServiceActivator
 */
class ServiceActivatorTest extends Specification {

    def 'Test create instance'() {
        when:
        new ServiceActivator(Mock(IExternalServiceLoader))

        then:
        noExceptionThrown()
    }

    def 'Test activate an activated service'() {
        given:
        def svc = Mock(Object)
        def svcHolder = Mock(ServiceHolder) {
            isActivated() >> true
            getService() >> svc
        }
        def svcActivator = new ServiceActivator(Mock(IExternalServiceLoader))

        when:
        def result = svcActivator.activeService(svcHolder)

        then:
        noExceptionThrown()
        result == svc
    }

    def 'Test activate service'() {
        given:
        def svc = Mock(Object)
        def svcHolder = Mock(ServiceHolder) {
            isActivated() >>> [false, true]
            isExternalService() >> false
            getService() >> svc
            getUnactivatedServices() >> []
        }
        def svcActivator = new ServiceActivator(Mock(IExternalServiceLoader))

        when:
        def result = svcActivator.activeService(svcHolder)

        then:
        noExceptionThrown()
        result == svc
    }

    def 'Test activate external service'() {
        given:
        def svc = Mock(Object)
        def svcHolder = Mock(ServiceHolder) {
            serviceId() >> 'svc'
            isActivated() >>> [false, true]
            getService() >> svc
            getUnactivatedServices() >> [Mock(UnactivatedService) {
                serviceId() >> 'extSvc'
                isExternalService() >> true
                dependency() >> Mock(Dependency)
                isActivated() >> true
            }]
        }
        def extSvc = Mock(ServiceHolder)
        def extSvcLoader = Mock(IExternalServiceLoader) {
            loadService(_) >> extSvc
        }
        def svcActivator = new ServiceActivator(extSvcLoader)

        when:
        def result = svcActivator.activeService(svcHolder)

        then:
        noExceptionThrown()
        result == svc
    }

    def 'Test activate external service which is no return'() {
        given:
        def svc = Mock(Object)
        def svcHolder = Mock(ServiceHolder) {
            serviceId() >> 'svc'
            isActivated() >>> [false, true]
            getService() >> svc
            getUnactivatedServices() >> [Mock(UnactivatedService) {
                serviceId() >> 'extSvc'
                isExternalService() >> true
                dependency() >> Mock(Dependency)
                isActivated() >> false
            }]
        }
        def extSvcLoader = Mock(IExternalServiceLoader) {
            loadService(_) >> null
        }
        def svcActivator = new ServiceActivator(extSvcLoader)

        when:
        def result = svcActivator.activeService(svcHolder)

        then:
        noExceptionThrown()
        result == null
    }

    def 'Test activate external service which is not activated'() {
        given:
        def svc = Mock(Object)
        def svcHolder = Mock(ServiceHolder) {
            serviceId() >> 'svc'
            isActivated() >>> [false, true]
            getService() >> svc
            getUnactivatedServices() >> [Mock(UnactivatedService) {
                serviceId() >> 'extSvc'
                isExternalService() >> true
                dependency() >> Mock(Dependency)
                isActivated() >> false
            }]
        }
        def extSvc = Mock(ServiceHolder)
        def extSvcLoader = Mock(IExternalServiceLoader) {
            loadService(_) >> extSvc
        }
        def svcActivator = new ServiceActivator(extSvcLoader)

        when:
        def result = svcActivator.activeService(svcHolder)

        then:
        thrown(ServiceException)
    }

    def 'Test activate service which is timed out'() {
        given:
        def svc = Mock(Object)
        def svcHolder = Mock(ServiceHolder) {
            getQualifiedId() >> Mock(QualifiedServiceId) {
                toString() >> 'svc'
            }
            serviceId() >> 'svc'
            isActivated() >>> [false, true]
            getService() >> svc
            getUnactivatedServices() >> [Mock(UnactivatedService) {
                serviceId() >> 'extSvc'
                isExternalService() >> true
                dependency() >> Mock(Dependency)
                isActivated() >> false
            }]
        }
        def extSvc = Mock(ServiceHolder)
        def lock = new Object()
        def extSvcLoader = new IExternalServiceLoader() {
            def unlocked = false
            public ServiceHolder loadService(Dependency dep) {
                if (! unlocked) {
                    synchronized (lock) {
                        lock.wait()
                    }
                }
                return extSvc
            }
        }
        def svcActivator = new ServiceActivator(extSvcLoader)

        when:
        svcActivator.activeService(svcHolder, IntervalTime.parse('1s'))
        extSvcLoader.unlocked = true
        synchronized (lock) {
            lock.notifyAll()
        }

        then:
        thrown(ServiceException)
    }

    def 'Test activate tasks'() {
        given:
        def extDependency = Mock(Dependency) {
            getServiceId() >> Mock(QualifiedServiceId) {
                toString() >> 'extSvc'
            }
            equals(_) >> true
        }
        def extUnactivatedSvc = Mock(UnactivatedService) {
            serviceId() >> 'extSvc'
            isExternalService() >> true
            dependency() >> extDependency
            isActivated() >>> [false, false, false, true]
            equals(_) >> true
        }
        def svc = Mock(Object)
        def svcHolder = Mock(ServiceHolder) {
            serviceId() >> 'svc'
            getService() >> svc
            isActivated() >>> [false, false, true]
            getUnactivatedServices() >> [extUnactivatedSvc]
        }

        def svc2 = Mock(Object)
        def svcHolder2 = Mock(ServiceHolder) {
            serviceId() >> 'svc'
            getService() >> svc2
            isActivated() >>> [false, false, true]
            getUnactivatedServices() >> [extUnactivatedSvc]
        }

        def extSvc = Mock(ServiceHolder) {
            serviceId() >> 'extSvcId'
            isActivated() >> true
        }
        def lock = new Object()
        def extSvcLoader = new IExternalServiceLoader() {
            def unlocked = false
            public ServiceHolder loadService(Dependency dep) {
                if (! unlocked) {
                    synchronized (lock) {
                        lock.wait()
                    }
                }
                return extSvc
            }
        }
        def svcActivator = new ServiceActivator(extSvcLoader)

        when:
        def result = null
        new Thread(new Runnable() {
            @Override
            void run() {
                result = svcActivator.activeService(svcHolder)
            }
        }).start()
        def result2 = null
        new Thread(new Runnable() {
            @Override
            void run() {
                result2 = svcActivator.activeService(svcHolder2)
            }
        }).start()
        Thread.sleep(500)
        extSvcLoader.unlocked = true
        synchronized (lock) {
            lock.notifyAll()
        }
        Thread.sleep(500)

        then:
        noExceptionThrown()
        result == svc
        result2 == svc2
    }
}
