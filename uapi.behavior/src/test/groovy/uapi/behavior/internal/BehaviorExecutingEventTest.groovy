package uapi.behavior.internal

import spock.lang.Ignore
import spock.lang.Specification
import uapi.behavior.BehaviorExecutingEvent

/**
 * Unit test for BehaviorExecutingEvent
 */
@Ignore
class BehaviorExecutingEventTest extends Specification {

    def 'Test create instance'() {
        when:
        def event = new BehaviorExecutingEvent(execId, aName, bName, oriData, data)

        then:
        noExceptionThrown()
        event.executionId() == execId
        event.actionId() == aName
        event.behaviorName() == bName
        event.data() == data
        event.originalData() == oriData

        where:
        topic   | aName     | bName     | data  | execId    | oriData
        'top'   | 'aname'   | 'bname'   | 'abc' | '1'       | '123'
        'top'   | 'aname'   | 'bname'   | null  | '2'       | null
    }
}
