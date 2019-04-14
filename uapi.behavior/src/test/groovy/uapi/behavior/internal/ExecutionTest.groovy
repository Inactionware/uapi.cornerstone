package uapi.behavior.internal

import spock.lang.Specification
import uapi.behavior.ActionIdentify
import uapi.behavior.ActionOutput
import uapi.behavior.ActionOutputMeta
import uapi.behavior.ActionType
import uapi.behavior.BehaviorExecutingEvent
import uapi.behavior.BehaviorFinishedEvent
import uapi.behavior.ExecutionIdentify
import uapi.behavior.IAction
import uapi.behavior.IExecutionContext

/**
 * Unit test for Execution
 */
//@Ignore
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

    def 'Test execute'() {
        given:
        def inputs = [input] as Object[]
        def action = Mock(IAction) {
            1 * process(_, _, _)
        }
        action.outputMetas() >> new ActionOutputMeta[0]
        def actionHolder = Mock(ActionHolder) {
            findNext(_) >> null
        }
        actionHolder.action() >> action
        actionHolder.inputs() >> inputs
        def behavior = Mock(Behavior) {
            getId() >> new ActionIdentify('bname', ActionType.BEHAVIOR)
            traceable() >> false
            headAction() >> actionHolder
        }

        when:
        def outMeta = new ActionOutputMeta(String.class)
        def outputs = [new ActionOutput<String>(Mock(ActionIdentify), outMeta)] as ActionOutput[]
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
        def inputs = [input] as Object[]
        def action = Mock(IAction) {
            getId() >> new ActionIdentify('aname', ActionType.ACTION)
            1 * process(_, _, _)
        }
        action.outputMetas() >> new ActionOutputMeta[0]
        def actionHolder = Mock(ActionHolder) {
            findNext(_) >> null
        }
        actionHolder.action() >> action
        actionHolder.inputs() >> inputs
        def behavior = Mock(Behavior) {
            getId() >> new ActionIdentify('bname', ActionType.BEHAVIOR)
            traceable() >> true
            headAction() >> actionHolder
        }
        def execCtx = Mock(ExecutionContext) {
            get(IExecutionContext.KEY_RESP_NAME) >> 'respName'
        }

        when:
        def outMeta = new ActionOutputMeta(String.class)
        def outputs = [new ActionOutput<String>(Mock(ActionIdentify), outMeta)] as ActionOutput[]
        def execution = new Execution(behavior, 1, null, null)
        execution.execute(inputs, outputs, execCtx)

        then:
        noExceptionThrown()
        1 * execCtx.fireEvent(_ as BehaviorExecutingEvent)
        1 * execCtx.fireEvent(_ as BehaviorFinishedEvent)

        where:
        input   | output
        'A'     | 'B'
    }
}
