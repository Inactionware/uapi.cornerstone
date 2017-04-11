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
}
