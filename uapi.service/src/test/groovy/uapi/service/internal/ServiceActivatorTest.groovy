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
import uapi.GeneralException
import uapi.service.Dependency
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
        thrown(ServiceException)
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
}
