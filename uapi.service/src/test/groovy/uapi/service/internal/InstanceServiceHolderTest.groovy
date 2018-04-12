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
import uapi.service.ISatisfyHook
import uapi.service.QualifiedServiceId

class InstanceServiceHolderTest extends Specification {

    def 'Test create instance'() {
        given:
        def instSvc = Mock(IInstance) {
            prototypeId() >> protoId
        }

        when:
        def svcHolder = new InstanceServiceHolder(from, instSvc, svcId, [dependency] as Dependency[], satisfiyHook)

        then:
        noExceptionThrown()
        svcHolder.id == svcId
        svcHolder.service == instSvc
        svcHolder.from == from
        svcHolder.qualifiedId == new QualifiedServiceId(svcId, from)
        ! svcHolder.isResolved()
        ! svcHolder.isInjected()
        ! svcHolder.isSatisfied()
        ! svcHolder.isActivated()
        svcHolder.prototypeId().id == protoId

        where:
        from    | protoId   | svcId   | dependency       | satisfiyHook
        'local' | 'proto'   | 'svcId' | Mock(Dependency) | Mock(ISatisfyHook)
    }
}
