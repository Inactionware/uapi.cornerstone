package uapi.service.internal

import spock.lang.Specification
import uapi.service.Dependency
import uapi.service.IInjectable
import uapi.service.ISatisfyHook
import uapi.service.IService
import uapi.service.IServiceFactory
import uapi.service.IServiceLifecycle
import uapi.service.ITagged
import uapi.service.Injection
import uapi.service.QualifiedServiceId
import uapi.service.ServiceException

/**
 * Unit tests for ServiceHolder
 */
class ServiceHolderTest extends Specification {

    def 'Test create instance'() {
        when:
        def svcHolder = new ServiceHolder(from, service, svcId, [dependency] as Dependency[], satisfiyHook)

        then:
        noExceptionThrown()
        svcHolder.id == svcId
        svcHolder.service == service
        svcHolder.from == from
        svcHolder.qualifiedId == new QualifiedServiceId(svcId, from)
        ! svcHolder.isResolved()
        ! svcHolder.isInjected()
        ! svcHolder.isSatisfied()
        ! svcHolder.isActivated()

        where:
        from    | service       | svcId     | dependency        | satisfiyHook
        'local' | Mock(Object)  | 'svcId'   | Mock(Dependency)  | Mock(ISatisfyHook)
    }

    def 'Test create instance without dependency'() {
        when:
        def svcHolder = new ServiceHolder(from, service, svcId, satisfiyHook)
        svcHolder.id == svcId
        svcHolder.service == service
        svcHolder.from == from
        svcHolder.qualifiedId == new QualifiedServiceId(svcId, from)
        ! svcHolder.isResolved()
        ! svcHolder.isInjected()
        ! svcHolder.isSatisfied()
        ! svcHolder.isActivated()

        then:
        noExceptionThrown()

        where:
        from    | service       | svcId     | satisfiyHook
        'local' | Mock(Object)  | 'svcId'   | Mock(ISatisfyHook)
    }

    def 'Test set dependency which is not its dependency'() {
        given:
        def dependency = Mock(Dependency) {
            getServiceId() >> Mock(QualifiedServiceId) {
                getId() >> 'depId'
            }
        }
        def svc = Mock(IInjectable)
        def svcHolder = new ServiceHolder(from, svc, svcId, [dependency] as Dependency[], satisfiyHook)
        def depSvcHolder = Mock(ServiceHolder) {
            getQualifiedId() >> Mock(QualifiedServiceId) {
                toString() >> 'dep2Id'
            }
        }
        def svcActivator = Mock(ServiceActivator)

        when:
        svcHolder.setDependency(depSvcHolder, svcActivator)

        then:
        thrown(ServiceException)
        ! svcHolder.isResolved()
        ! svcHolder.isInjected()
        ! svcHolder.isSatisfied()
        ! svcHolder.isActivated()

        where:
        from    | svcId     | satisfiyHook
        'local' | 'svcId'   | Mock(ISatisfyHook)
    }

    def 'Test set dependency'() {
        given:
        def dependency = Mock(Dependency) {
            getServiceId() >> Mock(QualifiedServiceId) {
                getId() >> 'depId'
            }
        }
        def svc = Mock(IInjectable)
        def svcHolder = new ServiceHolder(from, svc, svcId, [dependency] as Dependency[], satisfiyHook)
        def depSvcHolder = Mock(ServiceHolder) {
            getQualifiedId() >> Mock(QualifiedServiceId) {
                toString() >> 'depId'
                1 * isAssignTo(_) >> true
            }
        }
        def svcActivator = Mock(ServiceActivator)

        when:
        svcHolder.setDependency(depSvcHolder, svcActivator)

        then:
        noExceptionThrown()
        ! svcHolder.isResolved()
        ! svcHolder.isInjected()
        ! svcHolder.isSatisfied()
        ! svcHolder.isActivated()

        where:
        from    | svcId     | satisfiyHook
        'local' | 'svcId'   | Mock(ISatisfyHook)
    }

    def 'Test is depends on by qualified id'() {
        given:
        def dependency = Mock(Dependency) {
            getServiceId() >> new QualifiedServiceId('depId', QualifiedServiceId.FROM_ANY)
        }
        def svc = Mock(IInjectable)
        def svcHolder = new ServiceHolder(from, svc, svcId, [dependency] as Dependency[], satisfiyHook)

        when:
        def isDependsOn = svcHolder.isDependsOn(new QualifiedServiceId('depId', QualifiedServiceId.FROM_LOCAL))

        then:
        noExceptionThrown()
        isDependsOn
        ! svcHolder.isResolved()
        ! svcHolder.isInjected()
        ! svcHolder.isSatisfied()
        ! svcHolder.isActivated()

        where:
        from    | svcId     | satisfiyHook
        'local' | 'svcId'   | Mock(ISatisfyHook)
    }

