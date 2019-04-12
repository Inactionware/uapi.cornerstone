package uapi.behavior.internal

import spock.lang.Ignore
import spock.lang.Specification
import uapi.behavior.ActionIdentify
import uapi.behavior.ActionOutput
import uapi.behavior.ActionType
import uapi.behavior.BehaviorExecutingEvent
import uapi.behavior.BehaviorFinishedEvent
import uapi.behavior.ExecutionIdentify
import uapi.behavior.IAction
import uapi.behavior.IExecutionContext

/**
 * Unit test for Execution
 */
@Ignore
class ExecutionTest extends Specification {

    def 'Test create instance'() {
        given:
        def behavior = Mock(Behavior) {
            getId() >> new ActionIdentify(bName, ActionType.BEHAVIOR)
        }

        when:
        def execution = new Execution(behavior, seq, null, null)

        then:
        noExceptionThrown()
        execution.id == new ExecutionIdentify(new ActionIdentify(bName, ActionType.BEHAVIOR), seq)

        where:
        bName   | seq
        'B'     | 1
    }
/// Verified here
    def 'Test execute'() {
        given:

        def action = Mock(IAction) {
            process(_, _, _) >> output
        }
        def actionHolder = Mock(ActionHolder) {
            findNext(_) >> null
        }
        actionHolder.action() >> action
        def behavior = Mock(Behavior) {
            getId() >> new ActionIdentify('bname', ActionType.BEHAVIOR)
            traceable() >> false
            headAction() >> actionHolder
        }

        when:
        def inputs = [input] as Object[]
        def outptus = [new ActionOutput<String>(Mock(ActionIdentify), output)] as ActionOutput[]
        def execution = new Execution(behavior, 1, null, null)
        execution.execute(inputs, outputs, Mock(ExecutionContext))

        then:
        noExceptionThrown()

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
            headAction() >> actionHolder
        }
        def execCtx = Mock(ExecutionContext) {
            get(IExecutionContext.KEY_RESP_NAME) >> 'respName'
            1 * fireEvent(_ as BehaviorExecutingEvent)
            1 * fireEvent(_ as BehaviorFinishedEvent)
        }

        when:
        def execution = new Execution(behavior, 1, null, null)
        def result = execution.execute(input, execCtx)

        then:
        noExceptionThrown()
        result == output

        where:
        input   | output
        'A'     | 'B'
    }
}
