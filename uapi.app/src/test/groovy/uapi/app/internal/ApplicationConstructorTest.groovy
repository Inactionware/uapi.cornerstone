package uapi.app.internal

import spock.lang.Specification
import uapi.behavior.IBehaviorBuilder
import uapi.behavior.IResponsible
import uapi.behavior.IResponsibleRegistry

/**
 * Unit tests for ApplicationConstructor
 */
class ApplicationConstructorTest extends Specification {

    def 'Test create instance'() {
        when:
        new ApplicationConstructor()

        then:
        noExceptionThrown()
    }

    def 'Test activate'() {
        given:
        def respReg = Mock(IResponsibleRegistry) {
            def startupBehaviorBuilder = Mock(IBehaviorBuilder)
            1 * startupBehaviorBuilder.then(_) >> startupBehaviorBuilder
            1 * startupBehaviorBuilder.traceable(_) >> startupBehaviorBuilder
            1 * startupBehaviorBuilder.build()
            def shutdownBehaviorBuilder = Mock(IBehaviorBuilder)
            1 * shutdownBehaviorBuilder.then(_) >> shutdownBehaviorBuilder
            1 * shutdownBehaviorBuilder.traceable(_) >> shutdownBehaviorBuilder
            1 * shutdownBehaviorBuilder.build()
            1 * register(ApplicationConstructor.RESPONSIBLE_NAME) >> Mock(IResponsible) {
                1 * newBehavior(ApplicationConstructor.BEHAVIOR_STARTUP, _, _) >> startupBehaviorBuilder
                1 * newBehavior(ApplicationConstructor.BEHAVIOR_SHUTDOWN, _, _) >> shutdownBehaviorBuilder
                1 * on(_)
            }
        }
        def appConstructor = new ApplicationConstructor()
        appConstructor._responsibleReg = respReg

        when:
        appConstructor.activate()

        then:
        noExceptionThrown()
    }
}
