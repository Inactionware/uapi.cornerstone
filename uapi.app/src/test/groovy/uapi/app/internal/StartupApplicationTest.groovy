package uapi.app.internal

import spock.lang.Specification
import uapi.app.AppException
import uapi.log.ILogger
import uapi.service.IRegistry
import uapi.service.IService

/**
 * Unit tests for StartupApplication
 */
class StartupApplicationTest extends Specification {

    def 'Test create instance'() {
        when:
        def startupApp = new ApplicationConstructor.StartupApplication()

        then:
        noExceptionThrown()
    }

    def 'Test start up but profile manager was not found'() {
        given:
        def logger = Mock(ILogger)
        def registry = Mock(IRegistry) {
            findService(ProfileManager.class) >> null
        }
        def startupApp = new ApplicationConstructor.StartupApplication()
        startupApp._logger = logger
        startupApp._registry = registry
        def event = Mock(SystemStartingUpEvent)

        when:
        startupApp.startup(event)

        then:
        thrown(AppException)
    }

    def 'Test start up'() {
        given:
        def logger = Mock(ILogger)
        def registry = Mock(IRegistry) {
            findService(ProfileManager.class) >> Mock(ProfileManager) {
                getActiveProfile() >> Mock(IProfile) {
                    1 * isAllow(_) >> true
                }
            }
        }
        def startupApp = new ApplicationConstructor.StartupApplication()
        startupApp._logger = logger
        startupApp._registry = registry
        def event = Mock(SystemStartingUpEvent) {
            applicationServices() >> [Mock(IService)]
        }

        when:
        startupApp.startup(event)

        then:
        noExceptionThrown()
        2 * logger.info(_)
    }

    def 'Test start up while contains auto active service'() {
        given:
        def logger = Mock(ILogger)
        def registry = Mock(IRegistry) {
            findService(ProfileManager.class) >> Mock(ProfileManager) {
                getActiveProfile() >> Mock(IProfile) {
                    1 * isAllow(_) >> true
                }
            }
            1 * findService('svc')
        }
        def startupApp = new ApplicationConstructor.StartupApplication()
        startupApp._logger = logger
        startupApp._registry = registry
        def service = Mock(IService) {
            getIds() >> ['svc']
            autoActive() >> true
        }
        def event = Mock(SystemStartingUpEvent) {
            applicationServices() >> [service]
        }

        when:
        startupApp.startup(event)

        then:
        noExceptionThrown()
        2 * logger.info(_)
    }
}