    def 'Test is depends on by dependency'() {
        given:
        def dependency = Mock(Dependency) {
            getServiceId() >> new QualifiedServiceId('depId', QualifiedServiceId.FROM_ANY)
        }
        def svc = Mock(IInjectable)
        def svcHolder = new ServiceHolder(from, svc, svcId, [dependency] as Dependency[], satisfiyHook)

        when:
        def isDependsOn = svcHolder.isDependsOn(Mock(Dependency) {
            getServiceId() >> new QualifiedServiceId('depId', QualifiedServiceId.FROM_LOCAL)
        })

        then:
        noExceptionThrown()
        isDependsOn
        ! svcHolder.isResolved()
        ! svcHolder.isInjected()
        ! svcHolder.isSatisfied()
        ! svcHolder.isActivated()

        where:
        from    | svcId     | satisfiyHook
        'local' | 'svcId'   | Mock(ISatisfyHook)
    }

    def 'Test get unactivated services'() {
        given:
        def dependency = Mock(Dependency) {
            getServiceId() >> new QualifiedServiceId('depId', QualifiedServiceId.FROM_ANY)
        }
        def dependency2 = Mock(Dependency) {
            getServiceId() >> new QualifiedServiceId('depId2', QualifiedServiceId.FROM_ANY)
        }
        def svc = Mock(IInjectable)
        def svcActivator = Mock(ServiceActivator)
        def svcHolder = new ServiceHolder(from, svc, svcId, [dependency, dependency2] as Dependency[], satisfiyHook)
        svcHolder.setDependency(Mock(ServiceHolder) {
            isActivated() >> true
            getQualifiedId() >> new QualifiedServiceId('depId2', QualifiedServiceId.FROM_LOCAL)
        }, svcActivator)

        when:
        def unactivatedSvcs = svcHolder.getUnactivatedServices()

        then:
        noExceptionThrown()
        unactivatedSvcs.size() == 1
        unactivatedSvcs.get(0).dependency() == dependency

        where:
        from    | svcId     | satisfiyHook
        'local' | 'svcId'   | Mock(ISatisfyHook)
    }

    def 'Test resolve but dependent service is not set'() {
        given:
        def dependency = Mock(Dependency) {
            getServiceId() >> Mock(QualifiedServiceId) {
                getId() >> 'depId'
            }
        }
        def svc = Mock(IInjectable)
        def svcHolder = new ServiceHolder(from, svc, svcId, [dependency] as Dependency[], satisfiyHook)

        when:
        svcHolder.resolve()

        then:
        thrown(ServiceException)
        ! svcHolder.isResolved()
        ! svcHolder.isInjected()
        ! svcHolder.isSatisfied()
        ! svcHolder.isActivated()

        where:
        from    | svcId     | satisfiyHook
        'local' | 'svcId'   | Mock(ISatisfyHook)
    }

    def 'Test resolve but dependent service is not resolved'() {
        given:
        def dependency = Mock(Dependency) {
            getServiceId() >> Mock(QualifiedServiceId) {
                getId() >> 'depId'
            }
            toString() >> 'depId'
        }
        def svc = Mock(IInjectable) {
            isOption('depId') >> true
        }
        def svcHolder = new ServiceHolder(from, svc, svcId, [dependency] as Dependency[], satisfiyHook)
        def depSvcHolder = Mock(ServiceHolder) {
            getQualifiedId() >> Mock(QualifiedServiceId) {
                toString() >> 'depId'
                1 * isAssignTo(_) >> true
            }
            isResolved() >> false
        }
        def svcActivator = Mock(ServiceActivator)
        svcHolder.setDependency(depSvcHolder, svcActivator)

        when:
        svcHolder.resolve()

        then:
        thrown(ServiceException)
        ! svcHolder.isResolved()
        ! svcHolder.isInjected()
        ! svcHolder.isSatisfied()
        ! svcHolder.isActivated()

        where:
        from    | svcId     | satisfiyHook
        'local' | 'svcId'   | Mock(ISatisfyHook)
    }

