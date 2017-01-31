/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.behavior.internal

import spock.lang.Ignore
import spock.lang.Specification
import uapi.behavior.IResponsible
import uapi.event.IEventBus
import uapi.log.ILogger

/**
 * Unit test for ResponsibleRegistry
 */
@Ignore
class ResponsibleRegistryTest extends Specification {

    def 'Test init'() {
        given:
        ResponsibleRegistry reg = new ResponsibleRegistry()
        def eventBus = Mock(IEventBus)
        reg._eventBus = eventBus
        reg._logger = Mock(ILogger)
        reg._responsibles.add(Mock(IResponsible) {
            behaviors() >> Mock(IEventDrivenBehavior) {
                topic() >> 'event-topic'
            }
        })

        when:
        reg.init()

        then:
        noExceptionThrown()
        1 * eventBus.register(_)
    }
}
