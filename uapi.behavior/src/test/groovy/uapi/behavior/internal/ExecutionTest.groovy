package uapi.behavior.internal

import spock.lang.Specification
import uapi.behavior.ActionIdentify
import uapi.behavior.ActionType
import uapi.behavior.BehaviorExecutingEvent
import uapi.behavior.BehaviorFinishedEvent
import uapi.behavior.BehaviorFinishedEventHandler
import uapi.behavior.ExecutionIdentify
import uapi.behavior.IAction
import uapi.behavior.IExecutionContext

/**
 * Unit test for Execution
 */
class ExecutionTest extends Specification {

    def 'Test create instance'() {
        given:
        def behavior = Mock(Behavior) {
            getId() >> new ActionIdentify(bName, ActionType.BEHAVIOR)
        }

        when:
        def execution = new Execution(behavior, seq, null, null, null)

        then:
        noExceptionThrown()
        execution.id == new ExecutionIdentify(new ActionIdentify(bName, ActionType.BEHAVIOR), seq)

        where:
        bName   | seq
        'B'     | 1
    }

    def 'Test execute'() {
        given:
        def action = Mock(IAction) {
            process(input, _) >> output
        }
        def actionHolder = Mock(ActionHolder) {
            findNext(_) >> null
        }
        actionHolder.action() >> action
        def behavior = Mock(Behavior) {
            getId() >> new ActionIdentify('bname', ActionType.BEHAVIOR)
            traceable() >> false
            entranceAction() >> actionHolder
        }

        when:
        def execution = new Execution(behavior, 1, null, null, null)
        def result = execution.execute(input, Mock(ExecutionContext))

        then:
        noExceptionThrown()
        result == output

        where:
        input   | output
        'A'     | 'B'
    }

    def 'Test traceable execution'() {
        given:
        def action = Mock(IAction) {
            getId() >> new ActionIdentify('aname', ActionType.ACTION)
            process(input, _) >> output
        }
        def actionHolder = Mock(ActionHolder) {
            findNext(_) >> null
        }
        actionHolder.action() >> action
        def behavior = Mock(Behavior) {
            getId() >> new ActionIdentify('bname', ActionType.BEHAVIOR)
            traceable() >> true
            entranceAction() >> actionHolder
        }
        def execCtx = Mock(ExecutionContext) {
            get(IExecutionContext.KEY_RESP_NAME) >> 'respName'
            1 * fireEvent(_ as BehaviorExecutingEvent)
            1 * fireEvent(_ as BehaviorFinishedEvent)
        }

        when:
        def execution = new Execution(behavior, 1, null, null, null)
        def result = execution.execute(input, execCtx)

        then:
        noExceptionThrown()
        result == output

        where:
        input   | output
        'A'     | 'B'
    }
}