    def 'Test resolve success'() {
        given:
        def dependency = Mock(Dependency) {
            getServiceId() >> Mock(QualifiedServiceId) {
                getId() >> 'depId'
            }
            toString() >> 'depId'
        }
        def svc = Mock(IInjectable) {
            isOption('depId') >> true
        }
        def svcHolder = new ServiceHolder(from, svc, svcId, [dependency] as Dependency[], satisfiyHook)
        def depSvcHolder = Mock(ServiceHolder) {
            getQualifiedId() >> Mock(QualifiedServiceId) {
                toString() >> 'depId'
                1 * isAssignTo(_) >> true
            }
            isActivated() >> true
        }
        def svcActivator = Mock(ServiceActivator)
        svcHolder.setDependency(depSvcHolder, svcActivator)

        when:
        svcHolder.resolve()

        then:
        noExceptionThrown()
        svcHolder.isResolved()
        ! svcHolder.isInjected()
        ! svcHolder.isSatisfied()
        ! svcHolder.isActivated()

        where:
        from    | svcId     | satisfiyHook
        'local' | 'svcId'   | Mock(ISatisfyHook)
    }

    def 'Test resolve a resolved service'() {
        given:
        def dependency = Mock(Dependency) {
            getServiceId() >> Mock(QualifiedServiceId) {
                getId() >> 'depId'
            }
            toString() >> 'depId'
        }
        def svc = Mock(IInjectable) {
            isOption('depId') >> true
        }
        def svcHolder = new ServiceHolder(from, svc, svcId, [dependency] as Dependency[], satisfiyHook)
        def depSvcHolder = Mock(ServiceHolder) {
            getQualifiedId() >> Mock(QualifiedServiceId) {
                toString() >> 'depId'
                1 * isAssignTo(_) >> true
            }
            isActivated() >> true
        }
        def svcActivator = Mock(ServiceActivator)
        svcHolder.setDependency(depSvcHolder, svcActivator)
        svcHolder.resolve()

        when:
        svcHolder.resolve()

        then:
        noExceptionThrown()
        svcHolder.isResolved()
        ! svcHolder.isInjected()
        ! svcHolder.isSatisfied()
        ! svcHolder.isActivated()

        where:
        from    | svcId     | satisfiyHook
        'local' | 'svcId'   | Mock(ISatisfyHook)
    }

    def 'Test inject dependency which is not at injected'() {
        given:
        def dependency = Mock(Dependency) {
            getServiceId() >> Mock(QualifiedServiceId) {
                getId() >> 'depId'
            }
            toString() >> 'depId'
        }
        def svc = Mock(IInjectable) {
            isOption('depId') >> true
        }
        def svcHolder = new ServiceHolder(from, svc, svcId, [dependency] as Dependency[], satisfiyHook)
        def depSvcHolder = Mock(ServiceHolder) {
            getQualifiedId() >> Mock(QualifiedServiceId) {
                toString() >> 'depId'
                1 * isAssignTo(_) >> true
            }
            isResolved() >> true
            isInjected() >> false
        }
        def svcActivator = Mock(ServiceActivator)
        svcHolder.setDependency(depSvcHolder, svcActivator)

        when:
        svcHolder.inject()

        then:
        thrown(ServiceException)
        ! svcHolder.isResolved()
        ! svcHolder.isInjected()
        ! svcHolder.isSatisfied()
        ! svcHolder.isActivated()

        where:
        from    | svcId     | satisfiyHook
        'local' | 'svcId'   | Mock(ISatisfyHook)
    }

    def 'Test inject dependency success'() {
        given:
        def dependency = Mock(Dependency) {
            getServiceId() >> Mock(QualifiedServiceId) {
                getId() >> 'depId'
            }
            toString() >> 'depId'
        }
        def svc = Mock(IInjectable) {
            isOption('depId') >> true
        }
        def svcHolder = new ServiceHolder(from, svc, svcId, [dependency] as Dependency[], satisfiyHook)
        def depSvc = Mock(Object)
        def depSvcHolder = Mock(ServiceHolder) {
            getId() >> 'depId'
            getQualifiedId() >> Mock(QualifiedServiceId) {
                toString() >> 'depId'
                1 * isAssignTo(_) >> true
            }
            getService() >> depSvc
            isActivated() >> true
        }
        def svcActivator = Mock(ServiceActivator)
        svcHolder.setDependency(depSvcHolder, svcActivator)

        when:
        svcHolder.inject()

        then:
        noExceptionThrown()
        1 * svc.injectObject(_ as Injection)
        svcHolder.isResolved()
        svcHolder.isInjected()
        ! svcHolder.isSatisfied()
        ! svcHolder.isActivated()

        where:
        from    | svcId     | satisfiyHook
        'local' | 'svcId'   | Mock(ISatisfyHook)
    }

