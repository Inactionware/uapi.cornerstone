/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.behavior.internal

import spock.lang.Specification
import uapi.behavior.ActionIdentify
import uapi.behavior.ActionType
import uapi.behavior.BehaviorEvent
import uapi.behavior.BehaviorException
import uapi.behavior.BehaviorExecutingEventHandler
import uapi.behavior.BehaviorFinishedEventHandler
import uapi.behavior.IAction
import uapi.behavior.IExecutionContext
import uapi.common.Repository
import uapi.event.IEventBus

/**
 * Unit test for Responsible
 */
class ResponsibleTest extends Specification {

    def 'Test create instance'() {
        when:
        def responsible = new Responsible('name', Mock(IEventBus), Mock(Repository))

        then:
        noExceptionThrown()
        responsible.name() == 'name'
    }

    def 'Test create new behavior by topic'() {
        when:
        def responsible = new Responsible('name', Mock(IEventBus), Mock(Repository))
        def bBuilder = responsible.newBehavior('bName', 'topic')

        then:
        noExceptionThrown()
        bBuilder != null
    }

    def 'Test create duplicated name behavior by topic'() {
        when:
        def responsible = new Responsible('name', Mock(IEventBus), Mock(Repository))
        responsible.newBehavior('bName', 'topic')
        responsible.newBehavior('bName', 'topic2')

        then:
        thrown(BehaviorException)
    }

    def 'Test create new behavior by type'() {
        when:
        def responsible = new Responsible('name', Mock(IEventBus), Mock(Repository))
        def bBuilder = responsible.newBehavior('bName', String.class)

        then:
        noExceptionThrown()
        bBuilder != null
    }

    def 'Test create duplicated name behavior by type'() {
        when:
        def responsible = new Responsible('name', Mock(IEventBus), Mock(Repository))
        responsible.newBehavior('bName', String.class)
        responsible.newBehavior('bName', Integer.class)

        then:
        thrown(BehaviorException)
    }

    def 'Test publish behavior to action repository'() {
        given:
        def actionId = new ActionIdentify('action', ActionType.ACTION)
        def action = Mock(IAction) {
            getId() >> new ActionIdentify('action', ActionType.ACTION)
            inputType() >> String.class
            outputType() >> String.class
            process(_, _) >> 'Out Data'
        }
        def repo = Mock(Repository) {
            get(actionId) >> action
            1 * put(_)
        }
        def eventBus = Mock(IEventBus) {
            0 * register(_)
        }

        when:
        def responsible = new Responsible('name', eventBus, repo)
        def behaviorBuilder = responsible.newBehavior('bName', String.class)
        def behavior = behaviorBuilder.then(actionId).build()

        then:
        noExceptionThrown()
    }

    def 'Test publish behavior to event bus'() {
        given:
        def actionId = new ActionIdentify('action', ActionType.ACTION)
        def action = Mock(IAction) {
            getId() >> new ActionIdentify('action', ActionType.ACTION)
            inputType() >> BehaviorEvent.class
            outputType() >> String.class
            process(_, _) >> 'Out Data'
        }
        def repo = Mock(Repository) {
            get(actionId) >> action
            0 * put(_)
        }
        def eventBus = Mock(IEventBus) {
            1 * register(_)
        }

        when:
        def responsible = new Responsible('name', eventBus, repo)
        def behaviorBuilder = responsible.newBehavior('bName', 'topic')
        def behavior = behaviorBuilder.then(actionId).build()

        then:
        noExceptionThrown()
    }

    def 'Test publish un-registered behavior'() {
        when:
        def responsible = new Responsible('name', Mock(IEventBus), Mock(Repository))
        responsible.publish(Mock(Behavior) {
            getId() >> new ActionIdentify('bName', ActionType.BEHAVIOR)
        })

        then:
        thrown(BehaviorException)
    }

    def 'Test double publish behavior'() {
        given:
        def actionId = new ActionIdentify('action', ActionType.ACTION)
        def action = Mock(IAction) {
            getId() >> new ActionIdentify('action', ActionType.ACTION)
            inputType() >> String.class
            outputType() >> String.class
            process(_, _) >> 'Out Data'
        }
        def repo = Mock(Repository) {
            get(actionId) >> action
            1 * put(_)
        }
        def eventBus = Mock(IEventBus) {
            0 * register(_)
        }

        when:
        def responsible = new Responsible('name', eventBus, repo)
        def behaviorBuilder = responsible.newBehavior('bName', String.class)
        def behavior = behaviorBuilder.then(actionId).build()
        responsible.publish((Behavior) behavior)

        then:
        thrown(BehaviorException)
    }

    def 'Test handle behavior executing event'() {
        given:
        def eventBus = Mock(IEventBus) {
            1 * register(_)
        }
        def exeHandler = Mock(BehaviorExecutingEventHandler)
        def finHandler = Mock(BehaviorFinishedEventHandler)

        when:
        def responsible = new Responsible('name', eventBus, Mock(Repository))
        responsible.on(exeHandler)
        responsible.on(finHandler)

        then:
        noExceptionThrown()
    }
}
