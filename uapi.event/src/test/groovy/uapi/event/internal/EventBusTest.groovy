/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.event.internal

import spock.lang.Specification
import uapi.common.IAttributed
import uapi.event.IAttributedEventHandler
import uapi.event.IEvent
import uapi.event.IEventFinishCallback
import uapi.event.IEventHandler
import uapi.log.ILogger

class EventBusTest extends Specification{

    def 'Test fire when no event handler'() {
        given:
        IEvent event = Mock(IEvent) {
            topic() >> eventTopic
        }
        EventBus eventBus = new EventBus()
        eventBus._logger = Mock(ILogger)
        eventBus.init()

        when:
        eventBus.fire(event)
        eventBus.destroy()

        then:
        noExceptionThrown()

        where:
        eventTopic  | none
        'Topic'     | null
    }

    def 'Test fire to one handler'() {
        given:
        IEvent event = Mock(IEvent) {
            topic() >> eventTopic
        }
        IEventHandler handler = Mock(IEventHandler) {
            topic() >> eventTopic
        }
        EventBus eventBus = new EventBus()
        eventBus.init()
        eventBus.register(handler)

        when:
        eventBus.fire(event)
        eventBus.destroy()

        then:
        noExceptionThrown()
        1 * handler.handle(event)

        where:
        eventTopic  | none
        'Topic'     | null
    }

    def 'Test fire to more handler'() {
        given:
        IEvent event = Mock(IEvent) {
            topic() >> eventTopic
        }
        IEventHandler handler1 = Mock(IEventHandler) {
            topic() >> eventTopic
        }
        IEventHandler handler2 = Mock(IEventHandler) {
            topic() >> eventTopic
        }
        EventBus eventBus = new EventBus()
        eventBus.init()
        eventBus.register(handler1)
        eventBus.register(handler2)

        when:
        eventBus.fire(event)
        Thread.currentThread().sleep(200)
        eventBus.destroy()

        then:
        noExceptionThrown()
        1 * handler1.handle(event)
        1 * handler2.handle(event)

        where:
        eventTopic  | none
        'Topic'     | null
    }

    def 'Test fire event with sync option'() {
        given:
        IEvent event = Mock(IEvent) {
            topic() >> eventTopic
        }
        IEventHandler handler = Mock(IEventHandler) {
            topic() >> eventTopic
        }
        EventBus eventBus = new EventBus()
        eventBus.init()
        eventBus.register(handler)

        when:
        eventBus.fire(event, true)

        then:
        noExceptionThrown()
        1 * handler.handle(event)

        where:
        eventTopic  | none
        'Topic'     | null
    }

    def 'Test unregistered handler'() {
        given:
        IEventHandler handler = Mock(IEventHandler) {
            topic() >> eventTopic
        }
        EventBus eventBus = new EventBus()
        eventBus.init()
        eventBus.register(handler)

        when:
        boolean success = eventBus.unregister(handler)

        then:
        noExceptionThrown()
        success

        where:
        eventTopic  | none
        'Topic'     | null
    }

    def 'Test event finish callback'() {
        given:
        IEvent event = Mock(IEvent) {
            topic() >> eventTopic
        }
        IEventHandler handler = Mock(IEventHandler) {
            topic() >> eventTopic
        }
        IEventFinishCallback callback = Mock(IEventFinishCallback)
        EventBus eventBus = new EventBus()
        eventBus.init()
        eventBus.register(handler)

        when:
        eventBus.fire(event, callback)
        eventBus.destroy()

        then:
        noExceptionThrown()
        1 * handler.handle(event)
        1 * callback.callback(event)

        where:
        eventTopic  | none
        'Topic'     | null
    }

    def 'Test event finish callback by multiple handler'() {
        given:
        IEvent event = Mock(IEvent) {
            topic() >> eventTopic
        }
        IEventHandler handler1 = Mock(IEventHandler) {
            topic() >> eventTopic
        }
        IEventHandler handler2 = Mock(IEventHandler) {
            topic() >> eventTopic
        }
        IEventFinishCallback callback = Mock(IEventFinishCallback)
        EventBus eventBus = new EventBus()
        eventBus.init()
        eventBus.register(handler1)
        eventBus.register(handler2)

        when:
        eventBus.fire(event, callback)
        Thread.sleep(500)
        eventBus.destroy()

        then:
        noExceptionThrown()
        1 * handler1.handle(event)
        1 * handler2.handle(event)
        1 * callback.callback(event)

        where:
        eventTopic  | none
        'Topic'     | null
    }

    def 'Test find attributed handlers'() {
        given:
        IEventHandler handler = Mock(IAttributedEventHandler) {
            1 * getAttributes() >> [1: '1']
            topic() >> eventTopic
        }
        IEvent event = Mock(IAttributedEvent) {
            1 * contains([1: '1']) >> true
            topic() >> eventTopic
        }
        EventBus eventBus = new EventBus()
        eventBus.register(handler)

        when:
        def found = eventBus.findHandlers(event)

        then:
        found.size() == 1
        found.get(0) == handler

        where:
        eventTopic  | none
        'Topic'     | null
    }

    interface IAttributedEvent extends IEvent, IAttributed {}
}