    def 'Test inject dependency twice'() {
        given:
        def dependency = Mock(Dependency) {
            getServiceId() >> Mock(QualifiedServiceId) {
                getId() >> 'depId'
            }
            toString() >> 'depId'
        }
        def svc = Mock(IInjectable) {
            isOption('depId') >> true
        }
        def svcHolder = new ServiceHolder(from, svc, svcId, [dependency] as Dependency[], satisfiyHook)
        def depSvc = Mock(Object)
        def depSvcHolder = Mock(ServiceHolder) {
            getId() >> 'depId'
            getQualifiedId() >> Mock(QualifiedServiceId) {
                toString() >> 'depId'
                1 * isAssignTo(_) >> true
            }
            getService() >> depSvc
            isActivated() >> true
        }
        def svcActivator = Mock(ServiceActivator)
        svcHolder.setDependency(depSvcHolder, svcActivator)

        when:
        svcHolder.inject()
        svcHolder.inject()

        then:
        noExceptionThrown()
        1 * svc.injectObject(_ as Injection)
        svcHolder.isResolved()
        svcHolder.isInjected()
        ! svcHolder.isSatisfied()
        ! svcHolder.isActivated()

        where:
        from    | svcId     | satisfiyHook
        'local' | 'svcId'   | Mock(ISatisfyHook)
    }

    def 'Test inject dependency by service factory'() {
        given:
        def dependency = Mock(Dependency) {
            getServiceId() >> Mock(QualifiedServiceId) {
                getId() >> 'depId'
            }
            toString() >> 'depId'
        }
        def svc = Mock(IInjectable) {
            isOption('depId') >> true
        }
        def svcHolder = new ServiceHolder(from, svc, svcId, [dependency] as Dependency[], satisfiyHook)
        def depSvc = Mock(IServiceFactory) {
            1 * createService(svc) >> Mock(Object)
        }
        def depSvcHolder = Mock(ServiceHolder) {
            getId() >> 'depId'
            getQualifiedId() >> Mock(QualifiedServiceId) {
                toString() >> 'depId'
                1 * isAssignTo(_) >> true
            }
            getService() >> depSvc
            isActivated() >> true
        }
        def svcActivator = Mock(ServiceActivator)
        svcHolder.setDependency(depSvcHolder, svcActivator)

        when:
        svcHolder.inject()

        then:
        noExceptionThrown()
        1 * svc.injectObject(_ as Injection)
        svcHolder.isResolved()
        svcHolder.isInjected()
        ! svcHolder.isSatisfied()
        ! svcHolder.isActivated()

        where:
        from    | svcId     | satisfiyHook
        'local' | 'svcId'   | Mock(ISatisfyHook)
    }

    def 'Test satisfy dependency which is not satisfied'() {
        given:
        def dependency = Mock(Dependency) {
            getServiceId() >> Mock(QualifiedServiceId) {
                getId() >> 'depId'
            }
            toString() >> 'depId'
        }
        def svc = Mock(IInjectable) {
            isOption('depId') >> true
        }
        def svcHolder = new ServiceHolder(from, svc, svcId, [dependency] as Dependency[], satisfiyHook)
        def depSvc = Mock(Object)
        def depSvcHolder = Mock(ServiceHolder) {
            getId() >> 'depId'
            getQualifiedId() >> Mock(QualifiedServiceId) {
                toString() >> 'depId'
                1 * isAssignTo(_) >> true
            }
            getService() >> depSvc
            isActivated() >> true
        }
        def svcActivator = Mock(ServiceActivator)
        svcHolder.setDependency(depSvcHolder, svcActivator)

        when:
        svcHolder.satisfy()

        then:
        thrown(ServiceException)
        1 * svc.injectObject(_ as Injection)
        ! svcHolder.isResolved()
        ! svcHolder.isInjected()
        ! svcHolder.isSatisfied()
        ! svcHolder.isActivated()

        where:
        from    | svcId     | satisfiyHook
        'local' | 'svcId'   | Mock(ISatisfyHook)
    }

