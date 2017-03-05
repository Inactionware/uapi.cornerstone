package uapi.behavior.internal

import spock.lang.Specification
import uapi.behavior.BehaviorEvent
import uapi.behavior.IBehaviorTraceEvent
import uapi.behavior.Scope
import uapi.event.IEvent
import uapi.event.IEventBus

/**
 * Unit test for ExecutionContext
 */
class ExecutionContextTest extends Specification {

    def 'Test create instance'() {
        when:
        def ctx = new ExecutionContext(Mock(IEventBus))

        then:
        noExceptionThrown()
    }

    def 'Test put item in behavior scope'() {
        when:
        def ctx = new ExecutionContext(Mock(IEventBus))
        ctx.put(itemName, item, Scope.BEHAVIOR)

        then:
        ctx.get(itemName) == item

        where:
        itemName    | item
        'a'         | 'b'
    }

    def 'Test put item in global scope'() {
        when:
        def ctx = new ExecutionContext(Mock(IEventBus))
        ctx.put(itemName, item, Scope.GLOBAL)

        then:
        ctx.get(itemName) == item

        where:
        itemName    | item
        'a'         | 'b'
    }

    def 'Test put item in both behavior and global scope'() {
        when:
        def ctx = new ExecutionContext(Mock(IEventBus))
        ctx.put(bitemName, bItem, Scope.BEHAVIOR)
        ctx.put(gItemName, gItem, Scope.GLOBAL)

        then:
        ctx.get(itemName) == item

        where:
        bitemName   | gItemName | bItem     | gItem     | itemName  | item
        '1'         | '1'       | 'b'       | 'g'       | '1'       | 'b'
        '1'         | '2'       | 'b'       | 'g'       | '2'       | 'g'
    }

    def 'Test put multiple items()'() {
        when:
        def ctx = new ExecutionContext(Mock(IEventBus))
        ctx.put(bItem, Scope.BEHAVIOR)
        ctx.put(gItem, Scope.GLOBAL)

        then:
        ctx.get(itemName) == item

        where:
        bItem               | gItem             | itemName  | item
        [b: '1']            | [g: '2']          | 'b'       | '1'
        [b: '1']            | [g: '2']          | 'g'       | '2'
        [a: '1']            | [a: '2']          | 'a'       | '1'
        [a: '1', b : '3']   | [a: '1']          | 'a'       | '1'
        [a: '1']            | [a: '1', g: '3']  | 'g'       | '3'
    }

    def 'Test fire behavior trace event'() {
        given:
        def eventBus = Mock(IEventBus) {
            1 * fire(_ as IBehaviorTraceEvent)
            0 * fire( _ as BehaviorEvent)
        }

        when:
        def ctx = new ExecutionContext(eventBus)
        ctx.fireEvent(Mock(IBehaviorTraceEvent))

        then:
        noExceptionThrown()
    }

    def 'Test fire behavior event'() {
        given:
        def eventBus = Mock(IEventBus) {
            0 * fire(_ as IBehaviorTraceEvent)
            1 * fire(_ as BehaviorEvent)
        }

        when:
        def ctx = new ExecutionContext(eventBus)
        ctx.fireEvent(Mock(BehaviorEvent))

        then:
        noExceptionThrown()
    }
}
