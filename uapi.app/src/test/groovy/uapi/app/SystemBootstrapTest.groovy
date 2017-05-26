package uapi.app

import spock.lang.Specification
import uapi.Tags
import uapi.app.internal.AppServiceLoader
import uapi.app.internal.SystemShuttingDownEvent
import uapi.app.internal.SystemStartingUpEvent
import uapi.event.IEventBus
import uapi.service.IRegistry
import uapi.service.IService
import uapi.service.ITagged

/**
 * Unit tests for SystemBootstrap
 */
class SystemBootstrapTest extends Specification {

    def 'Test boot with zero registry'() {
        given:
        Bootstrap.setSvcLoader(Mock(AppServiceLoader) {
            loadServices() >> []
        })
        def bootstrap = new Bootstrap()

        when:
        bootstrap.boot()

        then:
        thrown(AppException)
    }

    def 'Test boot with more registry'() {
        given:
        def registry = Mock(IRegistryService)
        Bootstrap.setSvcLoader(Mock(AppServiceLoader) {
            loadServices() >> [registry, registry]
        })
        def bootstrap = new Bootstrap()

        when:
        bootstrap.boot()

        then:
        thrown(AppException)
    }

    def 'Test boot when registry cannot be initialized'() {
        given:
        def registry = Mock(IRegistryService)
        registry.findService(IRegistry.class) >> null
        Bootstrap.setSvcLoader(Mock(AppServiceLoader) {
            loadServices() >> [registry]
        })
        def bootstrap = new Bootstrap()

        when:
        bootstrap.boot()

        then:
        thrown(AppException)
    }

    def 'Test boot'() {
        given:
        def registry = Mock(IRegistryService)
        1 * registry.findService(IRegistry.class) >> registry
        1 * registry.findService(IEventBus.class) >> Mock(IEventBus) {
            1 * fire(_ as SystemStartingUpEvent)
            0 * fire(_ as SystemShuttingDownEvent, true)
        }
        Bootstrap.setSvcLoader(Mock(AppServiceLoader) {
            loadServices() >> [registry]
        })
        def bootstrap = new Bootstrap()

        when:
        bootstrap.boot()

        then:
        noExceptionThrown()
        bootstrap.loadConfigCount == 1
        bootstrap.beforeLaunchingCount == 1
        bootstrap.afterLaunchingCount == 1
    }

    def 'Test boot with tagged service'() {
        given:
        def registry = Mock(IRegistryService)
        def taggedSvc = Mock(ITaggedService2) {
            1 * getTags() >> ['tag']
        }
        def taggedSvc2 = Mock(ITaggedService2) {
            1 * getTags() >> [Tags.APPLICATION]
        }
        1 * registry.findService(IRegistry.class) >> registry
        1 * registry.findService(IEventBus.class) >> Mock(IEventBus) {
            1 * fire(_ as SystemStartingUpEvent)
            0 * fire(_ as SystemShuttingDownEvent, true)
        }
        Bootstrap.setSvcLoader(Mock(AppServiceLoader) {
            loadServices() >> [registry, taggedSvc, taggedSvc2]
        })
        def bootstrap = new Bootstrap()

        when:
        bootstrap.boot()

        then:
        noExceptionThrown()
        bootstrap.loadConfigCount == 1
        bootstrap.beforeLaunchingCount == 1
        bootstrap.afterLaunchingCount == 1
    }

    private final class Bootstrap extends SystemBootstrap {

        private int loadConfigCount = 0
        private int beforeLaunchingCount = 0
        private int afterLaunchingCount = 0

        static setSvcLoader(AppServiceLoader svcLoader) {
            SystemBootstrap.appSvcLoader = svcLoader
        }

        @Override
        protected void loadConfig(IRegistry registry) {
            loadConfigCount++
        }

        @Override
        protected void beforeSystemLaunching(IRegistry registry, List<IService> appSvcs) {
            beforeLaunchingCount++
        }

        @Override
        protected void afterSystemLaunching(IRegistry registry, List<IService> appSvcs) {
            afterLaunchingCount++
        }
    }

    interface IRegistryService extends IRegistry, IService {}

    interface ITaggedService2 extends IService, ITagged {}
}