    def 'Test satisfy but the service cannot be satisfied'() {
        given:
        def dependency = Mock(Dependency) {
            getServiceId() >> Mock(QualifiedServiceId) {
                getId() >> 'depId'
            }
            toString() >> 'depId'
        }
        def svc = Mock(IInjectable) {
            isOption('depId') >> true
        }
        def satisfyHook  = Mock(ISatisfyHook) {
            1 * isSatisfied(_ as ServiceHolder) >> false
        }
        def svcHolder = new ServiceHolder(from, svc, svcId, [dependency] as Dependency[], satisfyHook)
        def depSvc = Mock(Object)
        def depSvcHolder = Mock(ServiceHolder) {
            getId() >> 'depId'
            getQualifiedId() >> Mock(QualifiedServiceId) {
                toString() >> 'depId'
                1 * isAssignTo(_) >> true
            }
            getService() >> depSvc
            isActivated() >> true
        }
        def svcActivator = Mock(ServiceActivator)
        svcHolder.setDependency(depSvcHolder, svcActivator)

        when:
        svcHolder.satisfy()

        then:
        thrown(ServiceException)
        1 * svc.injectObject(_ as Injection)
        ! svcHolder.isResolved()
        ! svcHolder.isInjected()
        ! svcHolder.isSatisfied()
        ! svcHolder.isActivated()

        where:
        from    | svcId
        'local' | 'svcId'
    }

    def 'Test satisfy service success'() {
        given:
        def dependency = Mock(Dependency) {
            getServiceId() >> Mock(QualifiedServiceId) {
                getId() >> 'depId'
            }
            toString() >> 'depId'
        }
        def svc = Mock(IInjectable) {
            isOption('depId') >> true
        }
        def satisfyHook  = Mock(ISatisfyHook) {
            1 * isSatisfied(_ as ServiceHolder) >> true
        }
        def svcHolder = new ServiceHolder(from, svc, svcId, [dependency] as Dependency[], satisfyHook)
        def depSvc = Mock(Object)
        def depSvcHolder = Mock(ServiceHolder) {
            getId() >> 'depId'
            getQualifiedId() >> Mock(QualifiedServiceId) {
                toString() >> 'depId'
                1 * isAssignTo(_) >> true
            }
            getService() >> depSvc
            isActivated() >> true
        }
        def svcActivator = Mock(ServiceActivator)
        svcHolder.setDependency(depSvcHolder, svcActivator)

        when:
        svcHolder.satisfy()

        then:
        noExceptionThrown()
        1 * svc.injectObject(_ as Injection)
        svcHolder.isResolved()
        svcHolder.isInjected()
        svcHolder.isSatisfied()
        ! svcHolder.isActivated()

        where:
        from    | svcId
        'local' | 'svcId'
    }

    def 'Test satisfy service twice'() {
        given:
        def dependency = Mock(Dependency) {
            getServiceId() >> Mock(QualifiedServiceId) {
                getId() >> 'depId'
            }
            toString() >> 'depId'
        }
        def svc = Mock(IInjectable) {
            isOption('depId') >> true
        }
        def satisfyHook  = Mock(ISatisfyHook) {
            1 * isSatisfied(_ as ServiceHolder) >> true
        }
        def svcHolder = new ServiceHolder(from, svc, svcId, [dependency] as Dependency[], satisfyHook)
        def depSvc = Mock(Object)
        def depSvcHolder = Mock(ServiceHolder) {
            getId() >> 'depId'
            getQualifiedId() >> Mock(QualifiedServiceId) {
                toString() >> 'depId'
                1 * isAssignTo(_) >> true
            }
            getService() >> depSvc
            isActivated() >> true
        }
        def svcActivator = Mock(ServiceActivator)
        svcHolder.setDependency(depSvcHolder, svcActivator)

        when:
        svcHolder.satisfy()
        svcHolder.satisfy()

        then:
        noExceptionThrown()
        1 * svc.injectObject(_ as Injection)
        svcHolder.isResolved()
        svcHolder.isInjected()
        svcHolder.isSatisfied()
        ! svcHolder.isActivated()

        where:
        from    | svcId
        'local' | 'svcId'
    }

    def 'Test activate service but its dependency is not activated'() {
        given:
        def dependency = Mock(Dependency) {
            getServiceId() >> Mock(QualifiedServiceId) {
                getId() >> 'depId'
            }
            toString() >> 'depId'
        }
        def svc = Mock(IInjectable) {
            isOption('depId') >> true
        }
        def satisfyHook  = Mock(ISatisfyHook) {
            0 * isSatisfied(_ as ServiceHolder) >> true
        }
        def svcHolder = new ServiceHolder(from, svc, svcId, [dependency] as Dependency[], satisfyHook)
        def depSvc = Mock(Object)
        def depSvcHolder = Mock(ServiceHolder) {
            getId() >> 'depId'
            getQualifiedId() >> Mock(QualifiedServiceId) {
                toString() >> 'depId'
                1 * isAssignTo(_) >> true
            }
            getService() >> depSvc
            isResolved() >> true
            isInjected() >> true
            isSatisfied() >> true
            isActivated() >> false
        }
        def svcActivator = Mock(ServiceActivator)
        svcHolder.setDependency(depSvcHolder, svcActivator)

        when:
        svcHolder.activate()

        then:
        thrown(ServiceException)
        0 * svc.injectObject(_ as Injection)
        ! svcHolder.isResolved()
        ! svcHolder.isInjected()
        ! svcHolder.isSatisfied()
        ! svcHolder.isActivated()

        where:
        from    | svcId
        'local' | 'svcId'
    }

