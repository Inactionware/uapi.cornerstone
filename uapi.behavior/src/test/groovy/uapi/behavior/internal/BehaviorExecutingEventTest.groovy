package uapi.behavior.internal

import spock.lang.Specification
import uapi.behavior.BehaviorExecutingEvent

/**
 * Unit test for BehaviorExecutingEvent
 */
class BehaviorExecutingEventTest extends Specification {

    def 'Test create instance'() {
        when:
        def event = new BehaviorExecutingEvent(topic, execId, aName, bName, oriData, data)

        then:
        noExceptionThrown()
        event.topic() == topic
        event.executionId() == execId
        event.actionName() == aName
        event.behaviorName() == bName
        event.data() == data
        event.originalData() == oriData

        where:
        topic   | aName     | bName     | data  | execId    | oriData
        'top'   | 'aname'   | 'bname'   | 'abc' | '1'       | '123'
        'top'   | 'aname'   | 'bname'   | null  | '2'       | null
    }
}
