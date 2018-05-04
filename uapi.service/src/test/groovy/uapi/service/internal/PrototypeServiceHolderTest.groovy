/*
 * Copyright (c) 2018. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.service.internal

import spock.lang.Specification
import uapi.service.Dependency
import uapi.service.IInstance
import uapi.service.IPrototype
import uapi.service.ISatisfyHook
import uapi.service.QualifiedServiceId

class PrototypeServiceHolderTest extends Specification {

    def 'Test create instance'() {
        when:
        def svcHolder = new PrototypeServiceHolder(from, service, svcId, [dependency] as Dependency[], satisfiyHook)

        then:
        noExceptionThrown()
        svcHolder.id == svcId
        svcHolder.service == service
        svcHolder.from == from
        svcHolder.qualifiedId == new QualifiedServiceId(svcId, from)
        ! svcHolder.isResolved()
        ! svcHolder.isInjected()
        ! svcHolder.isSatisfied()
        ! svcHolder.isActivated()

        where:
        from    | service          | svcId   | dependency       | satisfiyHook
        'local' | Mock(IPrototype) | 'svcId' | Mock(Dependency) | Mock(ISatisfyHook)
    }

    def 'Test create new instance'() {
        given:
        def instSvc = Mock(IInstance)
        def prototypeSvc = Mock(IPrototype) {
            1 * newInstance(_ as Map) >> instSvc
        }
        def svcHolder = new PrototypeServiceHolder(from, prototypeSvc, svcId, [dependency] as Dependency[], satisfiyHook)

        when:
        def inst = svcHolder.newInstance(attributes)

        then:
        noExceptionThrown()
        inst == instSvc

        where:
        from    | svcId   | dependency       | satisfiyHook          | attributes
        'local' | 'svcId' | Mock(Dependency) | Mock(ISatisfyHook)    | ['a': 'b']
    }
}
