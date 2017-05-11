package uapi.app.internal

import spock.lang.Specification

/**
 * Unit tests for ShutdownApplication
 */
class ShutdownApplicationTest extends Specification {

    def 'Test create instance'() {
        when:
        new ShutdownApplication()

        then:
        noExceptionThrown()
    }

    def 'Test shutdown application'() {
        given:
        def event = Mock(SystemShuttingDownEvent)
        def shutdownApp = new ShutdownApplication()

        when:
        shutdownApp.shutdown(event)

        then:
        noExceptionThrown()
    }
}
