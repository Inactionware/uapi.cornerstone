package uapi.app.internal

import spock.lang.Specification
import uapi.log.ILogger
import uapi.service.IRegistry
import uapi.service.IService

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
        def registry = Mock(IRegistry) {
            1 * deactivateServices(_)
        }
        def event = Mock(SystemShuttingDownEvent) {
            1 * applicationServices() >> [Mock(IService) {
                1 * getIds() >> ['sid']
            }]
        }
        def shutdownApp = new ShutdownApplication()
        shutdownApp._registry = registry
        shutdownApp._logger = Mock(ILogger)

        when:
        shutdownApp.shutdown(event)

        then:
        noExceptionThrown()
    }
}