    def 'Test activate service success'() {
        given:
        def dependency = Mock(Dependency) {
            getServiceId() >> Mock(QualifiedServiceId) {
                getId() >> 'depId'
            }
            toString() >> 'depId'
        }
        def svc = Mock(IInjectableInitableLifecycle) {
            isOption('depId') >> true
            1 * onActivate()

        }
        def satisfyHook  = Mock(ISatisfyHook) {
            1 * isSatisfied(_ as ServiceHolder) >> true
        }
        def svcHolder = new ServiceHolder(from, svc, svcId, [dependency] as Dependency[], satisfyHook)
        def depSvc = Mock(Object)
        def depSvcHolder = Mock(ServiceHolder) {
            getId() >> 'depId'
            getQualifiedId() >> Mock(QualifiedServiceId) {
                toString() >> 'depId'
                1 * isAssignTo(_) >> true
            }
            getService() >> depSvc
            isResolved() >> true
            isInjected() >> true
            isSatisfied() >> true
            isActivated() >> true
        }
        def svcActivator = Mock(ServiceActivator)
        svcHolder.setDependency(depSvcHolder, svcActivator)

        when:
        svcHolder.activate()

        then:
        noExceptionThrown()
        1 * svc.injectObject(_ as Injection)
        svcHolder.isResolved()
        svcHolder.isInjected()
        svcHolder.isSatisfied()
        svcHolder.isActivated()

        where:
        from    | svcId
        'local' | 'svcId'
    }

    def 'Test activate service twice'() {
        given:
        def dependency = Mock(Dependency) {
            getServiceId() >> Mock(QualifiedServiceId) {
                getId() >> 'depId'
            }
            toString() >> 'depId'
        }
        def svc = Mock(IInjectableInitableLifecycle) {
            isOption('depId') >> true
            1 * onActivate()
        }
        def satisfyHook  = Mock(ISatisfyHook) {
            1 * isSatisfied(_ as ServiceHolder) >> true
        }
        def svcHolder = new ServiceHolder(from, svc, svcId, [dependency] as Dependency[], satisfyHook)
        def depSvc = Mock(Object)
        def depSvcHolder = Mock(ServiceHolder) {
            getId() >> 'depId'
            getQualifiedId() >> Mock(QualifiedServiceId) {
                toString() >> 'depId'
                1 * isAssignTo(_) >> true
            }
            getService() >> depSvc
            isResolved() >> true
            isInjected() >> true
            isSatisfied() >> true
            isActivated() >> true
        }
        def svcActivator = Mock(ServiceActivator)
        svcHolder.setDependency(depSvcHolder, svcActivator)

        when:
        svcHolder.activate()
        svcHolder.activate()

        then:
        noExceptionThrown()
        1 * svc.injectObject(_ as Injection)
        svcHolder.isResolved()
        svcHolder.isInjected()
        svcHolder.isSatisfied()
        svcHolder.isActivated()

        where:
        from    | svcId
        'local' | 'svcId'
    }

    def 'Test set dependency when the service is activated'() {
        given:
        def dependency = Mock(Dependency) {
            getServiceId() >> new QualifiedServiceId('depId', QualifiedServiceId.FROM_ANY)
            toString() >> 'depId'
        }
        def svc = Mock(IInjectableInitableLifecycle) {
            isOptional('depId') >> true
            1 * onActivate()
        }
        def satisfyHook  = Mock(ISatisfyHook) {
            1 * isSatisfied(_ as ServiceHolder) >> true
        }
        def svcHolder = new ServiceHolder(from, svc, svcId, [dependency] as Dependency[], satisfyHook)
        def depSvc = Mock(Object)
        def depSvcHolder = Mock(ServiceHolder) {
            getId() >> 'depId'
            getQualifiedId() >> Mock(QualifiedServiceId) {
                toString() >> 'depId'
                1 * isAssignTo(_) >> true
            }
            getService() >> depSvc
            isResolved() >> true
            isInjected() >> true
            isSatisfied() >> true
            isActivated() >> true
        }
        def svcActivator = Mock(ServiceActivator)

        when:
        svcHolder.activate()
        svcHolder.setDependency(depSvcHolder, svcActivator)

        then:
        noExceptionThrown()
        1 * svc.onDependencyInject('depId', depSvc)
        svcHolder.isResolved()
        svcHolder.isInjected()
        svcHolder.isSatisfied()
        svcHolder.isActivated()

        where:
        from    | svcId
        'local' | 'svcId'
    }

