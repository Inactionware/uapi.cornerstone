/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.app.internal

import org.junit.Ignore
import spock.lang.Specification
import uapi.app.AppException
import uapi.app.Bootstrap
import uapi.config.ICliConfigProvider
import uapi.service.IRegistry
import uapi.service.IService
import uapi.service.ITagged

/**
 * Unit tests for Bootstrap
 */
@Ignore
class BootstrapTest extends Specification {

    def 'Test start up with zero registry'() {
        given:
        Bootstrap.appSvcLoader = Mock(AppServiceLoader) {
            loadServices() >> []
        }

        when:
        Bootstrap.main([] as String[])

        then:
        thrown(AppException)
    }

    def 'Test start up with more registry'() {
        given:
        def registry = Mock(IRegistryService)
        Bootstrap.appSvcLoader = Mock(AppServiceLoader) {
            loadServices() >> [registry, registry]
        }

        when:
        Bootstrap.main([] as String[])

        then:
        thrown(AppException)
    }

    def 'Test start up when registry cannot be initialized'() {
        given:
        def registry = Mock(IRegistryService)
        registry.findService(IRegistry.class) >> null
        Bootstrap.appSvcLoader = Mock(AppServiceLoader) {
            loadServices() >> [registry]
        }

        when:
        Bootstrap.main([] as String[])

        then:
        thrown(AppException)
    }

    def 'Test start up when cli config provider service was not found'() {
        given:
        def registry = Mock(IRegistryService)
        registry.findService(IRegistry.class) >> registry
        Bootstrap.appSvcLoader = Mock(AppServiceLoader) {
            loadServices() >> [registry]
        }

        when:
        Bootstrap.main([] as String[])

        then:
        thrown(AppException)
    }

    def 'Test start up when profile manager service was not found'() {
        given:
        def registry = Mock(IRegistryService)
        registry.findService(IRegistry.class) >> registry
        registry.findService(ICliConfigProvider.class) >> Mock(ICliConfigProvider)
        Bootstrap.appSvcLoader = Mock(AppServiceLoader) {
            loadServices() >> [registry]
        }

        when:
        Bootstrap.main([] as String[])

        then:
        thrown(AppException)
    }

    def 'Test start up when application service was not found'() {
        given:
        def registry = Mock(IRegistryService)
        registry.findService(IRegistry.class) >> registry
        registry.findService(ICliConfigProvider.class) >> Mock(ICliConfigProvider)
        registry.findService(ProfileManager.class) >> Mock(ProfileManager) {
            getActiveProfile() >> Mock(IProfile) {
                isAllow() >> true
            }
        }
        Bootstrap.appSvcLoader = Mock(AppServiceLoader) {
            loadServices() >> [registry]
        }

        when:
        Bootstrap.main([] as String[])

        then:
        thrown(AppException)
    }

//    def 'Test start up'() {
//        given:
//        def cliCfg = Mock(ICliConfigProvider)
//        def profileMgr = Mock(ProfileManager) {
//            getActiveProfile() >> Mock(IProfile) {
//                isAllow() >> true
//            }
//        }
//        def app = Mock(Application)
//        def registry = Mock(IRegistryService)
//        registry.findService(IRegistry.class) >> registry
//        registry.findService(ICliConfigProvider.class) >> cliCfg
//        registry.findService(ProfileManager.class) >> profileMgr
//        registry.findService(Application.class) >> app
//        Bootstrap.appSvcLoader = Mock(AppServiceLoader) {
//            loadServices() >> [registry]
//        }
//
//        when:
//        Bootstrap.main([] as String[])
//
//        then:
//        noExceptionThrown()
//        1 * cliCfg.parse(_)
//        1 * app.startup(_)
//    }

    interface IRegistryService extends IRegistry, IService {}

    interface ITaggedService2 extends IService, ITagged {}
}
