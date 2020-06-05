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
import uapi.behavior.ActionInputMeta
import uapi.behavior.ActionOutputMeta
import uapi.behavior.ActionType
import uapi.behavior.BehaviorEvent
import uapi.behavior.BehaviorException
import uapi.behavior.BehaviorExecutingEvent
import uapi.behavior.BehaviorExecutingEventHandler
import uapi.behavior.BehaviorFinishedEvent
import uapi.behavior.BehaviorFinishedEventHandler
import uapi.behavior.IAction
import uapi.behavior.BehaviorTraceEvent
import uapi.common.Repository
import uapi.event.IEvent
import uapi.event.IEventBus
import uapi.event.IEventFinishCallback
import uapi.event.IEventHandler

/**
 * Unit test for Responsible
 */
class ResponsibleTest extends Specification {

    def 'Test create instance'() {
        when:
        def responsible = new Responsible('name', Mock(IEventBus), Mock(ActionRepository))

        then:
        noExceptionThrown()
        responsible.name() == 'name'
    }

    def 'Test create new behavior by topic'() {
        when:
        def responsible = new Responsible('name', Mock(IEventBus), Mock(ActionRepository))
        def bBuilder = responsible.newBehavior('bName', BehaviorEvent.class, 'topic')

        then:
        noExceptionThrown()
        bBuilder != null
    }

    def 'Test create duplicated name behavior by topic'() {
        when:
        def responsible = new Responsible('name', Mock(IEventBus), Mock(ActionRepository))
        responsible.newBehavior('bName', BehaviorEvent.class, 'topic')
        responsible.newBehavior('bName', BehaviorEvent.class, 'topic2')

        then:
        thrown(BehaviorException)
    }

    def 'Test create new behavior by type'() {
        when:
        def responsible = new Responsible('name', Mock(IEventBus), Mock(ActionRepository))
        def bBuilder = responsible.newBehavior('bName', String.class)

        then:
        noExceptionThrown()
        bBuilder != null
    }

    def 'Test create duplicated name behavior by type'() {
        when:
        def responsible = new Responsible('name', Mock(IEventBus), Mock(ActionRepository))
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
        }
        action.inputMetas() >> ([new ActionInputMeta(String.class)] as ActionInputMeta[])
        action.outputMetas() >> ([new ActionOutputMeta(String.class)] as ActionOutputMeta[])
        def repo = Mock(ActionRepository) {
            get(actionId, null) >> action
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
            inputMetas() >> ([new ActionInputMeta(BehaviorEvent.class)] as ActionInputMeta[])
            outputMetas() >> ([new ActionOutputMeta(String.class)] as ActionOutputMeta[])
        }
        def repo = Mock(ActionRepository) {
            get(actionId, null) >> action
            0 * put(_)
        }
        def eventBus = Mock(IEventBus) {
            1 * register(_)
        }

        when:
        def responsible = new Responsible('name', eventBus, repo)
        def behaviorBuilder = responsible.newBehavior('bName', BehaviorEvent.class, 'topic')
        def behavior = behaviorBuilder.then(actionId).build()

        then:
        noExceptionThrown()
    }

    def 'Test publish un-registered behavior'() {
        when:
        def responsible = new Responsible('name', Mock(IEventBus), Mock(ActionRepository))
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
            inputMetas() >> ([new ActionInputMeta(String.class)] as ActionInputMeta[])
            outputMetas() >> ([new ActionOutputMeta(String.class)] as ActionOutputMeta[])
        }
        def repo = Mock(ActionRepository) {
            get(actionId, null) >> action
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

    def 'Test register behavior tracer event handler'() {
        given:
        def eventBus = Mock(IEventBus) {
            1 * register(_)
        }
        def exeHandler = Mock(BehaviorExecutingEventHandler)
        def finHandler = Mock(BehaviorFinishedEventHandler)

        when:
        def responsible = new Responsible('name', eventBus, Mock(ActionRepository))
        responsible.on(exeHandler)
        responsible.on(finHandler)

        then:
        noExceptionThrown()
    }

    def 'Test invoke behavior execution event handler'() {
        given:
        def eventBus = new MockEventBus()
        def exeHandler = Mock(BehaviorExecutingEventHandler)
        def exeEvent = Mock(BehaviorExecutingEvent) {
            responsibleName() >> 'name'
        }

        when:
        def responsible = new Responsible('name', eventBus, Mock(ActionRepository))
        responsible.on(exeHandler)
        eventBus.invokeHandler(exeEvent)

        then:
        noExceptionThrown()
        1 * exeHandler.accept(exeEvent)
    }

    def 'Test invoke behavior finished event handler'() {
        given:
        def eventBus = new MockEventBus()
        def finHandler = Mock(BehaviorFinishedEventHandler)
        def finEvent = Mock(BehaviorFinishedEvent) {
            responsibleName() >> 'name'
        }

        when:
        def responsible = new Responsible('name', eventBus, Mock(ActionRepository))
        responsible.on(finHandler)
        eventBus.invokeHandler(finEvent)

        then:
        noExceptionThrown()
        1 * finHandler.accept(finEvent)
    }

    def 'Test invoke unsupported behavior event'() {
        given:
        def eventBus = new MockEventBus()
        def event = Mock(BehaviorTraceEvent) {
            responsibleName() >> 'name'
        }

        when:
        def responsible = new Responsible('name', eventBus, Mock(ActionRepository))
        responsible.on(Mock(BehaviorExecutingEventHandler))
        eventBus.invokeHandler(event)

        then:
        thrown(BehaviorException)
    }

    def 'Test invoke behavior event handler'() {
        given:
        def actionId = new ActionIdentify('action', ActionType.ACTION)
        def action = Mock(IAction) {
            getId() >> new ActionIdentify('action', ActionType.ACTION)
            inputMetas() >> ([new ActionInputMeta(String.class)] as ActionInputMeta[])
            outputMetas() >> ([new ActionOutputMeta(String.class)] as ActionOutputMeta[])
        }
        def repo = Mock(ActionRepository) {
            get(actionId, null) >> action
            1 * put(_)
        }
        def eventBus = new MockEventBus()

        when:
        def responsible = new Responsible('name', eventBus, repo)
        def behaviorBuilder = responsible.newBehavior('bName', String.class)
        def behavior = behaviorBuilder.then(actionId).build()
        eventBus.invokeHandler(Mock(BehaviorEvent))

        then:
        noExceptionThrown()
    }

    class MockEventBus implements IEventBus {

        private IEventHandler _handler

        @Override
        void fire(String topic) {

        }

        @Override
        void fire(String topic, boolean syncable) {

        }

        @Override
        void fire(IEvent event) {

        }

        @Override
        void fire(IEvent event, boolean syncable) {

        }

        @Override
        <T extends IEvent> void fire(T event, IEventFinishCallback<T> callback) {

        }

        @Override
        <T extends IEvent> void fire(T event, IEventFinishCallback<T> callback, boolean sync) {

        }

        @Override
        void register(IEventHandler eventHandler) {
            this._handler = eventHandler
        }

        @Override
        boolean unregister(IEventHandler eventHandler) {
            return false
        }

        void invokeHandler(IEvent event) {
            if (this._handler != null) {
                this._handler.handle(event)
            }
        }
    }
}