    def 'Test set dependency by factory when the service is activated'() {
        given:
        def dependency = Mock(Dependency) {
            getServiceId() >> new QualifiedServiceId('depId', QualifiedServiceId.FROM_ANY)
            toString() >> 'depId'
        }
        def svc = Mock(IInjectableInitableLifecycle) {
            isOptional('depId') >> true
            1 * onActivate()
        }
        def satisfyHook  = Mock(ISatisfyHook) {
            1 * isSatisfied(_ as ServiceHolder) >> true
        }
        def svcHolder = new ServiceHolder(from, svc, svcId, [dependency] as Dependency[], satisfyHook)
        def realSvc = Mock(Object)
        def depSvc = Mock(IServiceFactory) {
            1 * createService(_) >> realSvc
        }
        def depSvcHolder = Mock(ServiceHolder) {
            getId() >> 'depId'
            getQualifiedId() >> Mock(QualifiedServiceId) {
                toString() >> 'depId'
                1 * isAssignTo(_) >> true
            }
            getService() >> depSvc
            isResolved() >> true
            isInjected() >> true
            isSatisfied() >> true
            isActivated() >> true
        }
        def svcActivator = Mock(ServiceActivator)

        when:
        svcHolder.activate()
        svcHolder.setDependency(depSvcHolder, svcActivator)

        then:
        noExceptionThrown()
        1 * svc.onDependencyInject('depId', realSvc)
        svcHolder.isResolved()
        svcHolder.isInjected()
        svcHolder.isSatisfied()
        svcHolder.isActivated()

        where:
        from    | svcId
        'local' | 'svcId'
    }

    def 'Test set dependency when the service is activated but the service is not instance IServiceLifecycle'() {
        given:
        def dependency = Mock(Dependency) {
            getServiceId() >> new QualifiedServiceId('depId', QualifiedServiceId.FROM_ANY)
            toString() >> 'depId'
        }
        def svc = Mock(IInjectableInitable) {
            isOptional('depId') >> true
        }
        def satisfyHook  = Mock(ISatisfyHook) {
            1 * isSatisfied(_ as ServiceHolder) >> true
        }
        def svcHolder = new ServiceHolder(from, svc, svcId, [dependency] as Dependency[], satisfyHook)
        def depSvc = Mock(Object)
        def depSvcHolder = Mock(ServiceHolder) {
            getId() >> 'depId'
            getQualifiedId() >> Mock(QualifiedServiceId) {
                toString() >> 'depId'
                1 * isAssignTo(_) >> true
            }
            getService() >> depSvc
            isResolved() >> true
            isInjected() >> true
            isSatisfied() >> true
            isActivated() >> true
        }
        def svcActivator = Mock(ServiceActivator)

        when:
        svcHolder.activate()
        svcHolder.setDependency(depSvcHolder, svcActivator)

        then:
        thrown(ServiceException)
//        0 * svc.onServiceInjected('depId', depSvc)
        svcHolder.isResolved()
        svcHolder.isInjected()
        svcHolder.isSatisfied()
        svcHolder.isActivated()

        where:
        from    | svcId
        'local' | 'svcId'
    }

    def 'Test set dependency when the service is deactivated'() {
        given:
        def dependency = Mock(Dependency) {
            getServiceId() >> new QualifiedServiceId('depId', QualifiedServiceId.FROM_ANY)
            toString() >> 'depId'
        }
        def svc = Mock(IInjectableInitableLifecycle) {
            isOptional('depId') >> true
            1 * onActivate()
        }
        def satisfyHook  = Mock(ISatisfyHook) {
            1 * isSatisfied(_ as ServiceHolder) >> true
        }
        def svcHolder = new ServiceHolder(from, svc, svcId, [dependency] as Dependency[], satisfyHook)
        def depSvcHolder = Mock(ServiceHolder) {
            getId() >> 'depId'
            getQualifiedId() >> Mock(QualifiedServiceId) {
                toString() >> 'depId'
                1 * isAssignTo(_) >> true
            }
            isResolved() >> true
            isInjected() >> true
            isSatisfied() >> true
            isActivated() >> false
        }
        def svcActivator = Mock(ServiceActivator) {
            1 * tryActivateService(depSvcHolder) >> Optional.ofNullable(null)
        }

        when:
        svcHolder.activate()
        svcHolder.setDependency(depSvcHolder, svcActivator)

        then:
        noExceptionThrown()
        0 * svc.onDependencyInject('depId', _)
        1 * depSvcHolder.addNotifier(_ as ServiceHolder.DependencyNotifier)
        svcHolder.isResolved()
        svcHolder.isInjected()
        svcHolder.isSatisfied()
        svcHolder.isActivated()

        where:
        from    | svcId
        'local' | 'svcId'
    }

