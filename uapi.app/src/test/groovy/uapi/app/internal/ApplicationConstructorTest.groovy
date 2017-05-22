package uapi.app.internal

import spock.lang.Ignore
import spock.lang.Specification
import uapi.app.AppException
import uapi.app.AppStartupEvent
import uapi.behavior.BehaviorException
import uapi.behavior.BehaviorExecutingEventHandler
import uapi.behavior.BehaviorFinishedEvent
import uapi.behavior.BehaviorFinishedEventHandler
import uapi.behavior.IBehaviorBuilder
import uapi.behavior.IResponsible
import uapi.behavior.IResponsibleRegistry
import uapi.event.IEvent
import uapi.log.ILogger

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

    @Ignore
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

    @Ignore
    def 'Test startup behavior finished event handler'() {
        given:
        def bBuilder = Mock(IBehaviorBuilder)
        bBuilder.then(_) >> bBuilder
        bBuilder.traceable(_) >> bBuilder
        bBuilder.onSuccess(_) >> bBuilder
        bBuilder.onFailure(_) >> bBuilder
        def mockResp = new MockResponsible(bBuilder)
        def respReg = Mock(IResponsibleRegistry) {
            1 * register(ApplicationConstructor.RESPONSIBLE_NAME) >> mockResp
        }
        def appConstructor = new ApplicationConstructor()
        appConstructor._responsibleReg = respReg
        appConstructor._logger = Mock(ILogger)
        appConstructor.activate()

        when:
        def event = mockResp._finishedHandler.accept(Mock(BehaviorFinishedEvent) {
            1 * behaviorName() >> ApplicationConstructor.BEHAVIOR_STARTUP
        })

        then:
        noExceptionThrown()
        event != null
        event instanceof AppStartupEvent
    }

    @Ignore
    def 'Test shutdown behavior finished event handler'() {
        given:
        def bBuilder = Mock(IBehaviorBuilder)
        bBuilder.then(_) >> bBuilder
        bBuilder.traceable(_) >> bBuilder
        def mockResp = new MockResponsible(bBuilder)
        def respReg = Mock(IResponsibleRegistry) {
            1 * register(ApplicationConstructor.RESPONSIBLE_NAME) >> mockResp
        }
        def appConstructor = new ApplicationConstructor()
        appConstructor._responsibleReg = respReg
        appConstructor._logger = Mock(ILogger)
        appConstructor.activate()

        when:
        def event = mockResp._finishedHandler.accept(Mock(BehaviorFinishedEvent) {
            3 * behaviorName() >> ApplicationConstructor.BEHAVIOR_SHUTDOWN
        })

        then:
        noExceptionThrown()
        event == null
    }

    @Ignore
    def 'Test unsupported behavior event'() {
        given:
        def bBuilder = Mock(IBehaviorBuilder)
        bBuilder.then(_) >> bBuilder
        bBuilder.traceable(_) >> bBuilder
        def mockResp = new MockResponsible(bBuilder)
        def respReg = Mock(IResponsibleRegistry) {
            1 * register(ApplicationConstructor.RESPONSIBLE_NAME) >> mockResp
        }
        def appConstructor = new ApplicationConstructor()
        appConstructor._responsibleReg = respReg
        appConstructor._logger = Mock(ILogger)
        appConstructor.activate()

        when:
        def event = mockResp._finishedHandler.accept(Mock(BehaviorFinishedEvent) {
            4 * behaviorName() >> 'test behavior'
        })

        then:
        thrown(AppException)
        event == null
    }

    class MockResponsible implements IResponsible {

        BehaviorFinishedEventHandler _finishedHandler

        IBehaviorBuilder _behavior

        MockResponsible(IBehaviorBuilder bBuilder) {
            this._behavior = bBuilder
        }

        @Override
        String name() {
            return 'respName'
        }

        @Override
        IBehaviorBuilder newBehavior(String name, String topic) throws BehaviorException {
            return this._behavior
        }

        @Override
        IBehaviorBuilder newBehavior(String name, Class<? extends IEvent> eventType, String topic) throws BehaviorException {
            return this._behavior
        }

        @Override
        IBehaviorBuilder newBehavior(String name, Class<?> type) throws BehaviorException {
            return this._behavior
        }

        @Override
        void on(BehaviorExecutingEventHandler handler) {

        }

        @Override
        void on(BehaviorFinishedEventHandler handler) {
            this._finishedHandler = handler
        }
    }
}
