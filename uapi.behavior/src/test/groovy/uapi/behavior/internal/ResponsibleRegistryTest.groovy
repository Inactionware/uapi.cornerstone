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
import uapi.behavior.ActionIdentify
import uapi.behavior.ActionType
import uapi.behavior.BehaviorException
import uapi.behavior.IAction
import uapi.behavior.IResponsible
import uapi.event.IEventBus
import uapi.log.ILogger

/**
 * Unit test for ResponsibleRegistry
 */
class ResponsibleRegistryTest extends Specification {

    def 'Test create instance'() {
        when:
        def respReg = new ResponsibleRegistry()

        then:
        noExceptionThrown()
        respReg.actionCount() == 0
        respReg.responsibleCount() == 0
    }

    def 'Test add action'() {
        given:
        def logger = Mock(ILogger) {
            0 * warn(_, _, _)
        }

        when:
        def respReg = new ResponsibleRegistry()
        respReg._logger = logger
        respReg.addAction(Mock(IAction) {
            getId() >> new ActionIdentify('aname', ActionType.ACTION)
        })

        then:
        noExceptionThrown()
        respReg.actionCount() == 1
        respReg.responsibleCount() == 0
    }

    def 'Test add duplicated action'() {
        given:
        def logger = Mock(ILogger) {
            1 * warn(_, _, _)
        }

        when:
        def respReg = new ResponsibleRegistry()
        respReg._logger = logger
        respReg.addAction(Mock(IAction) {
            getId() >> new ActionIdentify('aname', ActionType.ACTION)
        })
        respReg.addAction(Mock(IAction) {
            getId() >> new ActionIdentify('aname', ActionType.ACTION)
        })

        then:
        noExceptionThrown()
        respReg.actionCount() == 1
        respReg.responsibleCount() == 0
    }

    def 'Test register'() {
        given:
        def eventBus = Mock(IEventBus)

        when:
        def respReg = new ResponsibleRegistry()
        respReg._eventBus = eventBus
        def resp = respReg.register('resp')

        then:
        noExceptionThrown()
        resp != null
        respReg.actionCount() == 0
        respReg.responsibleCount() == 1
    }

    def 'Test register with duplicated name'() {
        given:
        def eventBus = Mock(IEventBus)

        when:
        def respReg = new ResponsibleRegistry()
        respReg._eventBus = eventBus
        respReg.register('resp')
        respReg.register('resp')

        then:
        thrown(BehaviorException)
        respReg.actionCount() == 0
        respReg.responsibleCount() == 1
    }

    def 'Test unregister'() {
        when:
        def respReg = new ResponsibleRegistry()
        respReg._eventBus = Mock(IEventBus)
        respReg.register('resp')
        respReg.unregister('resp')

        then:
        respReg.actionCount() == 0
        respReg.responsibleCount() == 0
    }
}