    def 'Test set dependency when the service is deactivated but it can be activated'() {
        given:
        def dependency = Mock(Dependency) {
            getServiceId() >> new QualifiedServiceId('depId', QualifiedServiceId.FROM_ANY)
            toString() >> 'depId'
        }
        def svc = Mock(IInjectableInitableLifecycle) {
            isOptional('depId') >> true
            1 * onActivate()
        }
        def satisfyHook  = Mock(ISatisfyHook) {
            1 * isSatisfied(_ as ServiceHolder) >> true
        }
        def svcHolder = new ServiceHolder(from, svc, svcId, [dependency] as Dependency[], satisfyHook)
        def depSvcHolder = Mock(ServiceHolder) {
            getId() >> 'depId'
            getQualifiedId() >> Mock(QualifiedServiceId) {
                toString() >> 'depId'
                1 * isAssignTo(_) >> true
            }
            isResolved() >> true
            isInjected() >> true
            isSatisfied() >> true
            isActivated() >> false
            getService() >> realSvc
        }
        def svcActivator = Mock(ServiceActivator) {
            1 * tryActivateService(depSvcHolder) >> Optional.ofNullable(realSvc)
        }

        when:
        svcHolder.activate()
        svcHolder.setDependency(depSvcHolder, svcActivator)

        then:
        noExceptionThrown()
        0 * svc.onDependencyInject('depId', _)
        0 * depSvcHolder.addNotifier(_ as ServiceHolder.DependencyNotifier)
        1 * svc.onDependencyInject('depId', realSvc)
        svcHolder.isResolved()
        svcHolder.isInjected()
        svcHolder.isSatisfied()
        svcHolder.isActivated()

        where:
        from    | svcId     | realSvc
        'local' | 'svcId'   | new Object()
    }

    def 'Test deactivate service which is not activated'() {
        given:
        def svc = Mock(IInjectableInitableLifecycle)
        def satisfyHook  = Mock(ISatisfyHook)
        def svcHolder = new ServiceHolder(from, svc, svcId, [] as Dependency[], satisfyHook)

        when:
        svcHolder.deactivate()

        then:
        noExceptionThrown()
        ! svcHolder.isResolved()
        ! svcHolder.isInjected()
        ! svcHolder.isSatisfied()
        ! svcHolder.isActivated()
        svcHolder.isDeactivated()

        where:
        from    | svcId
        'local' | 'svcId'
    }

    def 'Test deactivate service'() {
        given:
        def svc = Mock(IInjectableInitableLifecycle) {
            1 * onDeactivate()
        }
        def satisfyHook  = Mock(ISatisfyHook) {
            1 * isSatisfied(_) >> true
        }
        def svcHolder = new ServiceHolder(from, svc, svcId, [] as Dependency[], satisfyHook)
        svcHolder.activate()

        when:
        svcHolder.deactivate()

        then:
        noExceptionThrown()
        ! svcHolder.isResolved()
        ! svcHolder.isInjected()
        ! svcHolder.isSatisfied()
        ! svcHolder.isActivated()
        svcHolder.isDeactivated()

        where:
        from    | svcId
        'local' | 'svcId'
    }

    def 'Test tagged service'() {
        given:
        def svc = Mock(ITaggedService) {
            1 * getTags() >> ['tag']
        }
        def satisfyHook  = Mock(ISatisfyHook)
        def svcHolder = new ServiceHolder(from, svc, svcId, [] as Dependency[], satisfyHook)

        when:
        svcHolder.serviceTags() == ['tag'] as String[]

        then:
        noExceptionThrown()

        where:
        from    | svcId
        'local' | 'svcId'
    }

    interface IInjectableInitable extends IInjectable {}

    interface IInjectableInitableLifecycle extends IInjectable, IServiceLifecycle {}

    interface ITaggedService extends ITagged, IService {}
}
